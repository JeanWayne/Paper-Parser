import lombok.Getter;
import lombok.Setter;
import metadata.Author;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charbonn on 04.11.2016.
 */
@Getter
@Setter
public class ResultSetJournal
{
	List<Author> Authors = new ArrayList<>();
	List<Author> Editor = new ArrayList<>();
	List<String> Sections = new ArrayList<>();
	String Body;
	String Title;
	Integer bodyLenght=-1;
	Integer abstractLenght=-1;
	String Abstract;
	String journalDOI ="";
	String journalName;
	String PublisherId;
	String publicationYear;
	String XMLPathYear;
	String XMLPathComplete;
	String volume;
	String issue;
	String pages;
	List<String> keywords = new ArrayList<>();
	String license;
	String publisher;

	String Error="";

	List<Result> resultList = new ArrayList<>();
}
