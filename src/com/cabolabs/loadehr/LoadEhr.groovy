package com.cabolabs.loadehr

import static groovyx.gpars.GParsPool.withPool
import com.cabolabs.ehrserver.EhrServerClient

class LoadEhr {

   EhrServerClient ehrserver
   Random random = new Random()
   
   def composers = [
   
   ]
   
   LoadEhr (EhrServerClient ehrserver)
   {
      this.ehrserver = ehrserver
      
      // TODO: load composers from JSON
   }

   def createEhrs(int amount = 1)
   {
      println "Creating $amount EHRs"
      if (amount < 1) amount = 1
      

      // TODO: create a thread per each 40 EHRs
      //withPool(5) {
      //   (1..amount).eachParallel {
      (1..amount).each {
            println it
            def res
            res = ehrserver.createEhr( java.util.UUID.randomUUID() as String )
            println res.status +' '+ res.message
         }
      //}
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
      def template = templates.find { it.templateId == 'datos_demograficos.es.v1' }
      
      if (!template)
      {
         println "Template datos_demograficos.es.v1 is not loaded in the EHRServer"
         return
      }
      
      
      def compo = loadTaggedDemographicInstance()
      
      def ehrs = ehrserver.getEhrs()
      
      withPool(5) {
         ehrs.eachParallel { ehr ->
            
            setTagsDemographicInstane( pickComposer() )
            ehrserver.commit(ehr.uid, compo)
         }
      }
      
      
   }
   
   def loadTaggedDemographicInstance()
   {
   }
   
   def pickComposer()
   {
   }
   
   def setTagsDemographicInstane(Map composerData)
   {
   }
   
   Date generateDateOfBirth(int minAge, int maxAge)
   {
      new Date() - random.nextInt((maxAge - minAge)*365) - minAge*365
   }
}