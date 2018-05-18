/**
 * Created by charbonn on 02.11.2016.
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import metadata.Author;
import metadata.Citation;
import metadata.ID;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoDBRepo {

    private static MongoDBRepo instance;
    private static MongoDatabase db;

    private static MongoClient mongoClient;
    private static String date;
    private MongoDBRepo () {}
    private static String IP;

    public static MongoDBRepo getInstance ()
    {
        if(IP == null && MongoDBRepo.getInstance()==null)
            throw new IllegalArgumentException("getInstance must be invoked with an Parameters first");
        else
            return MongoDBRepo.instance;
    }
    public static MongoDBRepo getInstance (String IP,int Port, String databaseName)
    {

        if (MongoDBRepo.instance == null) {
            MongoDBRepo.instance = new MongoDBRepo();
            MongoDBRepo.mongoClient = new MongoClient(IP,Port);
//            MongoDBRepo.mongoClient = new MongoClient("127.0.0.1", 27017);
            MongoDBRepo.db = mongoClient.getDatabase(databaseName);

            date = System.currentTimeMillis() + "";
        }
            return MongoDBRepo.instance;
    }


    public void write(String journal,String graphicID, String captionBody, String captionTitle,byte[] image)
    {
        Document d = new Document("journalName", journal);
        d.append("graphicOID",graphicID).append("captionTitle",captionTitle).append("captionBody",captionBody).append("image",image);
        db.getCollection("plos").insertOne(d);
    }

    public void writeError(String Path,String ExceptionText, String modus)
	{
		Document d = new Document("Exception",ExceptionText);
		d.append("path2file",Path);
		db.getCollection("Errors_"+date).insertOne(d);
	}
    public void writeJournal(ResultSetJournal rsj,boolean withDownload) {
        Document d = new Document("journalName", rsj.getJournalName());
        d.put("_id",new ObjectId());
        d.append("DOI",rsj.getJournalDOI());
        List<Document> Authors = new ArrayList<>();
        List<Document> Editors = new ArrayList<>();
        List<Document> findings = new ArrayList<>();
        List<Object> findingsRef = new ArrayList<>();
        List<Document> Bibliography = new ArrayList<>();
        List<Document> IDList =new ArrayList<>();

        for (ID ID: rsj.getIDs()) {
            IDList.add(new Document("type", ID.getType()).append("number", ID.getNumber()));
        }

        for (Citation c : rsj.getBibliography()){
            List<Document> BibAuthors = new ArrayList<>();

            for (Author bibAuthor : c.getAuthors()){
                BibAuthors.add(new Document("firstName", bibAuthor.getFirstName()).append("lastName", bibAuthor.getLastName()));
            }
            List<Document> IDs =new ArrayList<>();
            for (ID ID: c.getIDs()){
                IDs.add(new Document("type", ID.getType()).append("number", ID.getNumber()));
            }
            Bibliography.add(new Document("Authors", BibAuthors).append("title", c.getTitle()).append("year", c.getYear()).append("journal", c.getJournal()).append("IDs", IDs).append("Text", c.getText()));
        }
        for(Author a : rsj.getAuthors())
        {
            Authors.add(new Document("firstName",a.getFirstName()).append("lastName",a.getLastName()));
        }
        for(Author a : rsj.getEditor())
        {
            Editors.add(new Document("firstName",a.getFirstName()).append("lastName",a.getLastName()));
        }

        if(rsj.getPublicationYear()!=null)
            d.append("year",rsj.getPublicationYear().replaceAll("\t","").replaceAll(" ",""));
        else {

            rsj.setPublicationYear(rsj.getFullDate().getYear());
            d.append("year", rsj.getPublicationYear());
        }
        metadata.PublicationDate publicationDate = rsj.getFullDate();
        Document pdate;
        try{
            pdate=(new Document("day", publicationDate.getDay()).append("month", publicationDate.getMonth()).append("year", publicationDate.getYear()) );
        } catch (Exception e){
            pdate=null;
        }

        Boolean tested= false;

        for(Result a : rsj.getResultList()) {
            if (rsj.getPublicationYear() == null)
                System.out.println("_____________________________________________");
            if (rsj.getPublicationYear().equals("null"))
                System.out.println("_____________________________________________");

            String s = "";

            //System.out.println(rsj.getJournalDOI());
            if (rsj.getXMLPathComplete().contains("PMC")) {
                s = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC" + rsj.getPmcID() + "/bin/" + a.getGraphicDOI() + ".jpg";
            } else if (rsj.getXMLPathComplete().matches(".*[hH]indawi.*")) {

                String link = a.getGraphicDOI();
                if(link==null)link="0";
                Matcher matcher = Pattern.compile("\\d+\\.(.*)").matcher(link);
                String figID;
                try {
                    matcher.find();
                    figID = matcher.group(1);
                }
                catch (Exception e){
                    figID="0";
                }
                String doi = rsj.getJournalDOI();
                matcher = Pattern.compile(".?\\/(.*)").matcher(doi);
                try {
                    matcher.find();
                    link = matcher.group(1)+"."+figID;                }catch (Exception e){
                    link="no valid URL found";
                }
                s = "https://www.hindawi.com/journals/" + rsj.getPublisherId() +  "/" +link + ".jpg";
                if (!tested) {
                    try {
                        URL hindawiTest = new URL(s);
                        BufferedReader in = new BufferedReader(new InputStreamReader(hindawiTest.openStream()));
                        String inputLine;
                        int n = 0;
                        while ((inputLine = in.readLine()) != null && n < 10) {
                            if (inputLine.contains("File or directory not found")) {
                                s = "https://www.hindawi.com/journals/" + rsj.getPublisherId() + "/" + rsj.getPublicationYear() + "/" + a.getGraphicDOI() + ".svgz";
                                in.close();
                                break;
                            }
                            n++;
                            tested = true;
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }            }  else if (rsj.getXMLPathComplete().matches(".*[sS]pringer.*")) {

                String newDoi = rsj.getJournalDOI().substring(0, 7) + "%2F" + rsj.getJournalDOI().substring(8);
                s = "https://static-content.springer.com/image/art%3A" + newDoi + "/" + a.getGraphicDOI();
            } else if (rsj.getPublisher().matches(".*[fF]rontiers.*")) {
                if (rsj.getFile().getParent() != null) {
                    s = rsj.getFile().getParent() + "\\" + a.getGraphicDOI();
                }
            } else if (rsj.getXMLPathComplete().matches(".*[cC]opernicus.*")) {
                s = a.getGraphicDOI();
            }

            a.setImageURL(s);

            boolean downloading = withDownload;

            int lengthOfTitle = 0;

            int lengthOfBody = 0;
            Document img = new Document();
            img.put("_id",new ObjectId());

            if (a.getCaptionTitle() != null)

                    lengthOfTitle = a.getCaptionTitle().length();

                    if (a.getCaptionBody() != null)

                        lengthOfBody = a.getCaptionBody().length();

                    if (downloading) {

                        try {
                            a.download();
                        } catch (IOException e) {
                            //e.printStackTrace();
                    System.out.println(a.getImageURL());
                }

                findings.add(img.append("findingID", a.getFindingID())
                        .append("originDOI",rsj.getJournalDOI())
                        .append("source_id",d.get("_id"))
                        .append("captionTitle", a.getLabel())
                        .append("captionTitleLenght",  a.getLabel().length())
                        .append("captionBody", a.getCaptionBody())
                        .append("captionBodyLength", lengthOfBody)
                        .append("URL2Image", s)
                        .append("binary_data", a.getImage())
                        .append("context", a.getContext()));
            } else

            {

                findings.add(img.append("findingID", a.getFindingID())
                        .append("originDOI",rsj.getJournalDOI())
                        .append("source_id",d.get("_id"))
                        .append("captionTitle", a.getLabel())

                        .append("captionBody", a.getCaptionBody())

                        .append("captionBodyLenght", lengthOfBody)

                        .append("URL2Image", s)
                        .append("context", a.getContext()));

            }
        }

        //set licenseType
        String license = (String) rsj.getLicense();
        String licenseType="unclassified";
        if(license==null){
            licenseType="invalid";
        }
        else if(license.contains("creativecommons.org/licenses/by/4.0")){
            licenseType="cc-by-4.0";
        }else if(license.contains("creativecommons.org/licenses/by/3.0")){
            licenseType="cc-by-3.0";
        }else if(license.contains("creativecommons.org/licenses/by/2.0")){
            licenseType="cc-by-2.0";
        }else if(license.contains("creativecommons.org/licenses/by/2.5")) {
            licenseType = "cc-by-2.5";
        }else if(license.contains("www.frontiersin.org/licenseagreement")){
            licenseType = "frontiers";
        }



        d.append("title",rsj.getTitle());
        d.append("authors",Authors);
        d.append("numOfAuthor",Authors.size());
        //d.append("editor",Editors);
        //d.append("numOfEditor",Editors.size());
        d.append("abstract",rsj.getAbstract());
        d.append("journalVolume", rsj.getVolume());
        d.append("journalIssue", rsj.getIssue());
        d.append("pages", rsj.getPages());
        d.append("license", rsj.getLicense());
	d.append("LicenseType", licenseType);
        d.append("publisher", rsj.getPublisher());
        d.append("keywords", rsj.getKeywords());
        d.append("Copyright Holder", rsj.getCopyrightHolder());
        d.append("Bibliography", Bibliography);
        d.append("IDList",IDList);
        d.append("PublicationDate", pdate);
        d.append("formula", rsj.hasFormula);

        if(rsj.getAbstract()!=null)
            d.append("abstractLenght",rsj.getAbstract().length());
        else
            d.append("abstractLenght","Abstract is NULL");

        d.append("body",rsj.getBody());
        if(rsj.getBody()!=null)
            d.append("bodyLenght",rsj.getBody().length());
        else
            d.append("bodyLenght","Body is NULL");

        //d.append("findings",findings);


        d.append("numOfFindings",findings.size());
        d.append("path2file",rsj.getXMLPathComplete());
        try{
            //db.getCollection("Version26.09.").insertOne(d);
            for(Document y : findings)
            {
                findingsRef.add(y.get("_id"));
                db.getCollection("Corpus_Image_"+date).insertOne(y);
            }
            d.append("findingsRef",findingsRef);

            db.getCollection("Corpus_Journal_"+date).insertOne(d);

            //try{
          //  db.getCollection("hindawi_1503401197146").insertOne(d);
        }catch(Exception e){
            System.out.println(e);
            System.out.println(rsj.getXMLPathComplete());
        }
    }

    @Deprecated
    public void write(String journal, String Year, String DOI, int findingID, String captionBody, String imageURL, List<Author> Author, List<Author> Editor)
    {
        Document d = new Document("journalName", journal);
        List<Document> Authors = new ArrayList<>();
        List<Document> Editors = new ArrayList<>();

        for(Author a : Author)
        {
            Authors.add(new Document("firstName",a.getFirstName()).append("lastName",a.getLastName()));
        }
        for(Author a : Editor)
        {
            Editors.add(new Document("firstName",a.getFirstName()).append("lastName",a.getLastName()));
        }
        d.append("Year",Year).append("DOI",DOI).append("findingID",findingID).append("captionBody",captionBody).append("ImageURL",imageURL).append("Authors",Authors).append("Editor",Editors);
        //db.getCollection("hindawi_"+date).insertOne(d);
    }
	@Deprecated
    public void writeError(String error)
    {
        Document d = new Document("Error", error);
        db.getCollection("Errors_"+date).insertOne(d);
    }
}


