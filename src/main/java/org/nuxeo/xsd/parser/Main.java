package org.nuxeo.xsd.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.nuxeo.common.utils.FileUtils;

public class Main {

    protected static void help() {
        System.out.println("This command takes between 3 and 6 parameters");
        System.out.println("name : name of the schema to generate (required)");
        System.out.println("prefix : prefix of the schema to generate (required)");
        System.out.println("xsdfilelocation : location of the XSD file to parse (required)");
        System.out.println("fieldExtractionFile : file listing fields to extract to generate a de-normalized schema (optional, if empty de-normalization is not run)");
        System.out.println("subName : target name of the de-normalized schema (optional, defaults to sub + name)");
        System.out.println("subPrefix : target prefix of the de-normalized schema (optional, default to sub + prefix)");
        System.out.println("");
        System.out.println(" Example :");
        System.out.println(" mySchema myPrefix myXSD.xsd fields mySubSchema mySubPrefix");
    }

    public static void main(String args[]) throws IOException {

        String name = null;
        String prefix = null;
        String file = null;
        if (args.length >= 3) {
            name = args[0];
            prefix = args[1];
            file = args[2];
        } else {
            help();
            return;
        }
        String mappingFileName = null;
        String subName = null;
        String subPrefix = null;

        if (args.length >= 4) {
            mappingFileName = args[3];
        }
        if (args.length >= 5) {
            subName = args[4];
        }
        if (args.length >= 6) {
            subPrefix = args[5];
        }

        try {

            List<String> subFields2Extract = null;
            if (mappingFileName != null) {
                File mappingFile = new File(mappingFileName);
                if (!mappingFile.exists()) {
                    System.out.println("Unable to read " + mappingFileName
                            + " exiting ...");
                    System.exit(-1);
                }
                subFields2Extract = FileUtils.readLines(mappingFile);
            }

            System.out.println("Running conversion on " + file);
            GenerationResult result = XSD2JSON.asJSON(name, prefix, file,
                    subFields2Extract, subName, subPrefix);
            String json = result.getJsonSchemas();
            System.out.println(json);
            result.saveFiles();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
