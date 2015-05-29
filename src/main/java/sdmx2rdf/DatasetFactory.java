package sdmx2rdf;

import java.io.IOException;
import java.io.InputStream;

public interface DatasetFactory {

	public InputStream getDSD(String dataset, boolean forceRefresh) throws Exception;
	public InputStream getData(String dataset, boolean forceRefresh) throws IOException, Exception;
	public InputStream getDataflow(String dataset) throws IOException;
}
