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
   
      def ehrAmount = 5000
   
      //def client = new EhrServerClient('http://', 'localhost', 8090, '/ehr')
      //def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/ehr', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      //def client = new EhrServerAsyncClient('http://', 'cabolabs-ehrserver.rhcloud.com', 80, '/', (ehrAmount>1000 ? ehrAmount.intdiv(100) : 10))
      client.login('orgman', 'orgman', '123456')
      
      def loadehr = new LoadEhr(client)
      
      def start = System.currentTimeMillis() 
      
      loadehr.createEhrs(ehrAmount)
      loadehr.commitBasicDemographic()
      loadehr.commitCodedDiagnosis(2)
      loadehr.commitWeightControl(3)
      loadehr.commitMedicationPresription(2)
      
      def now = System.currentTimeMillis()  
      println '...'+ ((now - start) + ' ms')
   }
   
}
