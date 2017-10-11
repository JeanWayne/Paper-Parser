import lombok.Getter;
import lombok.Setter;
import metadata.Author;
import metadata.Citation;
import metadata.ID;
import metadata.PublicationDate;

import java.io.File;
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
	String Volume;
	String Issue;
	String Pages;
	List<String> Keywords = new ArrayList<>();
	String License;
	String Publisher;
	String pmcID;
	File file;
	boolean hasFormula;
	String copyrightHolder;
	String Error="";
	List <Citation> Bibliography = new ArrayList<>();
	List<ID> IDs = new ArrayList<>();
	List<Result> resultList = new ArrayList<>();
	PublicationDate fullDate;
}
