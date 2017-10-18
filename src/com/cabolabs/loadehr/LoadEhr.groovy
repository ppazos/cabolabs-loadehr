package com.cabolabs.loadehr

import com.cabolabs.ehrserver.*
import java.text.SimpleDateFormat
import groovy.json.*

class LoadEhr {

   def ehrserver
   
   private static String PS = System.getProperty("file.separator")
   private static String datetime_format_openEHR = "yyyyMMdd'T'HHmmss,SSSZ"
   private static String date_format_openEHR = "yyyyMMdd"
   
   Random random = new Random()
   
   // cache of templates
   List templates
   
   List composers = []
   List diagnosis = []
   List texts     = []
   List generos = [[name: 'Masculino', code:'at0003'], [name: 'Femenino', code:'at0004']]
   List severities = [
      [name: 'Leve',     code:'at0047'], 
      [name: 'Moderada', code:'at0048'],
      [name: 'Severo',   code:'at0049']
   ]
   
   LoadEhr (EhrServerAsyncClient ehrserver)
   {
      this.ehrserver = ehrserver
      
      loadComposers()
      loadDiagnosis()
      loadTexts()
   }
   
   LoadEhr (EhrServerClient ehrserver)
   {
      this.ehrserver = ehrserver
      
      loadComposers()
      loadDiagnosis()
      loadTexts()
   }
   
   def loadComposers()
   {
      // load composers from JSON
      def data_composers = new File('.'+PS+'resources'+PS+'data'+PS+'composers.json')
      def jsonSlurper = new JsonSlurper()
      this.composers = jsonSlurper.parseText(data_composers.text)
   }
   
   def loadDiagnosis()
   {
      // load composers from JSON
      def data_diagnosis = new File('.'+PS+'resources'+PS+'data'+PS+'diagnosticos.json')
      def jsonSlurper = new JsonSlurper()
      this.diagnosis = jsonSlurper.parseText(data_diagnosis.text)
   }
   
