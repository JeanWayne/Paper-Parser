import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import metadata.Author;
import org.bson.Document;

/**
 * Created by charbonn on 02.12.2016.
 */
public class DBAuthorCollector
{
	private static DBAuthorCollector instance;

	private static MongoDatabase db;
	private static MongoClient mongoClient;
	private static String date;
	private DBAuthorCollector () {}
	public static DBAuthorCollector getInstance ()
	{
		if (DBAuthorCollector.instance == null) {
			DBAuthorCollector.instance = new DBAuthorCollector();
			DBAuthorCollector.mongoClient = new MongoClient("localhost", 27017);
			DBAuthorCollector.db = mongoClient.getDatabase("Papers");
			date=System.currentTimeMillis()+"";
		}
		return DBAuthorCollector.instance;
	}

	private void writeAuthor(Author s)
	{
		Document d = new Document("FirstName",s.getFirstName() );
		d.append("LastName",s.getLastName());
		db.getCollection("Author"+date).insertOne(d);
	}
	private Author readAuthor(String Name)
	{
		Author a = new Author();
		//Document d = new Document()
		//db.getCollection("Author"+date).find()
		return a;
	}
}
