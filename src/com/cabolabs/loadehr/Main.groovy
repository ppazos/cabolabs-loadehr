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

      def ehrAmount = 10
      try { ehrAmount = Integer.parseInt(args[0]) } catch (all) { }

      /*
      //def client = new EhrServerClient('http://', 'localhost', 8090, '/ehr')
      def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/ehr', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
  //    def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      def res = client.login('orgman', 'orgman', '123456')

      if (res.status in 200..299)
      {}
      else
      {
         println "ERROR: "+ res.message
         System.exit(-1)
      }
      */

      def client = new EhrServerAsyncClient('https://', 'server001.cloudehrserver.com', 443, '/', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      client.setAPIKey('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFwaWtleXBoa3FkdnFpYXl1cnRiZG9nZ2hjZXVwZHJ2ZXdjc2Zqc3BkeGNjeXpweXl0YmZsa2V5IiwiZXh0cmFkYXRhIjp7Im9yZ2FuaXphdGlvbiI6IjcyMzcyMiIsIm9yZ191aWQiOiI1NzkxOTk5MS1mYWExLTQ0YzQtODM2ZC1kYTgyY2I4MjkwZGMifSwiaXNzdWVkX2F0IjoiMjAxNy0xMC0yN1QxOTowNzo1My4zOTMtMDI6MDAifQ==.eWdKGScdgQNynMkWJ5alRAF7tN5t8eZ2veuHp0i43fc=')

      def loadehr = new LoadEhr(client)

      def start = System.currentTimeMillis()


      loadehr.createEhrs(ehrAmount)
      loadehr.commitBasicDemographic()
      loadehr.commitCodedDiagnosis(3)
      loadehr.commitWeightControl(3)
      loadehr.commitMedicationPresription(2)


      // data should be indexed before committing obstetric history
  //    sleep(120 * 1000)
      //loadehr.commitObstetricHistory()

      //loadehr.testIsFemale()
      //loadehr.testEhrContainsCompositionWithArchetypeID()
      //loadehr.testAgeLowerThan()

      def now = System.currentTimeMillis()
      println '...'+ ((now - start) + ' ms')
      System.exit(0)
   }
}
