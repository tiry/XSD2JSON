package org.nuxeo.xsd.parser;

import java.io.File;
import java.io.IOException;

import org.nuxeo.common.utils.FileUtils;

public class Main {

    protected static void help() {
        System.out.println("name prefix xsdfilelocation");
    }

    public static void main(String args[]) throws IOException {

        String name = null;
        String prefix = null;
        String file = null;
        if (args.length == 3) {
            name = args[0];
            prefix = args[1];
            file = args[2];
        } else {
            help();
            return;
        }
        try {
            System.out.println("Running conversion on " + file);
            String json = XSD2JSON.asJSON(name, prefix, file);
            File out = new File(name + ".json");
            FileUtils.writeFile(out, json);
            System.out.println(json);
            System.out.println("Completed ok : result saved in file "
                    + out.getAbsolutePath());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
