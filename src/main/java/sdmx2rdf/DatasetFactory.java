package sdmx2rdf;

import java.io.IOException;
import java.io.InputStream;

public interface DatasetFactory {

	public InputStream getDSD(String dataset) throws IOException;
	public InputStream getData(String dataset) throws IOException;
	public InputStream getDataflow(String dataset) throws IOException;
}
