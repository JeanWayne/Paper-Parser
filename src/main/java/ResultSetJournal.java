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
	String journalDOI;
	String journalName;
	String publisherId;
	String publicationYear;
	String XMLPathYear;

	String Error="";

	List<Result> resultList = new ArrayList<>();


}
