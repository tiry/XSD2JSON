
Simple command line utility to convert a XSD schema to Nuxeo Studio JSON representation


Build :

    mvn clean install

The build will generate a "uberjar" that can directly be run

    java -jar target/xsd2json-5.6-SNAPSHOT-with-deps.jar <schemaname> <prefix> <xsdfile>

    java -jar target/xsd2json-5.6-SNAPSHOT-with-deps.jar dublincore dc src/test/resources/dublincore.xsd

restult will be output in schemaname.json file as well as in stdout

In order to allow direct generation of an associated denormalized jar, the command automatically generate a .xPathes that lists the xpath for each properties of the target XSD.

Using this file, you can do a selection of the fields you want to de-normalize and save it in an other file.

Once done, you can rerun the command to generate 2 schemas : the one corresponding to the XSD and the associates de-normalized sub schema.


    java -jar target/xsd2json-5.6-SNAPSHOT-with-deps.jar <schemaname> <prefix> <xsdfile> <xpathSelectionPath> <denormalizedSchemaName> <denormalizedSchemaPrefix>


