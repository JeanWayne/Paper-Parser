/**
 * Created by SohmenL on 27.07.2017.
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.saxon.event.Receiver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

// For write operation
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import javax.xml.parsers.*;
import net.sf.saxon.*;
import net.sf.saxon.s9api.*;


public class formulaConverter {

    public HashMap mapping= new HashMap();

    public static void convert(String formula) {

    }
    public static void createMapping(String formula) throws IOException, SaxonApiException {
        Processor processor = new Processor(false);
        XsltCompiler compiler= processor.newXsltCompiler();
        Source mml2tex = new Source() {
            @Override
            public void setSystemId(String s) {

            }

            @Override
            public String getSystemId() {
                return null;
            }
        };
        mml2tex.setSystemId("mml2tex-master/mml2tex-master/xsl/invoke-mml2tex.xsl");
        Source input = new StreamSource(new StringReader(formula));
        StringReader reader = new StringReader(formula);
        XsltExecutable executable = compiler.compile(mml2tex);

        XsltTransformer transformer = executable.load();
        Destination destination = new Destination() {
            @Override
            public Receiver getReceiver(Configuration configuration) throws SaxonApiException {
                return null;
            }

            @Override
            public void close() throws SaxonApiException {

            }
        };
        transformer.setDestination(destination);
        transformer.setSource(input);
        transformer.transform();
        transformer.close();
        String line;
        String key;
        String value;

            }




}