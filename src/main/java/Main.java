
import metadata.Author;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by charbonn on 02.11.2016.
 */
public class Main {

	//TODO: Entfernen von Whitespace, aber belassen von echten Absätzen etc
	//TODO: Lösung für Formeln
//	static final String location="c://Hindawi";
	static final String location="C://Users//SohmenL//temp//Hindawi//Hindawi.AA";
    static int i=0;
    static final String outputEncoding = "UTF-8";
    //public static List<Result> resultTorso = new ArrayList<>();
    public static List<ResultSetJournal> resultTorso = new ArrayList<>();
	public static HashMap figureContext = new HashMap();
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
//        for(ResultSetJournal rsj : resultTorso)
//		{
//			MongoDBRepo.getInstance().writeJournal(rsj);
//
//			System.out.println("Wrote: "+rsj.getXMLPathYear());
//		}

        //Paralelisieren nicht ohne weiteres möglich?!
//        resultTorso.parallelStream().forEach(rsj -> {
//			MongoDBRepo.getInstance().writeJournal(rsj);
//			System.out.println("Wrote: "+rsj.getXMLPathYear());
////			for(Result res : rsj.getResultList())
////			{
////				try {
////					res.save2dbWithImage();
////				} catch (IOException e) {
////					e.printStackTrace();
////				}
////			}
//		});
//		for(ResultSetJournal r : resultTorso)
//		{
//			for(Result res : r.getResultList())
//			{
////				res.save2dbWithOutImage();
//				res.save2dbWithImage();
//			}
//			//System.out.println(r.getJournalDOI()+ " --- was written to Mongo \\o/");
//		}
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
		rsj.setXMLPathYear(xmlSource.substring(11,15));
		rsj.setXMLPathComplete((xmlSource));
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
		//docBuilderFactory.setValidating(true);
		//docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
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
		{
			getAllTokens(rsj, document.getDocumentElement());
			doSomething(rsj, document.getDocumentElement());
			context(rsj);
			MongoDBRepo.getInstance().writeJournal(rsj);
			System.out.println("Wrote: "+rsj.getXMLPathComplete());

		}
//		++i;
//		if(i%1000==0)
//        System.out.println("\n\n      #"+(i)+" DONE  -  XML Parse DONE   \n\n");

    }

    private static void getAllTokens( ResultSetJournal rsj,Node root)
	{
		//System.out.println(root.getTextContent());
		getAbstract(rsj, root);
		getBody(rsj, root);
	}
	private static void getAbstract(ResultSetJournal rsj, Node root)
	{
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			try {
				if (currentNode.getNodeName().equals("abstract"))
				{
					try
					{
						//System.out.println(currentNode.getTextContent());
						rsj.setAbstract(currentNode.getTextContent().trim());
						return;
					}
					catch (Exception e)
					{
						System.out.println("Error getAbstract:" +e);
					}
				}
			}
			catch(NullPointerException e)
			{

			}
			if(currentNode.getNodeType() == Node.ELEMENT_NODE)
				getAbstract(rsj, currentNode);
		}
	}
	private static void getAllSections(ResultSetJournal rsj, Node root)
	{
		{
			NodeList nodeList = root.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				try {
					if (currentNode.getNodeName().equals("sec"))
					{
						try
						{
							//System.out.println("---- Section "+i+" -----");
							//System.out.println(currentNode.getTextContent());
							rsj.getSections().add(getContentNoWhiteSpace(currentNode));
						}
						catch (Exception e)
						{
							System.out.println("Error getAllSections:" +e);
						}
					}
				}
				catch(NullPointerException e)
				{
					System.out.println("Error getAllSections:" +e);

				}
			}
		}
	}
	private static void getBody(ResultSetJournal rsj ,Node root)
	{
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			try {
				if (currentNode.getNodeName().equals("body"))
				{
					try
					{
						getAllSections(rsj, currentNode);
						rsj.setBody(getContentNoWhiteSpace(currentNode));
					}
					catch (Exception e)
					{
						System.out.println("Error getBody:" +e);
					}
				}
			}
			catch(NullPointerException e)
			{
				System.out.println("Error getBody:" +e);
			}
		}
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
							rsj.setJournalDOI(currentNode.getTextContent().trim());
						} catch (Exception e) {
							rsj.setJournalDOI("NO DOI FOUND");
						}
					}
					if (currentNode.getAttributes().item(0).getNodeValue().equals("publisher-id")) {
						rsj.setIssue("Article ID " + currentNode.getTextContent());
					}
				}

				catch(NullPointerException e)
				{
//
				}
            }
            if(currentNode.getNodeName().equals("article-title"))
			{
				try
				{
					rsj.setTitle(currentNode.getTextContent());

				}
				catch (Exception e) {
				rsj.setTitle("NO TAG 'article-title' FOUND");
				}
			}
			if(currentNode.getNodeName().equals("license"))
			{
				try
				{
					String license = currentNode.getTextContent();
					try
					{
						license = currentNode.getAttributes().getNamedItem("xlink:href").getTextContent() +" "+license;
					}
					catch (Exception e)
					{
					}
					rsj.setLicense(license);

				}
				catch (Exception e) {
					rsj.setLicense("NO TAG 'license' FOUND");
				}
			}

			if(currentNode.getNodeName().equals("volume"))
			{
				try
				{
					if(currentNode.getParentNode().getNodeName().equals("article-meta"))
					rsj.setVolume(currentNode.getTextContent());

				}
				catch (Exception e) {
					rsj.setVolume("NO TAG 'volume' FOUND");
				}
			}
			if(currentNode.getNodeName().equals("publisher-name"))
			{
				try
				{
					if(currentNode.getParentNode().getParentNode().getNodeName().equals("journal-meta"))
						rsj.setPublisher(currentNode.getTextContent());

				}
				catch (Exception e) {
					rsj.setPublisher("NO TAG 'publisher' FOUND");
				}
			}
			if(currentNode.getNodeName().equals("page-count"))
			{
				try
				{
						rsj.setPages(currentNode.getAttributes().item(0).getTextContent());

				}
				catch (Exception e) {
					rsj.setPages("NO TAG 'pages' FOUND");
				}
			}

            if(currentNode.getNodeName().equals("contrib"))
			{
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("author")) {

						try {
							for(int j=0; j<currentNode.getChildNodes().getLength();j++)
							{

								Author a= new Author();
								if(currentNode.getChildNodes().item(j).getNodeName().equals("name"))
								{
									for (int h=0; h<currentNode.getChildNodes().item(j).getChildNodes().getLength(); h++) {
										//System.out.println("try" + currentNode.getChildNodes().item(j).getTextContent());
										if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("surname"))
											a.setLastName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());


										if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("given-names"))
											a.setFirstName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}

								}
								if(a.getFirstName()!=null&&a.getLastName()!=null)
								rsj.getAuthors().add(a);

							}
							//currentNode.getChildNodes().item(0).getChildNodes().item(1).getTextContent()
