/**
 * Created by charbonn on 02.11.2016.
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

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
    public void write(String journal, String Year, String DOI, int findingID, String captionBody, String imageURL, List<String> Author, List<String> Editor)
    {
        Document d = new Document("journalName", journal);
        d.append("Year",Year).append("DOI",DOI).append("findingID",findingID).append("captionBody",captionBody).append("ImageURL",imageURL).append("Author",Author).append("Editor",Editor);
        db.getCollection("hindawi_"+date).insertOne(d);
    }

    public void writeError(String error)
    {
        Document d = new Document("Error", error);
        db.getCollection("Errors_"+date).insertOne(d);
    }
}


