package demo.sdmxsource.webservice.main.chapter1.builder;

import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataflowBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.DataflowMutableBean;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.metadatastructure.DataflowMutableBeanImpl;
import org.springframework.stereotype.Service;

@Service
public class DataflowBuilder {

	/**
	 * Builds a dataflow that references the DSD
	 * @param id the id of the dataflow
	 * @param name the english name of the dataflow
	 * @param dsd the data structure that is being referenced from the dataflow
	 * @return
	 */
	public DataflowBean buildDataflow(String id, String name, DataStructureBean dsd) {
		DataflowMutableBean dataflow = new DataflowMutableBeanImpl();
		dataflow.setAgencyId("SDMXSOURCE");
		dataflow.setId(id);
		dataflow.addName("en", name);
		
		dataflow.setDataStructureRef(dsd.asReference());
		
		return dataflow.getImmutableInstance();
	}
	
}
