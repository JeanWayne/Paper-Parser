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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public void writeJournal(ResultSetJournal rsj,boolean withDownload) {
        Document d = new Document("journalName", rsj.getJournalName());
        List<Document> Authors = new ArrayList<>();
        List<Document> Editors = new ArrayList<>();
        List<Document> findings = new ArrayList<>();
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

            rsj.setPublicationYear(rsj.getXMLPathYear());
            d.append("year", rsj.getXMLPathYear());
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
                s = "https://www.hindawi.com/journals/" + rsj.getPublisherId() + "/" + rsj.getPublicationYear() + "/" + a.getGraphicDOI() + ".jpg";
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
                }

            } else if (rsj.getXMLPathComplete().matches(".*[sS]pringer.*")) {

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

                findings.add(new Document("findingID", a.getFindingID())
                        .append("captionTitle", a.getCaptionTitle())
                        .append("captionBody", a.getCaptionBody())
                        .append("captionBodyLength", lengthOfBody)
                        .append("URL2Image", s)
                        .append("binary_data", a.getImage())
                        .append("context", a.getContext()));
            } else

            {

                findings.add(new Document("findingID", a.getFindingID())

                        .append("captionTitle", a.getCaptionTitle())

                        .append("captionTitleLenght", lengthOfTitle)

                        .append("captionBody", a.getCaptionBody())

                        .append("captionBodyLenght", lengthOfBody)

                        .append("URL2Image", s));

            }
        }

        d.append("DOI",rsj.getJournalDOI());



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
        d.append("publisher", rsj.getPublisher());
        d.append("keywords", rsj.getKeywords());
        d.append("Copyright Holder", rsj.getCopyrightHolder());
        d.append("Bibliography", Bibliography);
        d.append("IDList",IDList);

        if(rsj.getAbstract()!=null)
            d.append("abstractLenght",rsj.getAbstract().length());
        else
            d.append("abstractLenght","Abstract is NULL");

        d.append("body",rsj.getBody());
        if(rsj.getBody()!=null)
            d.append("bodyLenght",rsj.getBody().length());
        else
            d.append("bodyLenght","Body is NULL");
        d.append("findings",findings);
            d.append("numOfFindings",findings.size());
        d.append("path2file",rsj.getXMLPathComplete());
        try{
        db.getCollection("hindawi_"+date).insertOne(d);
        //try{
          //  db.getCollection("hindawi_1503401197146").insertOne(d);
        }catch(Exception e){
            System.out.println(e);
            System.out.println(rsj.getXMLPathComplete());
        }
    }


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
        db.getCollection("hindawi_"+date).insertOne(d);
    }

    public void writeError(String error)
    {
        Document d = new Document("Error", error);
        db.getCollection("Errors_"+date).insertOne(d);
    }
}


