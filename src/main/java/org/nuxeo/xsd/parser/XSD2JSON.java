package org.nuxeo.xsd.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.core.schema.Namespace;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.ecm.core.schema.XSDLoader;
import org.nuxeo.ecm.core.schema.types.ComplexType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.QName;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.SchemaImpl;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.util.SimpleRuntime;

public class XSD2JSON {

    public final static List<String> supportedScalarTypes = Arrays.asList(
            "string", "date", "boolean", "integer", "double");

    public static GenerationResult asJSON(String name, String prefix,
            String xsdFileLocation, List<String> subFields2Extract,
            String subName, String subPrefix) throws Exception {
        return asJSON(name, prefix, new File(xsdFileLocation),
                subFields2Extract, subName, subPrefix);
    }

    public static GenerationResult asJSON(String name, String prefix,
            File xsdFile, List<String> subFields2Extract, String subSchemaName,
            String subSchemaPrefix) throws Exception {
        List<Schema> schemas = new ArrayList<Schema>();

        Schema schema = loadSchema(name, prefix, xsdFile);
        if (schema != null) {

            schemas.add(schema);

            // collect xPathes
            Map<String, Field> collectedFieldsByPath = collectXPathes("",
                    schema);
            List<String> xPathes = new ArrayList<String>(
                    collectedFieldsByPath.keySet());
            Collections.sort(xPathes);

            Map<String, String> nameMapping = null;

            // generate sub schema is needed
            if (subFields2Extract != null && subFields2Extract.size() > 0) {

                if (subSchemaName == null) {
                    subSchemaName = "sub" + name;
                }
                if (subSchemaPrefix == null) {
                    subSchemaPrefix = "sub" + prefix;
                }

                Namespace ns = schema.getNamespace();
                String targetURI = ns.uri.replace(name, subSchemaName);
                Namespace subNS = new Namespace(targetURI, subSchemaPrefix);
                Schema subSchema = new SchemaImpl(subSchemaName, subNS);

                nameMapping = new HashMap<String, String>();

                for (String xpath : subFields2Extract) {
                    Field subField = collectedFieldsByPath.get(xpath);
                    if (subField != null) {
                        String newSubFieldName = getTargetName(prefix, xpath);
                        nameMapping.put(xpath, subSchemaPrefix + ":"
                                + newSubFieldName);
                        QName newSubFieldQName = new QName(newSubFieldName);
                        subSchema.addField(newSubFieldQName,
                                subField.getType().getRef());
                    }
                }
                if (subSchema.getFields().size() > 0) {
                    schemas.add(subSchema);
                }
            }
            return new GenerationResult(name, asJSON(schemas), xPathes,
                    nameMapping);
        }
        return null;
    }

    protected static String getTargetName(String prefix, String name) {
        String targetName = name.substring(prefix.length() + 2);
        targetName = targetName.replaceAll("/", "_");
        return targetName;
    }

    protected static void collectXPathes(String prefix, Field field,
            Map<String, Field> collector) {
        if (field.getType().isSimpleType()) {
            collector.put(prefix + "/" + field.getName().getPrefixedName(),
                    field);
        } else if (field.getType().isListType()) {

            ListType lt = (ListType) field.getType();

            if (lt.getFieldType().isSimpleType()) {
                collector.put(prefix + "/" + field.getName().getPrefixedName()
                        + "[*]", field);
            } else {
                collectXPathes(prefix + "/" + field.getName().getPrefixedName()
                        + "[*]", lt.getField(), collector);
            }

        } else {
            ComplexType ct = (ComplexType) field.getType();
            for (Field subField : ct.getFields()) {
                String path = prefix + "/" + field.getName().getPrefixedName();
                if (field.getName().getLocalName().equals("item")
                        && prefix.endsWith("[*]")) {
                    path = prefix;
                }
                collectXPathes(path, subField, collector);

            }
        }

    }

    protected static Map<String, Field> collectXPathes(String prefix,
            Schema schema) {
        Map<String, Field> collector = new HashMap<String, Field>();
        for (Field field : schema.getFields()) {
            collectXPathes(prefix, field, collector);
        }
        return collector;
    }

    protected static Schema loadSchema(String name, String prefix, File xsdFile)
            throws Exception {
        if (!Framework.isInitialized()) {
            RuntimeService runtime = new SimpleRuntime();
            System.setProperty("nuxeo.home",
                    System.getProperty("java.io.tmpdir"));
            Framework.initialize(runtime);
        }
        SchemaManagerImpl schemaManager = new SchemaManagerImpl();
        XSDLoader loader = new XSDLoader(schemaManager);
        Schema schema = loader.loadSchema(name, prefix, xsdFile, true);

        return schema;
    }

    protected static String asJSON(List<Schema> schemas) throws JSONException {

        JSONObject schemasObject = new JSONObject();
        JSONObject rootObject = new JSONObject();

        for (Schema schema : schemas) {
            JSONObject schemaObject = new JSONObject();
            schemaObject.put("@prefix", schema.getNamespace().prefix);
            for (Field field : schema.getFields()) {
                addField(schemaObject, field);
            }
            schemasObject.put(schema.getName(), schemaObject);
        }
        rootObject.put("schemas", schemasObject);
        return rootObject.toString(2);
    }

    protected static String getFiltredScalarType(String type) {
        if (supportedScalarTypes.contains(type)) {
            return type;
        }
        if ("long".equalsIgnoreCase(type)) {
            return "integer";
        }
        return "string";
    }

    protected static void addField(JSONObject object, Field field)
            throws JSONException {

        if (!field.getType().isComplexType()) {
            if (field.getType().isListType()) {
                ListType lt = (ListType) field.getType();
                if (lt.getFieldType().isComplexType()) {
                    if (lt.getFieldType().getName().equals("content")) {
                        object.put(field.getName().getLocalName(), "blob[]");
                    } else {
                        JSONObject cplx = new JSONObject();
                        cplx.put("type", "complex[]");
                        JSONObject fields = buildComplexFields(lt.getField());
                        cplx.put("fields", fields);
                        object.put(field.getName().getLocalName(), cplx);
                    }
                } else {
                    object.put(field.getName().getLocalName(),
                            getFiltredScalarType(lt.getFieldType().getName())
                                    + "[]");
                }
            } else {
                object.put(field.getName().getLocalName(),
                        getFiltredScalarType(field.getType().getName()));
            }
        } else {
            if (field.getType().getName().equals("content")) {
                object.put(field.getName().getLocalName(), "blob");
            } else {
                JSONObject cplx = new JSONObject();
                cplx.put("type", "complex");
                JSONObject fields = buildComplexFields(field);
                cplx.put("fields", fields);
                object.put(field.getName().getLocalName(), cplx);
            }
        }
    }

    protected static JSONObject buildComplexFields(Field field)
            throws JSONException {
        JSONObject fields = new JSONObject();
        ComplexType cplXType = (ComplexType) field.getType();
        for (Field subField : cplXType.getFields()) {
            addField(fields, subField);
        }
        return fields;
    }
}