   def loadTexts()
   {
      // load composers from JSON
      def data_texts = new File('.'+PS+'resources'+PS+'data'+PS+'random_texts.json')
      def jsonSlurper = new JsonSlurper()
      this.texts = jsonSlurper.parseText(data_texts.text)
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
      if (!this.templates) this.templates = ehrserver.getTemplates().result
      def template = this.templates.find { it.templateId == 'datos_demograficos.es.v1' }
      
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
   
   
   def commitCodedDiagnosis()
   {
      if (!this.templates) this.templates = ehrserver.getTemplates().result
      def template = this.templates.find { it.templateId == 'encuentro_diagnostico_codificado.es.v1' }
      
      if (!template)
      {
         println "Template encuentro_diagnostico_codificado.es.v1 is not loaded in the EHRServer"
         return
      }
      
      
      String compo = loadTaggedCodedDiagnosisInstance() // ***
      String final_compo

      def res
      def offset = 0
      def ehrs = ehrserver.getEhrs(50, offset)
      
      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->
         
            // pick composer / committer
            def composerData = this.composers[ random.nextInt(this.composers.size) ]
         
            final_compo = setTagsCodedDiagnosisInstane(compo, composerData) // ***
            
            //println final_compo
            
            res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')
            
            //println ">>> " + res
            println res.message
         }
         
         offset = ehrs.result.pagination.nextOffset
         ehrs = ehrserver.getEhrs(50, offset) // get next 50
      }
   }
   
   def commitWeightControl()
   {
      if (!this.templates) this.templates = ehrserver.getTemplates().result
      def template = this.templates.find { it.templateId == 'control_del_peso.es.v1' }
      
      if (!template)
      {
         println "Template control_del_peso.es.v1 is not loaded in the EHRServer"
         return
      }
      
      
      String compo = loadTaggedWeightControlInstance() // ***
      String final_compo

      def res
      def offset = 0
      def ehrs = ehrserver.getEhrs(50, offset)
      
      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->
         
            // pick composer / committer
            def composerData = this.composers[ random.nextInt(this.composers.size) ]
         
            final_compo = setTagsWeightControlInstane(compo, composerData) // ***
            
            res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')
            
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
   
   def loadTaggedCodedDiagnosisInstance()
   {
      def compo = new File('.'+PS+'resources'+PS+'tagged_compositions'+PS+'encuentro_diagnostico_codificado.es.v1.xml')
      return compo.text
   }
   
   def loadTaggedWeightControlInstance()
   {
      def compo = new File('.'+PS+'resources'+PS+'tagged_compositions'+PS+'control_del_peso.es.v1.xml')
      return compo.text
   }
   

   def setTagsDemographicInstane(String tagged_compo, Map composerData)
   {
      def formatdt_oehr = new SimpleDateFormat(datetime_format_openEHR)
      def now_formatted = formatdt_oehr.format(new Date())
      
      // pick genero
      def genero = generos[random.nextInt(this.generos.size)]
      
      def dob = Date.generateDateOfBirth(18, 85)
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
   
   def setTagsCodedDiagnosisInstane(String tagged_compo, Map composerData)
   {
      def formatdt_oehr = new SimpleDateFormat(datetime_format_openEHR)
      def now_formatted = formatdt_oehr.format(new Date())
      
      // pick diagnosis
      def diagnosisData = this.diagnosis[ random.nextInt(this.diagnosis.size) ]
      
      // pick severity
      def severity = severities[random.nextInt(this.severities.size)]
      
      // pick text
      def text = texts[random.nextInt(this.texts.size)]
      
      def data = [
        '[[CONTRIBUTION:::UUID]]'         : java.util.UUID.randomUUID() as String,
        '[[COMMITTER_ID:::UUID]]'         : composerData.uid,
        '[[COMMITTER_NAME:::STRING]]'     : composerData.first_name+" "+composerData.last_name,
        '[[COMPOSER_ID:::UUID]]'          : composerData.uid,
        '[[COMPOSER_NAME:::STRING]]'      : composerData.first_name+" "+composerData.last_name,
        '[[TIME_COMMITTED:::DATETIME]]'   : now_formatted,
        '[[VERSION_ID:::VERSION_ID]]'     : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        '[[COMPOSITION_DATE:::DATETIME]]' : now_formatted,
        '[[COMPOSITION_SETTING_VALUE:::STRING]]' : 'Atencion medica primaria',
        '[[COMPOSITION_SETTING_CODE:::STRING]]'  : '228',
        
        '[[Última_actualización:::DATETIME]]': now_formatted,
        '[[problema:::CODEDTEXT_VALUE]]'     : diagnosisData.name,
        '[[problema:::CODEDTEXT_CODE]]'      : diagnosisData.conceptid,
        '[[descripcion:::STRING]]'           : text,
        '[[reconocimiento:::DATETIME]]'      : now_formatted,
        '[[Severidad:::CODEDTEXT_VALUE]]'    : severity.name,
        '[[Severidad:::CODEDTEXT_CODE]]'     : severity.code,
      ]
      
      data.each { k, v ->
         //println "$k : $v"
         tagged_compo = tagged_compo.replace(k, v) // reaplace all strings
      }
      
      return tagged_compo
   }
   
   def setTagsWeightControlInstane(String tagged_compo, Map composerData)
   {
      def formatdt_oehr = new SimpleDateFormat(datetime_format_openEHR)
      def now_formatted = formatdt_oehr.format(new Date())
      
      def weight = random.nextInt(85) + 45 // 45..130 Kg
      def height = (random.nextInt(70) + 140) / 100 // 1.40..2.10 m
      def imc    = weight / (height**2)
      
      // pick text
      def text = texts[random.nextInt(this.texts.size)]
      
      def data = [
        '[[CONTRIBUTION:::UUID]]'         : java.util.UUID.randomUUID() as String,
        '[[COMMITTER_ID:::UUID]]'         : composerData.uid,
        '[[COMMITTER_NAME:::STRING]]'     : composerData.first_name+" "+composerData.last_name,
        '[[COMPOSER_ID:::UUID]]'          : composerData.uid,
        '[[COMPOSER_NAME:::STRING]]'      : composerData.first_name+" "+composerData.last_name,
        '[[TIME_COMMITTED:::DATETIME]]'   : now_formatted,
        '[[VERSION_ID:::VERSION_ID]]'     : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        '[[COMPOSITION_DATE:::DATETIME]]' : now_formatted,
        '[[COMPOSITION_SETTING_VALUE:::STRING]]' : 'Atencion medica primaria',
        '[[COMPOSITION_SETTING_CODE:::STRING]]'  : '228',
        
        '[[Peso:::DV_QUANTITY_MAGNITUDE]]'  : weight.toString(),
        '[[Peso:::DV_QUANTITY_UNITS]]'      : 'kg',
        '[[Observaciones:::STRING]]'        : text,
        
        '[[Altura:::DV_QUANTITY_MAGNITUDE]]': height.toString(),
        '[[Altura:::DV_QUANTITY_UNITS]]'    : 'm',
        
        '[[IMC:::DV_QUANTITY_MAGNITUDE]]'   : imc.toString(),
        '[[IMC:::DV_QUANTITY_UNITS]]'       : 'kg/m2',
      ]
      
      data.each { k, v ->
         tagged_compo = tagged_compo.replace(k, v) // reaplace all strings
      }
      
      return tagged_compo
   }
   
   /*
   // TODO: this can be metaprogarmming adding the method to Date
   Date generateDateOfBirth(int minAge, int maxAge)
   {
      new Date() - random.nextInt((maxAge - minAge)*365) - minAge*365
   }
   */
}
