/**
 * Created by charbonn on 02.11.2016.
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import metadata.Author;
import org.bson.BsonDocument;
import org.bson.Document;

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

    public static MongoDBRepo getInstance ()
    {
        if (MongoDBRepo.instance == null) {
            MongoDBRepo.instance = new MongoDBRepo();
            MongoDBRepo.mongoClient = new MongoClient("localhost", 27017);
            MongoDBRepo.db = mongoClient.getDatabase("Papers");
            date=System.currentTimeMillis()+"";
        }
        return MongoDBRepo.instance;
    }
    
    public void write(String journal,String graphicID, String captionBody, String captionTitle,byte[] image)
    {
        Document d = new Document("journalName", journal);
        d.append("graphicOID",graphicID).append("captionTitle",captionTitle).append("captionBody",captionBody).append("image",image);
        db.getCollection("plos").insertOne(d);
    }

    public void writeJournal(ResultSetJournal rsj)
    {
        Document d = new Document("journalName", rsj.getJournalName());
        List<Document> Authors = new ArrayList<>();
        List<Document> Editors = new ArrayList<>();
        List<Document> findings = new ArrayList<>();
        for(Author a : rsj.getAuthors())
        {
            Authors.add(new Document("firstName",a.getFirstName()).append("lastName",a.getLastName()));
        }
        for(Author a : rsj.getEditor())
        {
            Editors.add(new Document("firstName",a.getFirstName()).append("lastName",a.getLastName()));
        }


        for(Result a : rsj.getResultList())
        {
            String s ="https://www.hindawi.com/journals/"+rsj.getPublisherId()+"/"+rsj.getPublicationYear()+"/"+a.getGraphicDOI()+".jpg";

            findings.add(new Document("findingID",a.getFindingID()).append("captionTitle",a.getCaptionTitle()).append("captionBody",a.getCaptionBody()).append("URL2Image",s));
        }

        d.append("DOI",rsj.getJournalDOI());

        if(rsj.getPublicationYear()!=null)
            d.append("year",rsj.getPublicationYear().replaceAll("\t","").replaceAll(" ",""));
        else
            d.append("year",rsj.getXMLPathYear());


        d.append("title",rsj.getTitle());
        d.append("authors",Authors);
        d.append("numOfAuthor",Authors.size());
        d.append("editor",Editors);
        d.append("numOfEditor",Editors.size());
        d.append("abstract",rsj.getAbstract());
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
        d.append("path2file",rsj.getXMLPathComplete());

        db.getCollection("hindawi_"+date).insertOne(d);
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


