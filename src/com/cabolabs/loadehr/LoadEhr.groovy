package com.cabolabs.loadehr

import com.cabolabs.ehrserver.*
import java.text.SimpleDateFormat
import groovy.json.*
import groovy.time.TimeCategory

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
   List drugs     = []
   List generos = [
      [name: 'Masculino', code:'at0003'],
      [name: 'Femenino',  code:'at0004']
   ]
   List severities = [
      [name: 'Leve',      code:'at0047'],
      [name: 'Moderada',  code:'at0048'],
      [name: 'Severo',    code:'at0049']
   ]

   LoadEhr (EhrServerAsyncClient ehrserver)
   {
      this.ehrserver = ehrserver

      loadComposers()
      loadDiagnosis()
      loadTexts()
      loadDrugs()
   }

   LoadEhr (EhrServerClient ehrserver)
   {
      this.ehrserver = ehrserver

      loadComposers()
      loadDiagnosis()
      loadTexts()
      loadDrugs()
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

   def loadDrugs()
   {
      // load composers from JSON
      def data_drugs = new File('.'+PS+'resources'+PS+'data'+PS+'drogas_snomed.json')
      def jsonSlurper = new JsonSlurper()
      this.drugs = jsonSlurper.parseText(data_drugs.text)
   }

   def loadTexts()
   {
      // load composers from JSON
      def data_texts = new File('.'+PS+'resources'+PS+'data'+PS+'random_texts.json')
      def jsonSlurper = new JsonSlurper()
      this.texts = jsonSlurper.parseText(data_texts.text)
   }

   def createEhrs(int amount = 0)
   {
      if (amount <= 0)
      {
         println "not creating new EHRs"
         return
      }

      println "Creating $amount EHRs"

      (1..amount).each {
         println "Creating EHR $it"
         def res
         res = ehrserver.createEhr( java.util.UUID.randomUUID() as String )
         println '> '+ res.message +' ehrUid: '+ res.ehrUid
      }
   }

   def commitBasicDemographic(int offset = 0)
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

      def res, composerData
      def ehrs = ehrserver.getEhrs(50, offset)

      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.eachWithIndex { ehr, i ->

            println "Commit $i tid:"+ template.templateId

            // pick composer / committer
            composerData = this.composers[ random.nextInt(this.composers.size) ]

            final_compo = setTagsDemographicInstance(compo, composerData)

            res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')

            println '> '+ res
         }

         offset = ehrs.result.pagination.nextOffset
         ehrs = ehrserver.getEhrs(50, offset) // get next 50
      }
   }

   def commitCodedDiagnosis(int amountPerEHR = 1, int offset = 0)
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

      def res, composerData
      def ehrs = ehrserver.getEhrs(50, offset)
      def i = 1
      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->

            (1..amountPerEHR).each {

               println "Commit $i tid:"+ template.templateId

               // pick composer / committer
               composerData = this.composers[ random.nextInt(this.composers.size) ]

               final_compo = setTagsCodedDiagnosisInstance(compo, composerData) // ***

               res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')

               println '> '+ res
               i++
            }
         }

         offset = ehrs.result.pagination.nextOffset
         ehrs = ehrserver.getEhrs(50, offset) // get next 50
      }
   }

   def commitWeightControl(int amountPerEHR = 1, int offset = 0)
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

      def res, composerData
      def ehrs = ehrserver.getEhrs(50, offset)
      def i = 1
      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->

            (1..amountPerEHR).each {

               println "Commit $i tid:"+ template.templateId

               // pick composer / committer
               composerData = this.composers[ random.nextInt(this.composers.size) ]

               final_compo = setTagsWeightControlInstance(compo, composerData) // ***

               res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')

               println '> '+ res
               i++
            }
         }

         offset = ehrs.result.pagination.nextOffset
         ehrs = ehrserver.getEhrs(50, offset) // get next 50
      }
   }

   def commitMedicationPresription(int amountPerEHR = 1, int offset = 0)
   {
      if (!this.templates) this.templates = ehrserver.getTemplates().result
      def template = this.templates.find { it.templateId == 'prescripcion_medicamentos.es.v1' }

      if (!template)
      {
         println "Template prescripcion_medicamentos.es.v1 is not loaded in the EHRServer"
         return
      }

      String compo = loadTaggedMedicationPresriptionInstance() // ***
      String final_compo

      def res, composerData
      def ehrs = ehrserver.getEhrs(50, offset)
      def i = 1
      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->

            (1..amountPerEHR).each {

               println "Commit $i tid:"+ template.templateId

               // pick composer / committer
               composerData = this.composers[ random.nextInt(this.composers.size) ]

               final_compo = setTagsMedicationPrescriptionInstance(compo, composerData) // ***

               res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')

               println '> '+ res
               i++
            }
         }

         offset = ehrs.result.pagination.nextOffset
         ehrs = ehrserver.getEhrs(50, offset) // get next 50
      }
   }

   def commitObstetricHistory(int offset = 0)
   {
      if (!this.templates) this.templates = ehrserver.getTemplates().result
      def template = this.templates.find { it.templateId == 'historial_obstetrico.es.v1' }

      if (!template)
      {
         println "Template historial_obstetrico.es.v1 is not loaded in the EHRServer"
         return
      }

      String compo = loadTaggedObstetricHistoryInstance() // ***
      String final_compo

      def res, composerData
      def ehrs = ehrserver.getEhrs(50, offset)
      def i = 1

      while (ehrs.result.ehrs.size() > 0) // pagination loop
      {
         ehrs.result.ehrs.each { ehr ->

            // TODO: for age > 40, do not set data for current pregnancy, just history.
            // Now all data is set for current pregnancy.

            // commits only for female patients
            if (isFemale(ehr.uid) && ageLowerThan(ehr.uid, 40))
            {
               //println "female < 40"

               // random skips, some women don't have pregnancies
               if (random.nextInt(3) % 3 != 0) // 2/3 of the patients will have a pregnancy record
               {
                  println "Commit $i tid:"+ template.templateId

                  // pick composer / committer
                  composerData = this.composers[ random.nextInt(this.composers.size) ]

                  final_compo = setTagsObstetricHistoryInstance(compo, composerData) // ***

                  res = ehrserver.commit(ehr.uid, final_compo, (composerData.first_name+" "+composerData.last_name), 'CABOLABS-LOADEHR')

                  println '> '+ res
                  i++
               }
            }
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

   def loadTaggedMedicationPresriptionInstance()
   {
      def compo = new File('.'+PS+'resources'+PS+'tagged_compositions'+PS+'prescripcion_medicamentos.es.v1.xml')
      return compo.text
   }

   def loadTaggedObstetricHistoryInstance()
   {
      def compo = new File('.'+PS+'resources'+PS+'tagged_compositions'+PS+'historial_obstetrico.es.v1_tagged.xml')
      return compo.text
   }

   def setTagsDemographicInstance(String tagged_compo, Map composerData)
   {
      def commit_time = formattedDateTime(new Date())

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
        '[[TIME_COMMITTED:::DATETIME]]'  : commit_time,
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

   def setTagsCodedDiagnosisInstance(String tagged_compo, Map composerData)
   {
      def commit_time = formattedDateTime(new Date())
      def start_time  = formattedDateTime(pastStartTime())

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
        '[[TIME_COMMITTED:::DATETIME]]'   : commit_time,
        '[[VERSION_ID:::VERSION_ID]]'     : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        '[[COMPOSITION_DATE:::DATETIME]]' : start_time,
        '[[COMPOSITION_SETTING_VALUE:::STRING]]' : 'Atencion medica primaria',
        '[[COMPOSITION_SETTING_CODE:::STRING]]'  : '228',

        '[[ultima_actualizacion:::DATETIME]]': start_time,
        '[[problema:::CODEDTEXT_VALUE]]'     : diagnosisData.name,
        '[[problema:::CODEDTEXT_CODE]]'      : diagnosisData.conceptid,
        '[[descripcion:::STRING]]'           : text,
        '[[reconocimiento:::DATETIME]]'      : start_time,
        '[[Severidad:::CODEDTEXT_VALUE]]'    : severity.name,
        '[[Severidad:::CODEDTEXT_CODE]]'     : severity.code,
      ]

      data.each { k, v ->
         //println "$k : $v"
         tagged_compo = tagged_compo.replace(k, v) // reaplace all strings
      }

      return tagged_compo
   }

   def setTagsWeightControlInstance(String tagged_compo, Map composerData)
   {
      def commit_time = formattedDateTime(new Date())
      def start_time  = formattedDateTime(pastStartTime())

      def weight = random.nextInt(105) + 45         // 45..150 Kg
      def height = (random.nextInt(55) + 140) / 100 // 1.40..1.95 m
      def imc    = weight / (height**2)

      // pick text
      def text = texts[random.nextInt(this.texts.size)]

      def data = [
        '[[CONTRIBUTION:::UUID]]'         : java.util.UUID.randomUUID() as String,
        '[[COMMITTER_ID:::UUID]]'         : composerData.uid,
        '[[COMMITTER_NAME:::STRING]]'     : composerData.first_name+" "+composerData.last_name,
        '[[COMPOSER_ID:::UUID]]'          : composerData.uid,
        '[[COMPOSER_NAME:::STRING]]'      : composerData.first_name+" "+composerData.last_name,
        '[[TIME_COMMITTED:::DATETIME]]'   : commit_time,
        '[[VERSION_ID:::VERSION_ID]]'     : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        '[[COMPOSITION_DATE:::DATETIME]]' : start_time,
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

   def setTagsMedicationPrescriptionInstance(String tagged_compo, Map composerData)
   {
      def commit_time = formattedDateTime(new Date())
      def start_time  = formattedDateTime(pastStartTime())

      def drug, drug_name, drug_code
      drug = drugs[random.nextInt(this.texts.size)]
      drug_name = drug.drug
      drug_code = drug.conceptid

      // pick text
      def text1 = texts[random.nextInt(this.texts.size)]
      def text2 = texts[random.nextInt(this.texts.size)]

      def data = [
        '[[CONTRIBUTION:::UUID]]'         : java.util.UUID.randomUUID() as String,
        '[[COMMITTER_ID:::UUID]]'         : composerData.uid,
        '[[COMMITTER_NAME:::STRING]]'     : composerData.first_name+" "+composerData.last_name,
        '[[COMPOSER_ID:::UUID]]'          : composerData.uid,
        '[[COMPOSER_NAME:::STRING]]'      : composerData.first_name+" "+composerData.last_name,
        '[[TIME_COMMITTED:::DATETIME]]'   : commit_time,
        '[[VERSION_ID:::VERSION_ID]]'     : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        '[[COMPOSITION_DATE:::DATETIME]]' : start_time,
        '[[COMPOSITION_SETTING_VALUE:::STRING]]' : 'Atencion medica primaria',
        '[[COMPOSITION_SETTING_CODE:::STRING]]'  : '228',

        '[[orden_narativa:::INSTRUCTION_NARRATIVE_VALUE]]' : text1,
        '[[Medicamento_nombre:::STRING]]'      : drug_name,
        '[[Medicamento_codigo:::STRING]]'      : drug_code,

        '[[Indicaciones_generales:::STRING]]'  : text2,
        '[[Dosis:::STRING]]'                   : '100mg', // TODO: generar dosis random
        '[[Temporizacion:::STRING]]'           : 'Cada 12 horas', // TODO: generar frecuencias random
        '[[Numero_de_repeticiones:::INTEGER]]' : '1',
        '[[Sustitucion_de_marca_permitida:::BOOLEAN]]' : 'true',

        // each 12 hours in HL7 GTS
        // this doesnt work with XML directly, needs the CDATA to validate.
        '[[Orden:::ACTIVITY_TIMING_VALUE]]' : $/
           <![CDATA[
           <effectiveTime xsi:type="PIVL_TS">
             <period value="12" unit="h"/>
           </effectiveTime>
           ]]>
        /$,
        '[[Orden:::ACTIVITY_TIMING_FORMALISM]]' : 'HL7_GTS'
      ]

      data.each { k, v ->
         tagged_compo = tagged_compo.replace(k, v) // reaplace all strings
      }

      return tagged_compo
   }

   def setTagsObstetricHistoryInstance(String tagged_compo, Map composerData)
   {
      def commit_time = formattedDateTime(new Date())
      def start_time  = formattedDateTime(pastStartTime())

      def num = random.nextInt(3) + 1 // 1..3

      // pick text
      def text1 = texts[random.nextInt(this.texts.size)]
      def text2 = texts[random.nextInt(this.texts.size)]

      def data = [
        '[[CONTRIBUTION:::UUID]]'         : java.util.UUID.randomUUID() as String,
        '[[COMMITTER_ID:::UUID]]'         : composerData.uid,
        '[[COMMITTER_NAME:::STRING]]'     : composerData.first_name+" "+composerData.last_name,
        '[[COMPOSER_ID:::UUID]]'          : composerData.uid,
        '[[COMPOSER_NAME:::STRING]]'      : composerData.first_name+" "+composerData.last_name,
        '[[TIME_COMMITTED:::DATETIME]]'   : commit_time,
        '[[VERSION_ID:::VERSION_ID]]'     : (java.util.UUID.randomUUID() as String) +'::CABOLABS-LOADEHR::1',
        '[[COMPOSITION_DATE:::DATETIME]]' : start_time,
        '[[COMPOSITION_SETTING_VALUE:::STRING]]'  : 'Atencion medica primaria',
        '[[COMPOSITION_SETTING_CODE:::STRING]]'   : '228',

        '[[Fecha_de_actualizaci�n:::DATETIME]]'   : start_time,
        '[[Ha_estado_embarazada:::BOOLEAN]]'      : (num == 1) ? 'false' : 'true', // 1 is current pregnancy, > 1 is had other pregnancies
        '[[Gravidez:::INTEGER]]'                  : num.toString(), // total pregnancies included current
        '[[Paridad:::INTEGER]]'                   : (num - 1).toString(), // number of time the pregnancy was carrier above 20 weeks (all minus current)
        '[[Nacimientos_a_termino:::INTEGER]]'     : (num - 1).toString(),
        '[[Nacimientos_pretermino:::INTEGER]]'    : "0",
        '[[Abortos:::INTEGER]]'                   : "0",

        '[[Abortos_involuntarios:::INTEGER]]'     : "0",
        '[[Embarazos_interrumpidos:::INTEGER]]'   : "0",
        '[[Embarazos_ectopicos:::INTEGER]]'       : "0",
        '[[Nacidos_muertos:::INTEGER]]'           : "0",
        '[[Nacidos_vivos:::INTEGER]]'             : (num - 1).toString(),
        '[[Cesarea:::INTEGER]]'                   : "0",
        '[[Nacimientos_Multiples__M_:::INTEGER]]' : "0",
        '[[Nacidos_viviendo:::INTEGER]]'          : (num - 1).toString()
      ]

      data.each { k, v ->
         tagged_compo = tagged_compo.replace(k, v) // reaplace all strings
      }

      return tagged_compo
   }

   String formattedDateTime(Date d)
   {
      def format = new SimpleDateFormat(datetime_format_openEHR)
      format.format(d)
   }

   Date pastStartTime()
   {
      def from  = Date.parse('yyyy-MM-dd', '2010-01-01')
      def until = new Date()
      dateBetween(from..until)
   }

   // call: dateBetween(dateFrom..dateTo)
   Date dateBetween(Range<Date> range)
   {
      // random date between
      def res = range.from + random.nextInt(range.to - range.from + 1)

      // random time
      use( TimeCategory ) {
          res = res + random.nextInt(24).hours + random.nextInt(60).minutes + random.nextInt(60).seconds
      }
      res
   }

   boolean ehrContainsCompositionWithArchetypeID(String ehrUid, String archetypeId)
   {
      def result = ehrserver.getCompositions(ehrUid, 1, 0, archetypeId)
      return result.result.result.size() > 0
   }

   def testEhrContainsCompositionWithArchetypeID()
   {
      def arhcIds = ['openEHR-EHR-COMPOSITION.encounter.v1',
                     'openEHR-EHR-COMPOSITION.obstetric_history.v1']

      def ehrs = ehrserver.getEhrs(50, 0)
      ehrs.result.ehrs.each { ehr ->
         arhcIds.each { archId ->
            println ehr.uid +" "+ archId +" "+ ehrContainsCompositionWithArchetypeID(ehr.uid, archId)
         }
      }
   }

   // queries for commit consistency, like records that can only be for females or for males
   boolean isFemale(String ehrUid)
   {
      def query = $/
      {
         "query": {
            "name": "Femenino",
            "type": "composition",
            "format": "json",
            "criteriaLogic": "AND",
            "where": [{
               "archetypeId": "openEHR-EHR-ADMIN_ENTRY.basic_demographic.v1",
               "path": "/data[at0001]/items[at0002]/value",
               "rmTypeName": "DV_CODED_TEXT",
               "class": "DataCriteriaDV_CODED_TEXT",
               "allowAnyArchetypeVersion": false,
               "codeValue": "at0004",
               "codeOperand": "eq",
               "terminologyIdValue": "local",
               "terminologyIdOperand": "eq"
            }],
            "select": [],
            "group": "none"
         },
         "fromDate": "",
         "toDate": "",
         "retrieveData": false,
         "format": "json",
         "qehrId": "${ehrUid}",
         "composerUid": "",
         "composerName": ""
      }
      /$

      def result = ehrserver.executeGivenQuery(query, ehrUid)

      println result

      return result.data.size() > 0
   }

   def testIsFemale()
   {
      def ehrs = ehrserver.getEhrs(50, 0)
      ehrs.result.ehrs.each { ehr ->
         println ehr.uid +" "+ isFemale(ehr.uid)
      }
   }

   boolean ageGreaterThan(String ehrUid, int age)
   {
      def query = $/
      {
         "query": {
            "name": "Mayor de X a�os",
            "type": "composition",
            "format": "json",
            "criteriaLogic": "AND",
            "where": [{
               "archetypeId": "openEHR-EHR-ADMIN_ENTRY.basic_demographic.v1",
               "path": "/data[at0001]/items[at0006]/value",
               "rmTypeName": "DV_DATE",
               "class": "DataCriteriaDV_DATE",
               "allowAnyArchetypeVersion": false,
               "age_in_yearsValue": "${age}",
               "age_in_yearsOperand": "gt",
               "spec" : 1
            }],
            "select": [],
            "group": "none"
         },
         "fromDate": "",
         "toDate": "",
         "retrieveData": false,
         "format": "json",
         "qehrId": "${ehrUid}",
         "composerUid": "",
         "composerName": ""
      }
      /$

      def result = ehrserver.executeGivenQuery(query, ehrUid)

      //println result

      return result.data.size() > 0
   }
   boolean ageLowerThan(String ehrUid, int age)
   {
      def query = $/
      {
         "query": {
            "name": "Mayor de X a�os",
            "type": "composition",
            "format": "json",
            "criteriaLogic": "AND",
            "where": [{
               "archetypeId": "openEHR-EHR-ADMIN_ENTRY.basic_demographic.v1",
               "path": "/data[at0001]/items[at0006]/value",
               "rmTypeName": "DV_DATE",
               "class": "DataCriteriaDV_DATE",
               "allowAnyArchetypeVersion": false,
               "age_in_yearsValue": "${age}",
               "age_in_yearsOperand": "lt",
               "spec" : 1
            }],
            "select": [],
            "group": "none"
         },
         "fromDate": "",
         "toDate": "",
         "retrieveData": false,
         "format": "json",
         "qehrId": "${ehrUid}",
         "composerUid": "",
         "composerName": ""
      }
      /$

      def result = ehrserver.executeGivenQuery(query, ehrUid)

      println result

      return result.data.size() > 0
   }

   def testAgeLowerThan()
   {
      def ehrs = ehrserver.getEhrs(50, 0)
      ehrs.result.ehrs.each { ehr ->
         println ehr.uid +" "+ ageLowerThan(ehr.uid, 40)
      }
   }
}
