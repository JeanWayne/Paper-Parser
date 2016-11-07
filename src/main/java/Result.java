import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by charbonn on 02.11.2016.
 */

@Getter
@Setter
public class Result
{
	ResultSetJournal rsj;
	int findingID;
	String DOI;
	String captionTitle;
	String captionBody;
	String label;
	String graphicDOI;
	String graphicID;
	String path;

	public void Restul(ResultSetJournal rsj)
	{this.rsj=rsj;}

	public void save2disk() throws IOException
	{
		graphicID=graphicDOI.substring(graphicDOI.length()-4,graphicDOI.length());
		path=graphicDOI.substring(graphicDOI.length()-25,graphicDOI.length());
		URL url = new URL("http://journals.plos.org/ploscompbiol/article/figure/image?size=large&id="+graphicDOI);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1!=(n=in.read(buf)))
		{
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
		FileOutputStream fos = new FileOutputStream("C://paper//"+path+".png");
		fos.write(response);
		fos.close();
	}
	public void save2dbWithImage() throws IOException
	{
      	MongoDBRepo db = MongoDBRepo.getInstance();
		graphicID=graphicDOI.substring(graphicDOI.length()-4,graphicDOI.length());
		path=graphicDOI.substring(graphicDOI.length()-25,graphicDOI.length());
		//URL url = new URL("http://journals.plos.org/ploscompbiol/article/figure/image?size=large&id="+graphicDOI);
		URL url = new URL("https://www.hindawi.com/journals/"+rsj.getPublisherId()+"/"+rsj.getPublicationYear()+"/"+graphicDOI+".jpg");
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1!=(n=in.read(buf)))
		{
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
		db.write("PLOS",graphicDOI,captionBody,captionTitle,response);
	}
	public void save2dbWithOutImage() throws IOException
	{
		MongoDBRepo db = MongoDBRepo.getInstance();
		//URL url = new URL("http://journals.plos.org/ploscompbiol/article/figure/image?size=large&id="+graphicDOI);
		URL url = new URL("https://www.hindawi.com/journals/"+rsj.getPublisherId()+"/"+rsj.getPublicationYear()+"/"+graphicDOI+".jpg");
		if(rsj.getError()=="")
		db.write(rsj.getJournalName(),rsj.getPublicationYear(),rsj.getJournalDOI(),findingID,captionBody,url.toString(),rsj.getAuthors(),rsj.getEditor());
		else
			db.writeError(rsj.getError());
	}





}
