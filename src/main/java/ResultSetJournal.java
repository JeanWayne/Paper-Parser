import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charbonn on 04.11.2016.
 */
@Getter
@Setter
public class ResultSetJournal
{
	List<String> Authors = new ArrayList<>();
	List<String> Editor = new ArrayList<>();
	String journalDOI;
	String journalName;
	String publisherId;
	String publicationYear;

	String Error="";

	List<Result> resultList = new ArrayList<>();


}
