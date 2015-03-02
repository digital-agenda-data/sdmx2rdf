package demo.sdmxsource.webservice.main.chapter1.builder;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.reference.StructureReferenceBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.DataStructureMutableBean;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.datastructure.DataStructureMutableBeanImpl;
import org.sdmxsource.sdmx.util.beans.reference.StructureReferenceBeanImpl;
import org.springframework.stereotype.Service;

@Service
public class DataStructureBuilder {

	public DataStructureBean buildDataStructure() {
		DataStructureMutableBean dsd = new DataStructureMutableBeanImpl();
		dsd.setAgencyId("SDMXSOURCE");
		dsd.setId("WDI");
		dsd.addName("en", "World Development Indicators");
		
		dsd.addDimension(createConceptReference("COUNTRY"), createCodelistReference("CL_COUNTRY"));
		dsd.addDimension(createConceptReference("INDICATOR"), createCodelistReference("CL_INDICATOR"));
		dsd.addDimension(createConceptReference("TIME"), null).setTimeDimension(true);
		dsd.addPrimaryMeasure(createConceptReference("OBS_VALUE"));
		
		return dsd.getImmutableInstance();
	}
	
	private StructureReferenceBean createCodelistReference(String id) {
		return new StructureReferenceBeanImpl("SDMXSOURCE", id, "1.0", SDMX_STRUCTURE_TYPE.CODE_LIST);
	}
	
	private StructureReferenceBean createConceptReference(String id) {
		return new StructureReferenceBeanImpl("SDMXSOURCE", "CONCEPTS", "1.0", SDMX_STRUCTURE_TYPE.CONCEPT, id);
	}
	
}
