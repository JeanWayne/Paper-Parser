
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

//	static final String location="c://Hindawi";
	static final String location="c://Hindawi_work";
    static int i=0;
    static final String outputEncoding = "UTF-8";
    //public static List<Result> resultTorso = new ArrayList<>();
    public static List<ResultSetJournal> resultTorso = new ArrayList<>();

    public static void main(String[] args) throws IOException
    {
        System.out.println("Start PaperParser");
 //       MongoDBRepo db = new MongoDBRepo();

        try(Stream<Path> paths = Files.walk(Paths.get(location))) {
            paths.forEach(yearPath -> {
                if (Files.isRegularFile(yearPath))
                {
                    //System.out.println(yearPath);
                    try {
                        parseXML(yearPath.toAbsolutePath().toString());
//						for(ResultSetJournal r : resultTorso)
//						{
//							for(Result res : r.getResultList())
//							{
//								res.save2dbWithOutImage();
//							}
//							System.out.println(r.getJournalDOI()+ " --- was written to Mongo \\o/");
//
//						}
//						resultTorso.clear();
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
		for(ResultSetJournal r : resultTorso)
		{
			for(Result res : r.getResultList())
			{
				res.save2dbWithOutImage();
			}
			//System.out.println(r.getJournalDOI()+ " --- was written to Mongo \\o/");
		}
        System.out.println("DONE ----  " + resultTorso.size() + " elements completed");

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
        ResultSetJournal rsj = new ResultSetJournal();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = null;
		try {
			 document = docBuilder.parse(new File(xmlSource));
		}
		catch(Exception e)
		{
			System.out.println("Could not been read: "+xmlSource+"\n" + e);
			rsj.setError(xmlSource +":   "+ e);
			document=null;
		}
		if(document!=null)
			doSomething(rsj,document.getDocumentElement());

		resultTorso.add(rsj);
//		++i;
//		if(i%1000==0)
//        System.out.println("\n\n      #"+(i)+" DONE  -  XML Parse DONE   \n\n");

    }

    private static void doSomething(ResultSetJournal rsj,Node node)
    {
        String DOI = null;

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if(currentNode.getNodeName().equals("article-id"))
            {
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("doi")) {
						try {
							rsj.setJournalDOI(currentNode.getTextContent());
						} catch (Exception e) {
							rsj.setJournalDOI("NO DOI FOUND");
						}
					}
				}
				catch(NullPointerException e)
				{

				}
            }
            if(currentNode.getNodeName().equals("contrib"))
			{
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("author")) {
						try {
							rsj.getAuthors().add(currentNode.getTextContent());
						} catch (Exception e) {
							rsj.setJournalName("NO AUTHOR FOUND");
						}
					}
					if (currentNode.getAttributes().item(0).getNodeValue().equals("Academic Editor")) {
						try {
							rsj.getEditor().add(currentNode.getTextContent());
						} catch (Exception e) {
							rsj.setJournalName("NO EDITOR FOUND");
						}
					}
				}
				catch(NullPointerException e)
				{

				}
			}
            if(currentNode.getNodeName().equals("journal-title"))
            {
				try{
                rsj.setJournalName(currentNode.getTextContent());
				}catch(Exception e)
				{
					rsj.setJournalName("NO NAME FOUND");
				}
            }
            if(currentNode.getNodeName().equals("pub-date"))
            {
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("publication-year")) {
						try {
							rsj.setPublicationYear(currentNode.getTextContent());
						} catch (Exception e) {
							rsj.setPublicationYear("NO YEAR FOUND");
						}
					}
				}
				catch(NullPointerException e)
				{

				}
            }
            if(currentNode.getNodeName().equals("journal-id"))
            {
				try{
                if(currentNode.getAttributes().item(0).getNodeValue().equals("publisher-id"))
                {
					try{
                    rsj.setPublisherId(currentNode.getTextContent());
					}catch(Exception e)
					{
						rsj.setPublisherId("NO ID FOUND");
					}
                }}
				catch(NullPointerException e)
				{

				}
            }
            if(currentNode.getNodeName().equals("fig"))
            {
                //System.out.println("FOUND");

                Result r = new Result();
                r.setDOI(DOI);
                NodeList fig = currentNode.getChildNodes();
                for (int j = 0; j < fig.getLength(); j++) {
                    Node figNode = fig.item(j);
                    switch(figNode.getNodeName())
                    {
//                        case "object-id":
//                        r.setDOI(figNode.getTextContent());
//                            break;
                        case "label":
                            r.setLabel(figNode.getTextContent());
                            break;
                        case "caption":
                            //TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
                            try
							{
								r.setCaptionBody(figNode.getChildNodes().item(0).getTextContent());
							}catch(Exception e)
							{
								r.setCaptionBody("NO BODY FOUND");
							}
                            //r.setCaptionTitle(figNode.getChildNodes().item(1).getTextContent());
                            //r.setCaptionBody(figNode.getChildNodes().item(3).getTextContent());
                            break;
                        case "graphic":
                            r.setGraphicDOI(figNode.getAttributes().getNamedItem("xlink:href").getNodeValue());
                            break;
                        default:
                            break;
                    }
                }
                r.setRsj(rsj);
				r.setFindingID(rsj.getResultList().size());
                rsj.getResultList().add(r);
            }
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                doSomething(rsj, currentNode);
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
