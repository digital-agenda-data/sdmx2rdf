package sdmx2rdf;

import java.io.InputStream;

public class DataflowFactory {

	public InputStream getDSD(String dataset) {
		return DataflowFactory.class.getResourceAsStream("/" + dataset + "/DSD_" + dataset + ".xml");
	}

	public InputStream getData(String dataset) {
		return DataflowFactory.class.getResourceAsStream("/" + dataset + "/" + dataset + "_data.xml");
	}

}
