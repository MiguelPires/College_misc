# prototype
Minimal versions of SD project part 1

## instructions

Download JUDDI:

* cd tmp
* curl http://disciplinas.tecnico.ulisboa.pt/leic-sod/2014-2015/download/juddi-3.2.1_tomcat-7.0.57_port-8081.zip
* unzip juddi-3.2.1_tomcat-7.0.57_port-8081.zip
* chmod +x /tmp/juddi-3.2.1_tomcat-7.0.57_port-8081/bin/*.sh
* /tmp/juddi-3.2.1_tomcat-7.0.57_port-8081/bin/startup.sh

### dependencies

* cd uddi-naming
* mvn install

(the test contained in this module validates that UDDI is working properly)

### sd-id

* cd sd-id
* mvn clean package exec:java


### sd-store

* cd sd-store
* mvn clean package exec:java

