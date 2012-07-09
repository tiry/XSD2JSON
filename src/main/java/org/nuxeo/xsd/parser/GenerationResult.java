package org.nuxeo.xsd.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.common.utils.FileUtils;

public class GenerationResult {

    protected String name;

    protected String jsonSchemas;

    protected List<String> xpathes;

    protected Map<String, String> nameMapping;

    public GenerationResult(String name, String json, List<String> xpathes,
            Map<String, String> nameMapping) {
        this.name = name;
        this.jsonSchemas = json;
        this.xpathes = xpathes;
        this.nameMapping = nameMapping;
    }

    public String getJsonSchemas() {
        return jsonSchemas;
    }

    public void setJsonSchemas(String jsonSchemas) {
        this.jsonSchemas = jsonSchemas;
    }

    public List<String> getXpathes() {
        return xpathes;
    }

    public void setXpathes(List<String> xpathes) {
        this.xpathes = xpathes;
    }

    public void saveFiles() throws IOException {
        File out = new File(name + ".json");
        FileUtils.writeFile(out, getJsonSchemas());

        System.out.println("result schema definitions saved in file "
                + out.getAbsolutePath());

        File xPathes = new File(name + ".xPathes");
        FileUtils.writeLines(xPathes, getXpathes());

        System.out.println("extracted xPathes saved in file "
                + xPathes.getAbsolutePath());

        if (nameMapping != null && nameMapping.size() > 0) {
            File mapping = new File(name + ".mapping");
            List<String> lines = new ArrayList<String>();
            for (String key : nameMapping.keySet()) {
                lines.add(key + " " + nameMapping.get(key));
            }
            FileUtils.writeLines(mapping, lines);

            System.out.println("resulting mapping saved in file "
                    + mapping.getAbsolutePath());
        }

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("JSON Schemas definition :\n");
        sb.append(getJsonSchemas());
        sb.append("\n\nExtracted XPathes :\n");
        for (String xp : getXpathes()) {
            sb.append(xp + "\n");
        }
        if (nameMapping != null && nameMapping.size() > 0) {
            sb.append("\n Resulting name mapping :\n");
            for (String key : nameMapping.keySet()) {
                sb.append("\n" + key);
                sb.append(" ==> ");
                sb.append(nameMapping.get(key));
            }
        }
        return sb.toString();
    }
}
