
import metadata.Author;
import metadata.Citation;
import metadata.ID;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Created by charbonn on 02.11.2016.
 */
public class Main implements Text{

	//TODO: Entfernen von Whitespace, aber belassen von echten Absätzen etc
	//TODO: Lösung für Formeln
	//TODO: Automatisches Einfügen von Referenzen aus anderer Stelle im Text (hauptsächlich Copernicus)
//	static final String location="c://Hindawi";
	static final String location="C:\\Users\\SohmenL\\Downloads\\testdocs";
    static int i=0;
    static final String outputEncoding = "UTF-8";
    static final boolean VERBOSE=true;
	static final String mongoIP="141.71.5.19";
	static final int mongoPort=27017;
	static final String mongoDataBase="beta";
	static final boolean withDownload=false; //Download Images as binary data?
	static final int outPutFormat=2; // 0=path, 1= current/overall, 2=percentage.


	/*
	* DATA HANDLING
	*/
	public static List<ResultSetJournal> resultTorso = new ArrayList<>();
	public static HashMap figureContext = new HashMap();
	public static HashMap references = new HashMap();
	public static int withFormula=0;
	public static int articles =0;
	//public static int aufrufszahl =0;
	public static long numberOfDocs=-1;
	public static long currentDoc=0;
	private static long startTime=0;

	public static void main(String[] args) throws IOException

