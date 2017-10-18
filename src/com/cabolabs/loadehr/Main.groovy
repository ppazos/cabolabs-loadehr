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
   
   
      //def client = new EhrServerClient('http://', 'localhost', 8090, '/ehr')
      def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/ehr')
      //def client = new EhrServerAsyncClient('http://', 'cabolabs-ehrserver.rhcloud.com', 80, '/')
      client.login('orgman', 'orgman', '123456')
      
      def loader = new LoadEhr(client)
      
      def start = System.currentTimeMillis() 
      
      loader.createEhrs(500)
      loader.commitBasicDemographic()
      loader.commitCodedDiagnosis()
      loader.commitWeightControl()
      
      def now = System.currentTimeMillis()  
      println '...'+ ((now - start) + ' ms')
   }
   
}
