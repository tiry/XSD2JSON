
Simple command line utility to convert a XSD schema to Nuxeo Studio JSON representation


Build :

    mvn clean install

The build will generate a "uberjar" that can directly be run

    java -jar target/xsd2json-5.6-SNAPSHOT-with-deps.jar <schemaname> <prefix> <xsdfile>

    java -jar target/xsd2json-5.6-SNAPSHOT-with-deps.jar dublincore dc src/test/resources/dublincore.xsd

restult will be output in schemaname.json file as well as in stdout


