/**
 * Created by charbonn on 02.11.2016.
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBRepo {

    private static MongoDBRepo instance;
    private static MongoDatabase db;
    private static MongoClient mongoClient;

    private MongoDBRepo () {}

    public static MongoDBRepo getInstance ()
    {
        if (MongoDBRepo.instance == null) {
            MongoDBRepo.instance = new MongoDBRepo();
            MongoDBRepo.mongoClient = new MongoClient("localhost", 27017);
            MongoDBRepo.db = mongoClient.getDatabase("Paper");
        }
        return MongoDBRepo.instance;
    }
    
    public void write(String journal,String graphicID, String captionBody, String captionTitle,byte[] image)
    {
        Document d = new Document("journalName", journal);
        d.append("graphicOID",graphicID).append("captionTitle",captionTitle).append("captionBody",captionBody).append("image",image);
        db.getCollection("plos").insertOne(d);
    }
}


