package com.cabolabs.loadehr

import com.cabolabs.ehrserver.*

class Main {

   private static String PS = System.getProperty("file.separator")
   
   static void main(String[] args)
   {
      //def client = new EhrServerClient('http://', 'localhost', 8090, '/ehr')
      def client = new EhrServerAsyncClient('http://', 'localhost', 8090, '/ehr')
      //def client = new EhrServerAsyncClient('http://', 'cabolabs-ehrserver.rhcloud.com', 80, '/')
      client.login('orgman', 'orgman', '123456')
      
      def loader = new LoadEhr(client)
      //loader.createEhrs(100)
      loader.commitBasicDemographic()
   }
   
}
