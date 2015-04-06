package sdmx2rdf;

import java.io.InputStream;

public class TestDatasetFactory implements DatasetFactory {

	public InputStream getDSD(String dataset) {
		return TestDatasetFactory.class.getResourceAsStream("/" + dataset + "/DSD_" + dataset + ".xml");
	}

	public InputStream getData(String dataset) {
		return TestDatasetFactory.class.getResourceAsStream("/" + dataset + "/" + dataset + "_data.xml");
	}
	
	public InputStream getDataflow(String dataset) {
		return TestDatasetFactory.class.getResourceAsStream("/" + dataset + "/" + dataset + "_dataflow.xml");
	}

}
