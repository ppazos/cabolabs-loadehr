package com.cabolabs.loadehr

import com.cabolabs.ehrserver.*
import java.text.SimpleDateFormat
import groovy.json.*

class LoadEhr {

   def ehrserver
   
   private static String PS = System.getProperty("file.separator")
   
   Random random = new Random()
   
   List composers = []
   List generos = [[name: 'Masculino', code:'at0003'], [name: 'Femenino', code:'at0004']]
   
   LoadEhr (EhrServerAsyncClient ehrserver)
   {
      this.ehrserver = ehrserver
      
      // load composers from JSON
      def data_composers = new File('.'+PS+'resources'+PS+'data'+PS+'composers.json')
      def jsonSlurper = new JsonSlurper()
      this.composers = jsonSlurper.parseText(data_composers.text)
   }
   
   LoadEhr (EhrServerClient ehrserver)
   {
      this.ehrserver = ehrserver
      
      // load composers from JSON
      def data_composers = new File('.'+PS+'resources'+PS+'data'+PS+'composers.json')
      def jsonSlurper = new JsonSlurper()
      this.composers = jsonSlurper.parseText(data_composers.text)
   }

   def createEhrs(int amount = 1)
   {
      println "Creating $amount EHRs"
      if (amount < 1) amount = 1
      
      (1..amount).each {
         println it
         def res
         res = ehrserver.createEhr( java.util.UUID.randomUUID() as String )
         println res.status +' '+ res.message +' '+ res.ehrUid
      }
   }
   
   def commitBasicDemographic()
   {
      // 1. check basic demogrpahic template is loaded
      // 2. get tagged instance
      // 3. get ehrs
      // 4. for each EHR, 
      // 4.1. inject values on instance tags
      // 4.2. commit instnace to EHR
      
      def templates = ehrserver.getTemplates()
      def template = templates.result.find { it.templateId == 'datos_demograficos.es.v1' }
      
      if (!template)
      {
         println "Template datos_demograficos.es.v1 is not loaded in the EHRServer"
         return
      }
      
      
      String compo = loadTaggedDemographicInstance()
      String final_compo

      def res
      def offset = 0
      def ehrs = ehrserver.getEhrs(50, offset)
      
      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->
         
            // pick composer / committer
            def composerData = this.composers[ random.nextInt(this.composers.size) ]
         
            final_compo = setTagsDemographicInstane(compo, composerData)
            
            //println final_compo
            
            res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')
            
            //println ">>> " + res
            println res.message
         }
         
         offset = ehrs.result.pagination.nextOffset
         ehrs = ehrserver.getEhrs(50, offset) // get next 50
      }
   }
   
   def loadTaggedDemographicInstance()
   {
      def compo = new File('.'+PS+'resources'+PS+'tagged_compositions'+PS+'Datos_demograficos_basicos_1.xml')
      return compo.text
   }
   

   def setTagsDemographicInstane(String tagged_compo, Map composerData)
   {
      def datetime_format_openEHR = "yyyyMMdd'T'HHmmss,SSSZ"
      def formatdt_oehr = new SimpleDateFormat(datetime_format_openEHR)
      def now_formatted = formatdt_oehr.format(new Date())
      
      // pick genero
      def genero = generos[random.nextInt(this.generos.size)]

      //println composerData
      
      def dob = generateDateOfBirth(18, 85)
      def date_format_openEHR = "yyyyMMdd"
      def formatd_oehr = new SimpleDateFormat(date_format_openEHR)
      def dob_formatted = formatd_oehr.format(dob)
      
      def data = [
        '[[CONTRIBUTION:::UUID]]'        : java.util.UUID.randomUUID() as String,
        '[[COMMITTER_ID:::UUID]]'        : composerData.uid,
        '[[COMMITTER_NAME:::STRING]]'    : composerData.first_name+" "+composerData.last_name,
        '[[COMPOSER_ID:::UUID]]'         : composerData.uid,
        '[[COMPOSER_NAME:::STRING]]'     : composerData.first_name+" "+composerData.last_name,
        '[[TIME_COMMITTED:::DATETIME]]'  : now_formatted,
        '[[VERSION_ID:::VERSION_ID]]'    : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        
        '[[GENERO:::CODEDTEXT_VALUE]]'   : genero.name,
        '[[GENERO:::CODEDTEXT_CODE]]'    : genero.code,
        '[[Fecha_de_nacimiento:::DATE]]' : dob_formatted
      ]
      
      data.each { k, v ->
         //println "$k : $v"
         tagged_compo = tagged_compo.replace(k, v) // reaplace all strings
      }
      
      return tagged_compo
   }
   
   Date generateDateOfBirth(int minAge, int maxAge)
   {
      new Date() - random.nextInt((maxAge - minAge)*365) - minAge*365
   }
}
