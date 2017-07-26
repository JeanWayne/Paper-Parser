
import metadata.Author;
import org.w3c.dom.*;
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
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by charbonn on 02.11.2016.
 */
public class Main implements Text{

	//TODO: Entfernen von Whitespace, aber belassen von echten Absätzen etc
	//TODO: Lösung für Formeln
	//TODO: Automatisches Einfügen von Referenzen aus anderer Stelle im Text (hauptsächlich Copernicus)
//	static final String location="c://Hindawi";
	static final String location="C:\\Users\\SohmenL\\temp\\test";
    static int i=0;
    static final String outputEncoding = "UTF-8";
    //public static List<Result> resultTorso = new ArrayList<>();
    public static List<ResultSetJournal> resultTorso = new ArrayList<>();
	public static HashMap figureContext = new HashMap();
	public static HashMap references = new HashMap();
	public static HashMap laufendeNummer=new HashMap();
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
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = null;
		rsj.setFile(new File(xmlSource));
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
			mapReferences(rsj, document.getDocumentElement(), document.getDocumentElement());
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

	private static void mapReferences(ResultSetJournal rsj, Node node, Node root) {
		//System.out.println(node.getNodeName());
    	NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			//System.out.println(currentNode.getNodeName());
				if (currentNode.getNodeName().equals("xref")&&!currentNode.hasChildNodes()){
					String key= currentNode.getAttributes().getNamedItem("rid").getTextContent();
					String value = findReference(key, root);
					if (value==null){
						Matcher matcher = Pattern.compile("\\d+").matcher(key);
						String m="";
						while(matcher.find()){
							m=matcher.group();
						}
						value=m;
					}
					if (value!=null) {
						references.put(key, value);
					}
					//System.out.println("key: "+key +" " +"value: "+value);
			}
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				//calls this method for all the children which is Element
				mapReferences(rsj, currentNode, root);
			}
		}
	}

	private static String findReference(String key, Node node) {
		String value=null;
		//System.out.println(key);
    	NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);

			if (currentNode.getNodeName().equals("ref")
					&& currentNode.getAttributes().getNamedItem("id").getTextContent().equals(key)) {
				//System.out.println(currentNode.getNodeName());
				NodeList childList = currentNode.getChildNodes();
				for (int j = 0; j < childList.getLength(); j++) {
					Node childNode = childList.item(j);
					if (childNode.getNodeName().equals("label")) {
						value=childNode.getTextContent();
						//if(value!=null)System.out.println(value);
						return value;

					}
				}


			}
			else if (currentNode.getNodeType() == Node.ELEMENT_NODE) {

				//calls this method for all the children which is Element
				value=findReference(key, currentNode);
				if (value!=null){
					return value;
				}

			}
		}
		//System.out.println(value);
		return value;
	}
	private static boolean condition(Node currentNode, String key) {
    	System.out.println("Aufruf");
		Boolean condition1=false;
		Boolean condition2=false;
		Boolean condition3=false;
    	try {
			 condition1 = currentNode.getAttributes().getNamedItem("content-type").getTextContent().equals("numbered");
		}
		catch (Exception e) {
		}
		try {
			 condition2 = currentNode.getAttributes().getNamedItem("id").getTextContent().equals(key);
		}
		catch (Exception e) {
		}
		try {
			 condition3 = currentNode.getAttributes().getNamedItem("rid").getTextContent().equals(key);
		}
		catch (Exception e) {
		}
			Boolean condition=(condition1&&(condition2||condition3));
			//System.out.println("1: "+condition1+" 2: " +condition2+" 3: "+condition3+" insegesamt: "+condition);
			return condition;


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
				if (currentNode.getNodeName().matches("[aA]bstract"))
				{
					try
					{
						//System.out.println(currentNode.getTextContent());
						rsj.setAbstract(getContentNoWhiteSpace(currentNode));
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
		if (rsj.getAbstract()==null) {

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					getAbstract(rsj, currentNode);
				}
			}
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
				if (currentNode.getNodeName().matches("[bB]ody") )
				{
					System.out.println("Body vorhanden");
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
		if (rsj.getBody()==null) {
//			Node alternativeRoot=null;
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				Node currentNode = nodeList.item(i);
//				if(currentNode.getNodeName().equals("record")){
//					System.out.println(currentNode.getNodeName());
//					alternativeRoot=currentNode;
//			if(root.getChildNodes())
//			getBody(rsj, root.getChildNodes().item(1));

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					getBody(rsj, currentNode);
				}
			}
		}
	//	}
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
						if(rsj.getIssue()==null){
							rsj.setIssue("Article ID " + currentNode.getTextContent());
						}
					}
					if (currentNode.getAttributes().item(0).getNodeValue().equals("pmc")) {
						rsj.setPmcID(currentNode.getTextContent());

					}
				}

				catch(NullPointerException e)
				{
//
				}
            }
			if(currentNode.getNodeName().equals("ArticleDOI")){
				rsj.setJournalDOI(currentNode.getTextContent());
			}
            if (currentNode.getNodeName().matches("issue|meta:IssueId")){
            	if(currentNode.getParentNode().getNodeName().matches("article-meta|meta:Info")){
            		rsj.setIssue(currentNode.getTextContent());
				}
			}
            if(currentNode.getNodeName().equals("article-title")||currentNode.getNodeName().equals("ArticleTitle"))
			{
				try
				{
					if (currentNode.getParentNode().getNodeName().equals("title-group")||currentNode.getParentNode().getNodeName().equals("ArticleInfo")){
						rsj.setTitle(currentNode.getTextContent());
					}


				}
				catch (Exception e) {
				rsj.setTitle("NO TAG 'article-title' FOUND");
				}
			}
			if(currentNode.getNodeName().matches("[Ll]icense|CopyrightComment"))
			{
				try
				{
					String license = getContentNoWhiteSpace(currentNode);
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

			if(currentNode.getNodeName().matches("volume|meta:VolumeId"))
			{
				try
				{
					if(currentNode.getParentNode().getNodeName().matches("article-meta|meta:Info"))
					rsj.setVolume(currentNode.getTextContent());

				}
				catch (Exception e) {
					rsj.setVolume("NO TAG 'volume' FOUND");
				}
			}
			if (currentNode.getNodeName().matches("Keyword|kwd")){
            	rsj.getKeywords().add(getContentNoWhiteSpace(currentNode));
			}
			if(currentNode.getNodeName().matches("publisher-name|PublisherName"))
			{
				try
				{
					if(currentNode.getParentNode().getParentNode().getNodeName().equals("journal-meta")||currentNode.getParentNode().getNodeName().equals("PublisherInfo"))
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
			if (currentNode.getNodeName().matches("fpage|ArticleFirstPage")){
            	if (currentNode.getParentNode().getNodeName().matches("article-meta|ArticleInfo")){
            		String firstPage = currentNode.getTextContent();
            		String lastPage="";
					if (currentNode.getNextSibling().getNodeName().matches("lpage|ArticleLastPage")){
						lastPage=currentNode.getNextSibling().getTextContent();
					}
					else if (currentNode.getNextSibling().getNextSibling().getNodeName().matches("lpage|ArticleLastPage")){
						lastPage=currentNode.getNextSibling().getNextSibling().getTextContent();
					}
					rsj.setPages(firstPage+ "-"+lastPage);
				}
			}

            if(currentNode.getNodeName().equals("contrib"))
			{
				//System.out.println("try" + currentNode.getTextContent());
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("author")) {

						try {
							for(int j=0; j<currentNode.getChildNodes().getLength();j++)
							{
								//System.out.println("try" + currentNode.getChildNodes().item(j).getTextContent());
								Author a= new Author();
								if(currentNode.getChildNodes().item(j).getNodeName().equals("name"))
								{
									//System.out.println("try" + currentNode.getChildNodes().item(j).getTextContent());
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
			if(currentNode.getNodeName().equals("Author"))
			{
				//System.out.println("try" + currentNode.getTextContent());
				try {

					for(int j=0; j<currentNode.getChildNodes().getLength();j++)
					{
						//System.out.println("try" + currentNode.getChildNodes().item(j).getTextContent());
						Author a= new Author();
						if(currentNode.getChildNodes().item(j).getNodeName().equals("AuthorName"))
						{
							//System.out.println("try" + currentNode.getChildNodes().item(j).getTextContent());
							for (int h=0; h<currentNode.getChildNodes().item(j).getChildNodes().getLength(); h++) {
								//System.out.println("try" + currentNode.getChildNodes().item(j).getTextContent());
								if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("FamilyName"))
									a.setLastName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());


								if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("GivenName"))
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
            if(currentNode.getNodeName().equals("journal-title"))
            {
				try{
                rsj.setJournalName(currentNode.getTextContent());
				}catch(Exception e)
				{
					rsj.setJournalName("NO NAME FOUND");
				}
            }
			if(currentNode.getNodeName().equals("JournalTitle"))
			{
				if(currentNode.getParentNode().getNodeName().equals("JournalInfo")) {
					try {
						rsj.setJournalName(currentNode.getTextContent());
					} catch (Exception e) {
						rsj.setJournalName("NO NAME FOUND");
					}
				}
			}
            if(currentNode.getNodeName().equals("publication-year")|| currentNode.getNodeName().matches("[Yy]ear"))
            {
				try {
					if (currentNode.getParentNode().getNodeName().equals("pub-date") ||currentNode.getParentNode().getNodeName().equals("OnlineDate")) {
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
            if(currentNode.getNodeName().equals("journal-id")&&!rsj.getXMLPathComplete().contains("PMC"))
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
//            else if(currentNode.getNodeName().equals("article-id")&&rsj.getXMLPathComplete().contains("PMC")){
//				try{
//					if(currentNode.getAttributes().item(0).getNodeValue().equals("publisher-id"))
//					{
//
//						try{
//							rsj.setPublisherId(currentNode.getTextContent());
//							System.out.println(rsj.getPublisherId());
//						}catch(Exception e)
//						{
//							rsj.setPublisherId("NO ID FOUND");
//						}
//					}}
//				catch(NullPointerException e)
//				{
//
//				}
		//	}
			if(currentNode.getNodeName().matches("xref|InternalRef"))
			{

				try
				{
					//System.out.println(currentNode.getAttributes().getNamedItem("RefID").getTextContent());

					if (currentNode.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig"))
					{
						//System.out.println("da");
						String key = currentNode.getAttributes().getNamedItem("rid").getTextContent();
						String value = getContentNoWhiteSpace(currentNode.getParentNode());
						figureContext.put(key, value);
					}


				}
				catch (Exception e) {
					//System.out.println("No Context found"+e);
				}
				try {
					if (currentNode.getAttributes().getNamedItem("RefID").getTextContent().startsWith("Fig")) {
						//System.out.println("hier");
						String key = currentNode.getAttributes().getNamedItem("RefID").getTextContent();
						String value = getContentNoWhiteSpace(currentNode.getParentNode());
						figureContext.put(key, value);
					}
				}
				catch(Exception e){

					}

			}
			if(currentNode.getNodeName().matches("fig|Figure"))
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
						case "Caption":
							System.out.println("CaptionNumber");
							r.setCaptionBody(getContentNoWhiteSpace(figNode));
							break;
                        case "label":
                            r.setLabel(getContentNoWhiteSpace(figNode));
                            //System.out.println(r.getLabel());
                            break;
//						case "CaptionContent":
//							System.out.println("CaptionContent");
//							try
//							{
//								r.setCaptionBody(r.getLabel()+ " "+getContentNoWhiteSpace(fig.item(j)));
//								//System.out.println(r.getCaptionBody());
//							}catch(Exception e)
//							{
//								r.setCaptionBody("NO BODY FOUND");
//							}
//							//r.setCaptionTitle(figNode.getChildNodes().item(1).getTextContent());
//							//r.setCaptionBody(figNode.getChildNodes().item(3).getTextContent());
//							break;
                        case "caption":
                            //TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
                            try
							{
								if(r.getLabel()!=null) {
									r.setCaptionBody(r.getLabel() + " " + getContentNoWhiteSpace(fig.item(j)));
								}
								else{
									r.setCaptionBody(getContentNoWhiteSpace(fig.item(j)));
								}
								//System.out.println(r.getCaptionBody());
							}catch(Exception e)
							{
								r.setCaptionBody("NO BODY FOUND");
							}
                            //r.setCaptionTitle(figNode.getChildNodes().item(1).getTextContent());
                            //r.setCaptionBody(figNode.getChildNodes().item(3).getTextContent());
                            break;
						case "MediaObject":
							r.setGraphicDOI(figNode.getChildNodes().item(1).getAttributes().getNamedItem("FileRef").getNodeValue());
							break;
                        case "graphic":
                            r.setGraphicDOI(figNode.getAttributes().getNamedItem("xlink:href").getNodeValue());
                            break;
                        default:
                            break;
                    }
                }
                try {
					r.setFigID(currentNode.getAttributes().getNamedItem("id").getTextContent());
				}
				catch (Exception E){
				}
				try {
					r.setFigID(currentNode.getAttributes().getNamedItem("ID").getTextContent());
				}
				catch (Exception E){
				}


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
		//if (Text.isElementContentWhitespace)
    	String content = "";

		//node.getIs

    	if (node.getNodeName().equals("inline-formula"))
		{
			content+=getFormula(node);
		}

		else if(node.getNodeName().matches("Heading|title")) {
			content += (node.getTextContent() + "\n");
			//System.out.println("*");

		}
		else if(node.getNodeName().matches("Para|p")){
			NodeList childNodes = node.getChildNodes();
			for (int g= 0; g<childNodes.getLength(); g++){
				Node child= childNodes.item(g);
				content +=getContentNoWhiteSpace(child);
			}
			content+="\n";
		}

		else if (node.hasChildNodes())
		{
			//System.out.println(node.getNodeName());
			NodeList childNodes = node.getChildNodes();
			for (int g= 0; g<childNodes.getLength(); g++)
			{
				Node child= childNodes.item(g);
				//if (child.getNodeType()==Node.ELEMENT_NODE)
					//System.out.println(child.getNodeName());
				content +=getContentNoWhiteSpace(child);
			}
		}

		else if (node.getNodeName().equals("xref")){
			//&&node.getAttributes().getNamedItem("ref-type").getNodeValue().equals("bibr")){
			//||node.getTextContent()==null){

			content+=(" "+references.get(node.getAttributes().getNamedItem("rid").getTextContent())+" ");
			System.out.println(references.get(node.getAttributes().getNamedItem("rid").getTextContent()));
		}

		else if (node.getNodeType()==Node.TEXT_NODE)
		{
			//System.out.println(node.getNodeValue());
			//if (!node.getNodeValue().startsWith("\n")&&!node.getNodeValue().startsWith("\t"))
			//{
				content += node.getNodeValue().trim().replaceAll("\n", " ").replaceAll("\t", " ");
			//}
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

	@Override
	public Text splitText(int i) throws DOMException {
		return null;
	}

	@Override
	public boolean isElementContentWhitespace() {
		return false;
	}

	@Override
	public String getWholeText() {
		return null;
	}

	@Override
	public Text replaceWholeText(String s) throws DOMException {
		return null;
	}

	@Override
	public String getData() throws DOMException {
		return null;
	}

	@Override
	public void setData(String s) throws DOMException {

	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public String substringData(int i, int i1) throws DOMException {
		return null;
	}

	@Override
	public void appendData(String s) throws DOMException {

	}

	@Override
	public void insertData(int i, String s) throws DOMException {

	}

	@Override
	public void deleteData(int i, int i1) throws DOMException {

	}

	@Override
	public void replaceData(int i, int i1, String s) throws DOMException {

	}

	@Override
	public String getNodeName() {
		return null;
	}

	@Override
	public String getNodeValue() throws DOMException {
		return null;
	}

	@Override
	public void setNodeValue(String s) throws DOMException {

	}

	@Override
	public short getNodeType() {
		return 0;
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public NodeList getChildNodes() {
		return null;
	}

	@Override
	public Node getFirstChild() {
		return null;
	}

	@Override
	public Node getLastChild() {
		return null;
	}

	@Override
	public Node getPreviousSibling() {
		return null;
	}

	@Override
	public Node getNextSibling() {
		return null;
	}

	@Override
	public NamedNodeMap getAttributes() {
		return null;
	}

	@Override
	public Document getOwnerDocument() {
		return null;
	}

	@Override
	public Node insertBefore(Node node, Node node1) throws DOMException {
		return null;
	}

	@Override
	public Node replaceChild(Node node, Node node1) throws DOMException {
		return null;
	}

	@Override
	public Node removeChild(Node node) throws DOMException {
		return null;
	}

	@Override
	public Node appendChild(Node node) throws DOMException {
		return null;
	}

	@Override
	public boolean hasChildNodes() {
		return false;
	}

	@Override
	public Node cloneNode(boolean b) {
		return null;
	}

	@Override
	public void normalize() {

	}

	@Override
	public boolean isSupported(String s, String s1) {
		return false;
	}

	@Override
	public String getNamespaceURI() {
		return null;
	}

	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public void setPrefix(String s) throws DOMException {

	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}

	@Override
	public String getBaseURI() {
		return null;
	}

	@Override
	public short compareDocumentPosition(Node node) throws DOMException {
		return 0;
	}

	@Override
	public String getTextContent() throws DOMException {
		return null;
	}

	@Override
	public void setTextContent(String s) throws DOMException {

	}

	@Override
	public boolean isSameNode(Node node) {
		return false;
	}

	@Override
	public String lookupPrefix(String s) {
		return null;
	}

	@Override
	public boolean isDefaultNamespace(String s) {
		return false;
	}

	@Override
	public String lookupNamespaceURI(String s) {
		return null;
	}

	@Override
	public boolean isEqualNode(Node node) {
		return false;
	}

	@Override
	public Object getFeature(String s, String s1) {
		return null;
	}

	@Override
	public Object setUserData(String s, Object o, UserDataHandler userDataHandler) {
		return null;
	}

	@Override
	public Object getUserData(String s) {
		return null;
	}
}
