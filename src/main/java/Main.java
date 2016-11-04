
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by charbonn on 02.11.2016.
 */
public class Main {

    static final String outputEncoding = "UTF-8";
    public static List<Result> resultTorso = new ArrayList<>();


    public static void main(String[] args) throws IOException
    {
        System.out.println("Start PaperParser");
 //       MongoDBRepo db = new MongoDBRepo();

        try(Stream<Path> paths = Files.walk(Paths.get("c://paper"))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    //System.out.println(filePath);
                    try {
                        parseXML(filePath.toAbsolutePath().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    for(Result r : resultTorso)
    {
        r.save2db();
        System.out.println("DB Wrote \\o/");
    }
        System.out.println("==== DONE ====");

    }
    public static void parseXML(String xmlSource) throws IOException, ParserConfigurationException, SAXException
    {
//        SAXBuilder jdomBuilder = new SAXBuilder();
//        Document jdomDocument = jdomBuilder.build(xmlSource);
//        Element root = jdomDocument.getRootElement();
//        Element body = root.getChild("body");
//        List<Element> sections = body.getChildren();
//        for(Element e : sections)
//        {
//            List<Element> p = e.getChildren();
//
//        }
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        org.w3c.dom.Document document = docBuilder.parse(new File(xmlSource));
        doSomething(document.getDocumentElement());
        System.out.println("XML Prase DONE");

    }

    private static void doSomething(Node node)
    {
        // do something with the current node instead of System.out
        //System.out.println(node.getNodeName());

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if(currentNode.getNodeName().equals("fig"))
            {
                //System.out.println("FOUND");

                Result r = new Result();

                NodeList fig = currentNode.getChildNodes();
                for (int j = 0; j < fig.getLength(); j++) {
                    Node figNode = fig.item(j);
                    switch(figNode.getNodeName())
                    {
                        case "object-id":
                        r.setDOI(figNode.getTextContent());
                            break;
                        case "label":
                            r.setLabel(figNode.getTextContent());
                            break;
                        case "caption":
                            //TODO:  This might be an error source if a Caotion has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
                            r.setCaptionTitle(figNode.getChildNodes().item(1).getTextContent());
                            r.setCaptionBody(figNode.getChildNodes().item(3).getTextContent());
                            break;
                        case "graphic":
                            r.setGraphicDOI(figNode.getAttributes().getNamedItem("xlink:href").getNodeValue());
                            break;
                        default:
                            break;
                    }
                }
//                NamedNodeMap a = currentNode.getAttributes();
//                Node caption = a.getNamedItem("caption");
//                Node grafik = a.getNamedItem("graphic");
                resultTorso.add(r);
            }
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                doSomething(currentNode);
            }
        }
    }

    public void resolveDOI()
    {

    }

    public void save2db()
    {

    }
}
