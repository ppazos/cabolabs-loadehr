package com.cabolabs.loadehr

import com.cabolabs.ehrserver.*

class Main {

   private static String PS = System.getProperty("file.separator")
   private static Random random = new Random()

   static void main(String[] args)
   {
      Date.metaClass.static.generateDateOfBirth = { int minAge, int maxAge ->

         new Date() - random.nextInt((maxAge - minAge)*365) - minAge*365
      }

      def ehrAmount = 200
      try { ehrAmount = Integer.parseInt(args[0]) } catch (all) { }


      //def client = new EhrServerClient('http://', 'localhost', 8090, '/ehr')
      def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/ehr', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      def res = client.login('orgman', 'orgman', '123456')
      if (res.status in 200..299)
      {}
      else
      {
         println "ERROR: "+ res.message
         System.exit(-1)
      }

/*
      def client = new EhrServerAsyncClient('http://', 'server001.cloudehrserver.com', 80, '/', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      //def client = new EhrServerAsyncClient('https://', 'server001.cloudehrserver.com', 443, '/', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      client.setAPIKey('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFwaWtleXhjd3FsaXlyZXJxbXlndmR6dnZmZWhranRpd3N0bXNkaXhyeGhtdHJqaWhrdXp0dnFuIiwiZXh0cmFkYXRhIjp7Im9yZ2FuaXphdGlvbiI6IjcyMzcyMiIsIm9yZ191aWQiOiI1NzkxOTk5MS1mYWExLTQ0YzQtODM2ZC1kYTgyY2I4MjkwZGMifSwiaXNzdWVkX2F0IjoiMjAxOC0wOS0xOVQyMDo1NjowOC4wNzktMDM6MDAifQ==.1OyxehbGZtm6vTIfhw1mWbj7M/lUFelsOXlaRhkgqdU=')
*/

      def loadehr = new LoadEhr(client)
      def start = System.currentTimeMillis()

      //loadehr.createEhrs(ehrAmount)
      //loadehr.commitBasicDemographic()
      //loadehr.commitCodedDiagnosis(3)
      //loadehr.commitWeightControl(3)
      //loadehr.commitMedicationPresription(2)
      //loadehr.commitSignosVitales(5)

      // Necesitan paciente femenina, pero el commit de demographic puede tardar
      // en indexar asi que deberia ejecutarse en segunda vuelta
      // data should be indexed before committing obstetric history
      //sleep(120 * 1000)
      //loadehr.commitObstetricHistory()

      //sleep(100 * 1000)
      //loadehr.commitPAPTestResults(3)
      // =========================================

      //loadehr.testIsFemale()
      //loadehr.testEhrContainsCompositionWithArchetypeID()
      loadehr.testAgeLowerThan()

      def now = System.currentTimeMillis()
      println '...'+ ((now - start) + ' ms')
      System.exit(0)
   }
}