	{
		generateNoaLabel();



		System.out.println("Start PaperParser");
        if(VERBOSE)
		{	startTime=System.currentTimeMillis();
			numberOfDocs= Files.walk(Paths.get(location))
					.parallel()
					.filter(p -> !p.toFile().isDirectory())
					.count();
			System.out.println("Parsing Papers from: "+location);
			System.out.println("Saving it in MongoDB : "+mongoIP+":"+mongoPort +"   dbName:"+mongoDataBase);
			System.out.println("Number of Documents overall: "+numberOfDocs);
		}
 //       MongoDBRepo db = new MongoDBRepo();

		try(Stream<Path> paths = Files.walk(Paths.get(location))) {
            paths.forEach(yearPath -> {
                if (Files.isRegularFile(yearPath))
                {
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
                    } catch (XMLStreamException e) {
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
		System.out.println("Artikel mit Bilden die Formeln in der Unterschrift haben: "+withFormula+" von "+articles);
        System.out.println("DONE ----  " + resultTorso.size() + " elements completed");

    }

	public static int countFilesInDir(File folder, int count) {
		File[] files = folder.listFiles();
		for (File file: files) {
			if (file.isFile()) {
				count++;
			} else {
				countFilesInDir(file, count);
			}
		}

		return count;
	}
	private static void generateNoaLabel()
	{
		System.out.println("888b    888  .d88888b.         d8888 ");
		System.out.println("8888b   888 d88P\" \"Y88b       d88888 ");
		System.out.println("88888b  888 888     888      d88P888 ");
		System.out.println("888Y88b 888 888     888     d88P 888 ");
		System.out.println("888 Y88b888 888     888    d88P  888 ");
		System.out.println("888  Y88888 888     888   d88P   888 ");
		System.out.println("888   Y8888 Y88b. .d88P  d8888888888 ");
		System.out.println("888    Y888  \"Y88888P\"  d88P     888 ");
		System.out.println();
		System.out.println("8888888b.                                     8888888b.                   ");
		System.out.println("888   Y88b                                    888   Y88b                  ");
		System.out.println("888    888                                    888    888                  ");
		System.out.println("888   d88P  8888b.  88888b.   .d88b.  888d888 888   d88P  8888b.  888d888 .d8888b   .d88b.  888d888 ");
		System.out.println("8888888P\"      \"88b 888 \"88b d8P  Y8b 888P\"   8888888P\"      \"88b 888P\"   88K      d8P  Y8b 888P\"   ");
		System.out.println("888        .d888888 888  888 88888888 888     888        .d888888 888     \"Y8888b. 88888888 888     ");
		System.out.println("888        888  888 888 d88P Y8b.     888     888        888  888 888          X88 Y8b.     888     ");
		System.out.println("888        \"Y888888 88888P\"   \"Y8888  888     888        \"Y888888 888      88888P'  \"Y8888  888     ");
		System.out.println("                    888                                                   ");
		System.out.println("                    888                                                   ");
		System.out.println("                    888                                                   ");


	}

//	private static XsltExecutable compileXSLT() throws SaxonApiException {
//		Processor processor = new Processor(false);
//		XsltCompiler compiler= processor.newXsltCompiler();
//		Source mml2tex = new Source() {
//			@Override
//			public void setSystemId(String s) {
//
//			}
//
//			@Override
//			public String getSystemId() {
//				return null;
//			}
//		};
//		mml2tex.setSystemId("mml2tex-master/mml2tex-master/xsl/invoke-mml2tex.xsl");
//		XsltExecutable executable = compiler.compile(mml2tex);
//		return executable;
//	}

	public static void parseXML(String xmlSource) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		long starting = System.currentTimeMillis();

        ResultSetJournal rsj = new ResultSetJournal();
		Boolean zip = false;
		InputStream stream = null;
        if(xmlSource.endsWith("zip")){
			Charset charset = ISO_8859_1;
			ZipFile zipFile = new ZipFile(xmlSource, charset);
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();){
				ZipEntry entry = e.nextElement();
				if(entry.getName().endsWith("zip")){
					continue;
				}
				if(entry.getName().endsWith("xml")){
					xmlSource=entry.getName();
					 stream = zipFile.getInputStream(entry);
					zip = true;
				}
			}
		}

		rsj.setXMLPathYear(xmlSource.substring(11,15));
		rsj.setXMLPathComplete((xmlSource));

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
		//docBuilderFactory.setValidating(true);
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = null;


		rsj.setFile(new File(xmlSource));

		if (zip){
			document = docBuilder.parse(stream);
		}else {
			try {
				document = docBuilder.parse(new File(xmlSource));
			} catch (Exception e) {
				MongoDBRepo.getInstance(mongoIP,mongoPort,mongoDataBase).writeError(xmlSource,e.toString(), "modus");
				System.out.println("Could not been read: " + xmlSource + "\n" + e);
				rsj.setError(xmlSource + ":   " + e);
				document = null;
			}
		}

		if(document!=null)
		{
			//long start = System.currentTimeMillis();
			mapReferences(rsj, document.getDocumentElement(), document.getDocumentElement());
			//System.out.print("mapreferences"); System.out.println( System.currentTimeMillis() - start);
			//start = System.currentTimeMillis();
			getAllTokens(rsj, document.getDocumentElement());
			doSomething(rsj, document.getDocumentElement());
			//System.out.print("doSomething");System.out.println(System.currentTimeMillis() - start);
			//start = System.currentTimeMillis();
			articles+=1;
			if(rsj.hasFormula)withFormula+=1;
			// (rsj, article, article);
			//getAllTokens(rsj, article);
			//doSomething(rsj, article);
			context(rsj);
			//System.out.print("context");System.out.println(System.currentTimeMillis() - start);
			//start = System.currentTimeMillis();
			//MongoDBRepo.getInstance(mongoIP,mongoPort,mongoDataBase).writeJournal(rsj,withDownload);
			//System.out.print("mondowrite");System.out.println(System.currentTimeMillis() - start);
			references.clear();
			figureContext.clear();
			System.out.println("Wrote: " + rsj.getXMLPathComplete());
			if(VERBOSE) {
				switch(outPutFormat){
					case 0:
						System.out.println("Wrote: " + rsj.getXMLPathComplete());
						break;
					case 1:
						System.out.printf("%-10d / %-10d\n",++currentDoc,numberOfDocs);
						break;
					case 2:
						System.out.println();
						printProgress(startTime,numberOfDocs,++currentDoc);
						break;
				}
			}
		}
//		++i;
//		if(i%1000==0)
//        System.out.println("\n\n      #"+(i)+" DONE  -  XML Parse DONE   \n\n");
		//System.out.print("It took ");System.out.println(System.currentTimeMillis() - starting);

    }

	private static void mapReferences(ResultSetJournal rsj, Node node, Node root) {
    	NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);

				if (currentNode.getNodeName().equals("xref")&&!currentNode.hasChildNodes()){
					try {
						String key= currentNode.getAttributes().getNamedItem("rid").getTextContent();
						//System.out.println(key);
						String value = findReference(key, root);
						if (value==null){
                            Matcher matcher = Pattern.compile("\\d+").matcher(key);
                            String m="";
                            while(matcher.find()){
                                m=matcher.group();
                            }
                            value=m;
                            //System.out.println(m);
                        }
						if (value!=null) {
                            references.put(key, value);
                        }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				//calls this method for all the children which is Element
				mapReferences(rsj, currentNode, root);
			}
		}
	}

	private static String findReference(String key, Node node) {
		String value=null;
    	NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);

			if (currentNode.getNodeName().equals("ref")
					&& currentNode.getAttributes().getNamedItem("id").getTextContent().equals(key)) {
				NodeList childList = currentNode.getChildNodes();
				for (int j = 0; j < childList.getLength(); j++) {
					Node childNode = childList.item(j);
					if (childNode.getNodeName().equals("label")) {
						value=childNode.getTextContent();
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
		return value;
	}
	private static boolean condition(Node currentNode, String key) {
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
			return condition;


	}

	private static void getAllTokens( ResultSetJournal rsj,Node root)
	{
		getAbstract(rsj, root);
		getBody(rsj, root);
	}
	private static void getAbstract(ResultSetJournal rsj, Node root)
	{
		if((root.getNodeType()==Node.TEXT_NODE)) {
			rsj.setAbstract("No abstract found");
			return;
		}
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			try {
				if (currentNode.getNodeName().matches("[aA]bstract"))
				{
					try
					{
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
			if(currentNode.getNodeType() == Node.ELEMENT_NODE){
				if(currentNode.hasChildNodes()){
					getAbstract(rsj, currentNode);
				}

			}

		}
		if (rsj.getAbstract()==null) {

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);

				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {

					if (currentNode.getNodeName().matches("[Bb]ody")){
						continue;
					}
					else{
						if (currentNode.hasChildNodes()){
							getAbstract(rsj, currentNode);
						}
						}


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
					try
					{
						getAllSections(rsj, currentNode);
						rsj.setBody(getContentNoWhiteSpace(currentNode));
						if (rsj.getBody().equals(null)){
							rsj.setBody("No body in document");
						}
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
            	ID id = new ID();
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("doi")) {
						try {
							rsj.setJournalDOI(currentNode.getTextContent().trim());
							id.setNumber(currentNode.getTextContent().trim());
							id.setType("doi");
							rsj.getIDs().add(id);
						} catch (Exception e) {
							rsj.setJournalDOI("NO DOI FOUND");
						}
					}else if (currentNode.getAttributes().item(0).getNodeValue().equals("pmc")) {
						try {
							id.setNumber(currentNode.getTextContent().trim());
							id.setType("pmc");
							rsj.getIDs().add(id);
						} catch (Exception e) {
							rsj.setJournalDOI("NO pmcid FOUND");
						}
					}else if (currentNode.getAttributes().item(0).getNodeValue().equals("pmid")) {
						try {
							id.setNumber(currentNode.getTextContent().trim());
							id.setType("pmid");
							rsj.getIDs().add(id);
						} catch (Exception e) {
							rsj.setJournalDOI("NO pmid FOUND");
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
			if (currentNode.getNodeName().equals("elocation-id")){
				if(currentNode.getParentNode().getNodeName().matches("article-meta")||rsj.getIssue().equals(null)){
					rsj.setIssue(currentNode.getTextContent());
				}
			}
            if(currentNode.getNodeName().equals("article-title")||currentNode.getNodeName().equals("ArticleTitle"))
			{
				try
				{
					if (currentNode.getParentNode().getNodeName().equals("title-group")||currentNode.getParentNode().getNodeName().equals("ArticleInfo")){
						rsj.setTitle(getContentNoWhiteSpace(currentNode));
					}


				}
				catch (Exception e) {
				rsj.setTitle("NO TAG 'article-title' FOUND");
				}
			}

			if (currentNode.getNodeName().matches("CopyrightHolderName|copyright-holder|copyright-statement")){
				rsj.setCopyrightHolder(currentNode.getTextContent());
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
			if (currentNode.getNodeName().matches("[aA]bstract"))
			{
				try
				{
					rsj.setAbstract(getContentNoWhiteSpace(currentNode));

				}
				catch (Exception e)
				{
					System.out.println("Error getAbstract:" +e);
				}
			}
			if (currentNode.getNodeName().matches("[Bb]ody"))
			{
				try
				{
					rsj.setBody(getContentNoWhiteSpace(currentNode));

				}
				catch (Exception e)
				{
					System.out.println("Error getBod<:" +e);
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
			if(currentNode.getNodeName().equals("page-count")&&rsj.getPages()==null)
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
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("author")) {

						try {
							for(int j=0; j<currentNode.getChildNodes().getLength();j++)
							{
								Author a= new Author();
								if(currentNode.getChildNodes().item(j).getNodeName().equals("name"))
								{
									for (int h=0; h<currentNode.getChildNodes().item(j).getChildNodes().getLength(); h++) {
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

				try {

					for(int j=0; j<currentNode.getChildNodes().getLength();j++)
					{
						Author a= new Author();
						if(currentNode.getChildNodes().item(j).getNodeName().equals("AuthorName"))
						{
							for (int h=0; h<currentNode.getChildNodes().item(j).getChildNodes().getLength(); h++) {
								if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("FamilyName"))
									if (a.getLastName()==null){
										a.setLastName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}
									else{
										a.setLastName(a.getLastName()+ " "+currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}


								if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("GivenName"))
									if (a.getFirstName()==null){
										a.setFirstName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}
									else{
										a.setFirstName(a.getFirstName()+ " "+currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}

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
				if(currentNode.getNodeName().matches("pub-date|OnlineDate")){

					if ((rsj.getFullDate()!=null)) {
						NodeList children = currentNode.getChildNodes();
						metadata.PublicationDate date = new metadata.PublicationDate();
						for (int m = 0; m < children.getLength(); m++) {
							Node childNode = children.item(m);


							if (childNode.getNodeName().equals("day")) {
								if(rsj.getFullDate().getDay()==null){
									date.setDay(childNode.getTextContent());
								}
							}
							if (childNode.getNodeName().equals("month")) {
								if(rsj.getFullDate().getMonth()==null){
									date.setMonth(childNode.getTextContent());
								}
							}
							if (childNode.getNodeName().equals("year")) {
								if(rsj.getFullDate().getYear()==null){
									date.setYear(childNode.getTextContent());
								}
							}
						}
						rsj.setFullDate(date);
					}
					else if(rsj.getFullDate()==null){

						NodeList children = currentNode.getChildNodes();
						metadata.PublicationDate date = new metadata.PublicationDate();
						for (int m = 0; m < children.getLength(); m++) {
							Node childNode = children.item(m);


							if (childNode.getNodeName().matches("[Dd]ay")) {
								date.setDay(childNode.getTextContent());
							}
							if (childNode.getNodeName().matches("[Mm]onth")) {
								date.setMonth(childNode.getTextContent());
							}
							if (childNode.getNodeName().matches("[Yy]ear")) {
								date.setYear(childNode.getTextContent());
							}
						}
						rsj.setFullDate(date);
					}
				}
            if(currentNode.getNodeName().equals("publication-year")|| currentNode.getNodeName().matches("[Yy]ear"))
            {

				try {
					if (currentNode.getParentNode().getNodeName().equals("pub-date") ||(currentNode.getParentNode().getNodeName().equals("OnlineDate"))) {

						if(currentNode.getParentNode().hasAttributes()){
							if (!currentNode.getParentNode().getAttributes().item(0).getNodeValue().matches("pmc-release|collection")){

								try {
									rsj.setPublicationYear(currentNode.getTextContent().trim());

								} catch (Exception e) {
									rsj.setPublicationYear("NO YEAR FOUND");
								}
							}
						}else{
							try {
								rsj.setPublicationYear(currentNode.getTextContent().trim());

							} catch (Exception e) {
								rsj.setPublicationYear("NO YEAR FOUND");
							}
						}

					}
				}
				catch(NullPointerException e)
				{
					e.printStackTrace();
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
			if (currentNode.getNodeName().equals("p")){
				if (currentNode.getTextContent().contains("Fig.")){
					{	Boolean match=false;
						String key="Ch1.F";
						List<String> keys = new ArrayList<>();
						String textContent=currentNode.getTextContent();
						Pattern pattern = Pattern.compile("Fig\\.\\u00A0(\\d+)");
						Matcher matcher = pattern.matcher((textContent));
						String figID="";
						while(matcher.find()){
							match=true;
								keys.add((key+matcher.group(1)));
								 figID = matcher.group();
								//System.out.println(matcher.group(1));
						}
						if(match){
							List<String> value = new ArrayList<>();

							for (int l=0; l<keys.size(); l++){
									if (figureContext.get(keys.get(l))==null) {

										value = new ArrayList<>();
										value.add(getContentNoWhiteSpace(currentNode, figID));
										figureContext.put(keys.get(l), value);
									}else{
										value = (List<String>) figureContext.get(keys.get(l));
										String container = getContentNoWhiteSpace(currentNode, figID);
										if(!value.contains(container)){
											value.add(getContentNoWhiteSpace(currentNode, figID));
										}
											figureContext.put(keys.get(l), value);
										}

								}


							//System.out.println(key+":"+getContentNoWhiteSpace(currentNode));
						}

					}
				}

			}
			if(currentNode.getNodeName().matches("xref|InternalRef"))
			{

				try
				{

					if (currentNode.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig"))
					{
						String key = currentNode.getAttributes().getNamedItem("rid").getTextContent();
						List<String> value = new ArrayList<>();
						if (figureContext.get(key)==null){
							value = new ArrayList<>();
							value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							figureContext.put(key, value);
						}
						else{
							value = (List<String>) figureContext.get(key);
							if(!value.contains(getContentNoWhiteSpace(currentNode.getParentNode(), key))){
								value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							}
							figureContext.put(key, value);
						}
					}






				}
				catch (Exception e) {
					//e.printStackTrace();
				}
				try {
					if (currentNode.getAttributes().getNamedItem("RefID").getTextContent().startsWith("Fig")) {
						String key = currentNode.getAttributes().getNamedItem("RefID").getTextContent();
						//System.out.println(key);
						List<String> value = new ArrayList<>();
						if (figureContext.get(key)==null){
							value = new ArrayList<>();
							value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							figureContext.put(key, value);
						}
						else{
							value = (List<String>) figureContext.get(key);
							if(!value.contains(getContentNoWhiteSpace(currentNode.getParentNode(), key))){
								value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							}
							figureContext.put(key, value);
						}
					}
				}
				catch(Exception e){
					//e.printStackTrace();
					}

			}
			try {
				if (currentNode.getNodeName().matches("inline-formula|disp-formula")) {

					if (node.getParentNode().getNodeName().matches("Caption|caption")) {
						rsj.setHasFormula(true);
					} else if (node.getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
						rsj.setHasFormula(true);
					} else if (node.getParentNode().getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
						rsj.setHasFormula(true);
					}
				}

			if(currentNode.getNodeName().matches("ref|Citation")){
				Citation citation = new Citation();
				getCitation(currentNode, citation);

					rsj.Bibliography.add(citation);

			}
			if(currentNode.getNodeName().matches("fig|Figure"))
            {
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
						case "Caption":
							r.setCaptionBody(getContentNoWhiteSpace(figNode));
							break;
                        case "label":
                            r.setLabel(getContentNoWhiteSpace(figNode));
                            break;

                        case "caption":
                            //TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
                            try
							{
								//if(r.getLabel()!=null) {
								//	r.setCaptionBody(r.getLabel() + " " + getContentNoWhiteSpace(figNode));
								//}
								//else{
								if (currentNode.getParentNode().getNodeName().equals("fig-group"))	{
									for (int g=0; g<currentNode.getChildNodes().getLength(); g++){
										if(currentNode.getParentNode().getChildNodes().item(g).getNodeName().equals("caption")){
											r.setCaptionBody(getContentNoWhiteSpace(currentNode.getParentNode().getChildNodes().item(g))+getContentNoWhiteSpace(figNode));

										}


									}
								}else{
									r.setCaptionBody(getContentNoWhiteSpace(figNode));

								}
									//System.out.println(r.getCaptionBody());
								//}
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
						switch (figNode.getNodeName()) {

//                        case "object-id":
//                        r.setDOI(figNode.getTextContent());
//                            break;
							case "label":
								r.setLabel(getContentNoWhiteSpace(figNode));
								break;
							case "caption":
								//TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
								try {
									//r.setCaptionBody(r.getLabel() + " " + getContentNoWhiteSpace(figgroup.item(j)));

									r.setCaptionBody(getContentNoWhiteSpace(figgroup.item(j)));
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
			catch(Exception e)
			{
			  //TODO
			}
        }
    }

	private static String getContentNoWhiteSpace(Node node, String key) {

			String content = "";

			//node.getIs

			/*if (node.getNodeName().matches("inline-formula|disp-formula")) {

				if (node.getParentNode().getNodeName().matches("Caption|caption|article-title|ArticleTitle")) {
					content += getFormula(node);
				} else if (node.getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
					content += getFormula(node);
				} else if (node.getParentNode().getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
					content += getFormula(node);
				} else if (node.getAttributes().getNamedItem("ref-type") != null) {
					if (node.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig")) {
						content += getFormula(node);
					}
				} else {
					content += node.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
				}


			}else  if (node.getNodeName().equals("sub")) {
				content += ("<sub>" + node.getTextContent() + "</sub>");

			} else if (node.getNodeName().equals("sup")) {
				content += ("<sup>" + node.getTextContent() + "</sup>");


			} else*/ if (node.getNodeName().matches("Para|p")) {
				NodeList childNodes = node.getChildNodes();
				for (int g = 0; g < childNodes.getLength(); g++) {
					Node child = childNodes.item(g);
					try{
						content += getContentNoWhiteSpace(child, key).replaceAll(key,("#figure#"));
					}catch (Exception e){
						//System.out.println("paragraph fehler.");
					}
				}
				content += "\n";

			} else if (node.getNodeName().matches("xref|InternalRef")) {

					try {
						if (node.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig")){
							if (node.getAttributes().getNamedItem("rid").getTextContent().equals(key)) {
								content += "#figure#";

							}
                        }else{
							content += (" " + references.get(node.getAttributes().getNamedItem("rid").getTextContent()) + " ");
						}

					} catch (Exception e) {
						//e.printStackTrace();
						//System.out.println("ref-type");
					}
					try {
						if(node.getAttributes().getNamedItem("RefID").getTextContent().startsWith("Fig")){
							if (node.getAttributes().getNamedItem("RefID").getTextContent().equals(key)) {
								content += "#figure#";

							}
                        }else{
							content += (" " + references.get(node.getAttributes().getNamedItem("rid").getTextContent()) + " ");
						}
					} catch (Exception e) {
						//e.printStackTrace();
						//System.out.println("RefID");
					}



			} else if (node.hasChildNodes()) {
				NodeList childNodes = node.getChildNodes();
				for (int g = 0; g < childNodes.getLength(); g++) {
					Node child = childNodes.item(g);
					try{
						content += getContentNoWhiteSpace(child, key);
					}catch (Exception e){
						//System.out.println("child nodes");
					}
				}
			} else if (node.getNodeType() == Node.TEXT_NODE) {

				String content1 = node.getNodeValue().replaceAll("\n", " ").replaceAll("\t", " ").trim();
				if (!(content1.endsWith(" "))) {
					content1 += " ";
				}
				content += content1;

			}
			//if((System.currentTimeMillis() - start)>10)
			//System.out.println(System.currentTimeMillis() - start);
			return (content.trim().replaceAll("\\s+", " ") + " ");

		/*catch(Exception e)
		{
			e.printStackTrace();
			e.getStackTrace()[0].getLineNumber();
			System.out.println("Fehler bei getContentNowhiteSpace von Context");
		}*/
		//return "Error";


	}

	private static void getCitation(Node citationNode, Citation citation) {
		try {
			NodeList citationPartList = citationNode.getChildNodes();
			if (citation.getText() == null) {
				citation.setText(getContentNoWhiteSpace(citationNode));

			}

			for (int j = 0; j < citationPartList.getLength(); j++) {
				Node citationPart = citationPartList.item(j);

				if (citationPart.getNodeName().matches("mixed-citation|BibUnstructured")) {
					citation.setText(citationPart.getTextContent().trim());
				}
				if (citationPart.getNodeName().matches("BibAuthorName|name")) {
					Author author = new Author();
					for (int h = 0; h < citationPart.getChildNodes().getLength(); h++) {
						if (citationPart.getChildNodes().item(h).getNodeName().matches("FamilyName|given-names"))
							if (author.getLastName() == null) {
								author.setLastName(citationPart.getChildNodes().item(h).getTextContent());
							} else {
								author.setLastName(author.getLastName() + " " + citationPart.getChildNodes().item(h).getTextContent());
							}


						if (citationPart.getChildNodes().item(h).getNodeName().matches("GivenName|Initials|surname"))
							if (author.getFirstName() == null) {
								author.setFirstName(citationPart.getChildNodes().item(h).getTextContent());
							} else {
								author.setFirstName(author.getFirstName() + " " + citationPart.getChildNodes().item(h).getTextContent());
							}


					}
					if (author.getFirstName() != null && author.getLastName() != null)
						citation.getAuthors().add(author);

				} else if (citationPart.getNodeName().matches("ArticleTitle|article-title")) {
					citation.setTitle(citationPart.getTextContent().trim());
				} else if (citationPart.getNodeName().matches("Year|year")) {
					citation.setYear(citationPart.getTextContent().trim());
				} else if (citationPart.getNodeName().matches("JournalTitle|source")) {
					citation.setJournal(citationPart.getTextContent().trim());
				} else if (citationPart.getNodeName().matches("Handle")) {
					ID id = new ID();
					id.setNumber(citationPart.getTextContent().trim());
					id.setType(citationPart.getParentNode().getAttributes().item(0).getNodeValue());
					citation.getIDs().add(id);
				} else if (citationPart.getNodeName().matches("pub-id")) {
					ID id = new ID();
					id.setType(citationPart.getAttributes().item(0).getNodeValue());
					id.setNumber(citationPart.getTextContent().trim());
					citation.getIDs().add(id);

				}


				if (citationPart.getNodeType() == Node.ELEMENT_NODE) {
					getCitation(citationPart, citation);
				}

			}
		}
		catch(Exception e)
		{

		}
	}


	private static String getContentNoWhiteSpace(Node node){
		//if (Text.isElementContentWhitespace)
		//long start = System.currentTimeMillis();
		try {
			String content = "";

			//node.getIs

			if (node.getNodeName().matches("inline-formula|disp-formula")) {

				if (node.getParentNode().getNodeName().matches("Caption|caption|article-title|ArticleTitle")) {
					content += getFormula(node);
				} else if (node.getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
					content += getFormula(node);
				} else if (node.getParentNode().getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
					content += getFormula(node);
				} else if (node.getAttributes().getNamedItem("ref-type") != null) {
					if (node.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig")) {
						content += getFormula(node);
					}
				} else {
					content += node.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
				}


			} else if (node.getNodeName().equals("sub")) {
				content += ("<sub>" + node.getTextContent() + "</sub>");

			} else if (node.getNodeName().equals("sup")) {
				content += ("<sup>" + node.getTextContent() + "</sup>");

			} else if (node.getNodeName().matches("Heading|title")) {
				content += (node.getTextContent() + "\n");

			} else if (node.getNodeName().matches("Para|p")) {
				NodeList childNodes = node.getChildNodes();
				for (int g = 0; g < childNodes.getLength(); g++) {
					Node child = childNodes.item(g);
					content += getContentNoWhiteSpace(child);
				}
				content += "\n";

			} else if (node.getNodeName().equals("xref")) {
				if (node.getParentNode().getNodeName().equals("article-title")) {

				} else {
					content += (" " + references.get(node.getAttributes().getNamedItem("rid").getTextContent()) + " ");

				}
			}  else if (node.hasChildNodes()) {
				NodeList childNodes = node.getChildNodes();
				for (int g = 0; g < childNodes.getLength(); g++) {
					Node child = childNodes.item(g);
					content += getContentNoWhiteSpace(child);
				}
			}else if (node.getNodeType() == Node.TEXT_NODE) {

				String content1 = node.getNodeValue().replaceAll("\n", " ").replaceAll("\t", " ").trim();
				if (!(content1.endsWith(" "))) {
					content1 += " ";
				}
				content += content1;

			}
			//if((System.currentTimeMillis() - start)>10)
			//System.out.println(System.currentTimeMillis() - start);
			return (content.trim().replaceAll("\\s+", " ") + " ");
		}
		catch(Exception e)
		{

		}
		return "Error";

	}
	private static String getFormula(Node node) {

		/*String formula= null;
		try {
			formula = convertFormula(node);
		} catch (SaxonApiException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		NodeList list = node.getChildNodes();
    	for (int j=0; j<list.getLength(); j++){
    		if(list.item(j).getNodeName().equals("mml2tex")){
				formula=list.item(j).getTextContent();
			}
		}


		if (formula.contains("overset")){
			String overset = "\\\\overset\\{\\\\mathrm\\{‾}}";
			String overline = "\\\\overline{";
			formula = formula.replaceAll(overset, overline);
			overset = "\\\\overset\\{ˇ}";
			String check ="\\\\check";
		}




		formula=" <math>"+formula+"</math> ";*/
    	//return formula;
		return node.getTextContent();
	}

	private static void context(ResultSetJournal rsj)
	{
		for(int i = 0; i<rsj.resultList.size(); i++)
		{
			if (figureContext.containsKey(rsj.resultList.get(i).figID))
				rsj.resultList.get(i).setContext((List<String>) figureContext.get(rsj.resultList.get(i).figID));
			else
				rsj.resultList.get(i).setContext((List<String>) figureContext.get(rsj.resultList.get(i).figGroupID));

			//System.out.println(rsj.resultList.get(i).figID+rsj.resultList.get(i).getContext());
		}
	}

	private static void printProgress(long startTime, long total, long current) {
		long eta = current == 0 ? 0 :
				(total - current) * (System.currentTimeMillis() - startTime) / current;

		String etaHms = current == 0 ? "N/A" :
				String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
						TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
						TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

		StringBuilder string = new StringBuilder(140);
		int percent = (int) (current * 100 / total);
		string
				.append('\r')
				.append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
				.append(String.format(" %d%% [", percent))
				.append(String.join("", Collections.nCopies(percent, "=")))
				.append('>')
				.append(String.join("", Collections.nCopies(100 - percent, " ")))
				.append(']')
				.append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
				.append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

		System.out.print(string);
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
