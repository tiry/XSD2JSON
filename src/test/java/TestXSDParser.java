import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.nuxeo.xsd.parser.XSD2JSON;

public class TestXSDParser {

    @Test
    public void testSimpleParser() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("dublincore.xsd");
        File xsdFile = new File(url.toURI());
        assertNotNull(xsdFile);
        System.out.println(XSD2JSON.asJSON("dublincore", "dc", xsdFile, null,
                null, null));
    }

    @Test
    public void testComplexParser() throws Exception {
        URL url = this.getClass().getClassLoader().getResource(
                "complexschema.xsd");
        File xsdFile = new File(url.toURI());
        assertNotNull(xsdFile);
        System.out.println(XSD2JSON.asJSON("complex", "cplx", xsdFile, null,
                null, null));
    }

    @Test
    public void testWithBlobs() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("TestSchema.xsd");
        File xsdFile = new File(url.toURI());
        assertNotNull(xsdFile);
        System.out.println(XSD2JSON.asJSON("test", "tst", xsdFile, null, null,
                null));
    }
}
