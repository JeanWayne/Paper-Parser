import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

/**
 * Created by SohmenL on 01.08.2017.
 */
public class XSLTConvert {
    public static XsltExecutable makeExecutable() throws XPathException, SaxonApiException {
        Processor processor = new Processor(false);
        XsltCompiler compiler= processor.newXsltCompiler();
        Source mml2tex = new StreamSource(new File("mml2tex-master/mml2tex-master/xsl/invoke-mml2tex.xsl"));

        XsltExecutable executable  = compiler.compile(mml2tex);


//        XPathException e = new Exception()
//            System.out.println("isglobalerror "+e.isGlobalError());
//            System.out.println("geterrorlocalpart "+e.getErrorCodeLocalPart());
//            System.out.println("getErrorCodeNamespace "+e.getErrorCodeNamespace());
//            System.out.println("getErrorCodeQname "+e.getErrorCodeQName());
//            System.out.println("getErrorObject "+e.getErrorObject());
//            System.out.println("getLocator "+e.getLocator());
//            System.out.println("isglobalerror "+e.isGlobalError());

        return executable;
    }
}