//							rsj.getAuthors().add(currentNode.getTextContent());
						} catch (Exception e) {
							rsj.setJournalName("NO AUTHOR FOUND");
						}

					}
					if (currentNode.getAttributes().item(0).getNodeValue().equals("Academic Editor")) {
						try {
							for(int j=0; j<currentNode.getChildNodes().getLength();j++)
							{
								Author a= new Author();
								if(currentNode.getChildNodes().item(j).getNodeName().equals("name"))
								{
									if(currentNode.getChildNodes().item(j).getChildNodes().item(0).getNodeName().equals("surname"))
										a.setLastName(currentNode.getChildNodes().item(j).getChildNodes().item(0).getTextContent());
									if(currentNode.getChildNodes().item(j).getChildNodes().item(1).getNodeName().equals("given-names"))
										a.setFirstName(currentNode.getChildNodes().item(j).getChildNodes().item(1).getTextContent());

								}
								if(a.getFirstName()!=null&&a.getLastName()!=null)
								rsj.getEditor().add(a);
							}
//							rsj.getEditor().add(currentNode.getTextContent());
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

							rsj.setPublicationYear(currentNode.getTextContent().trim());
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
			if(currentNode.getNodeName().equals("xref"))
			{
				try
				{
					if (currentNode.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig"))
					{
						String key = currentNode.getAttributes().getNamedItem("rid").getTextContent();
						String value = getContentNoWhiteSpace(currentNode.getParentNode());
						figureContext.put(key, value);
					}

				}
				catch (Exception e) {
					System.out.println("No Context found");
				}
			}
            if(currentNode.getNodeName().equals("fig"))
            {

                Result r = new Result();
                r.setDOI(DOI);
                NodeList fig = currentNode.getChildNodes();
                for (int j = 0; j < fig.getLength(); j++) {
                    Node figNode = fig.item(j);
					//System.out.println(currentNode.getAttributes().item(0).getTextContent());
                    switch(figNode.getNodeName())
                    {

//                        case "object-id":
//                        r.setDOI(figNode.getTextContent());
//                            break;
                        case "label":
                            r.setLabel(getContentNoWhiteSpace(figNode));
                            //System.out.println(r.getLabel());
                            break;
                        case "caption":
                            //TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
                            try
							{
								r.setCaptionBody(r.getLabel()+ " "+getContentNoWhiteSpace(fig.item(j)));
								//System.out.println(r.getCaptionBody());
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
                r.setFigID(currentNode.getAttributes().getNamedItem("id").getTextContent());

                Node iter=currentNode.getParentNode();
                if (iter.getNodeName().equals("fig-group"))
                	r.setFigGroupID(iter.getAttributes().getNamedItem("id").getTextContent());
				if(r.getCaptionBody()==null&&iter!=null) {
					NodeList figgroup = iter.getChildNodes();
					for (int j = 0; j < figgroup.getLength(); j++) {
						Node figNode = figgroup.item(j);
						//System.out.println(currentNode.getAttributes().item(0).getTextContent());
						switch (figNode.getNodeName()) {

//                        case "object-id":
//                        r.setDOI(figNode.getTextContent());
//                            break;
							case "label":
								r.setLabel(getContentNoWhiteSpace(figNode));
								//System.out.println(r.getLabel());
								break;
							case "caption":
								//TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
								try {
									r.setCaptionBody(r.getLabel() + " " + getContentNoWhiteSpace(figgroup.item(j)));
									System.out.println(r.getCaptionBody());
								} catch (Exception e) {
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

	private static String getContentNoWhiteSpace(Node node)
	{
    	String content = "";
    	if (node.getNodeName().equals("inline-formula"))
		{
			content+=getFormula(node);
		}
		else if (node.hasChildNodes())
		{
			NodeList childNodes = node.getChildNodes();
			for (int g= 0; g<childNodes.getLength(); g++)
			{
				Node child= childNodes.item(g);
				//if (child.getNodeType()==Node.ELEMENT_NODE)
					//System.out.println(child.getNodeName());
				content +=getContentNoWhiteSpace(child);
			}
		}
		else if (node.getNodeType()==Node.TEXT_NODE)
		{
			//System.out.println(node.getNodeValue());
			if (!node.getNodeValue().startsWith("\n")&&!node.getNodeValue().startsWith("\t"))
			{
				content += node.getNodeValue();
			}
		}
		//System.out.println(node.getNodeName() +content);
		return content;

//
//			NodeList captionContent =figNode.getChildNodes();
//		for (int h=0; h<captionContent.getLength(); h++)
//		{
//			Node content = captionContent.item(h);
//			if (content.getNodeType()== Node.ELEMENT_NODE)
//			{
//
//				r.setCaptionBody(content.getTextContent());
//			}
//		}
//		NodeList formula = figNode.getChildNodes().item(1).getChildNodes();
//		for (int k = 0; k<formula.getLength(); k++)
//		{
//			Node formulaPart = formula.item(k);
//			String caption;
//			if (content.getNodeName().equals("inline-formula"))
//			{
//				caption+=getFormula(content);
//			}
//			System.out.println(formulaPart.getNodeType() + " " + formulaPart.getNodeName());
//		}

	}


	private static String getFormula(Node content) {
    	String text = "";
    	NodeList formula = content.getChildNodes();
    	for(i=0; i<formula.getLength(); i++){
    		text+=getContentNoWhiteSpace(formula.item(i));
		}
    	return text;

	}

	private static void context(ResultSetJournal rsj)
	{
		for(int i = 0; i<rsj.resultList.size(); i++)
		{
			if (figureContext.containsKey(rsj.resultList.get(i).figID))
				rsj.resultList.get(i).setContext((String) figureContext.get(rsj.resultList.get(i).figID));
			else
				rsj.resultList.get(i).setContext((String) figureContext.get(rsj.resultList.get(i).figGroupID));

		}
	}
    public void resolveDOI()
    {

    }

    public void save2db()
    {

    }
}
