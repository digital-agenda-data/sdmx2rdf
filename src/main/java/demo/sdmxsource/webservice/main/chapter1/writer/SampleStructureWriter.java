package demo.sdmxsource.webservice.main.chapter1.writer;

import java.io.OutputStream;

import org.sdmxsource.sdmx.api.manager.output.StructureWriterManager;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.format.StructureFormat;
import org.sdmxsource.sdmx.util.beans.container.SdmxBeansImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import demo.sdmxsource.webservice.main.chapter1.builder.AgencySchemeBuilder;
import demo.sdmxsource.webservice.main.chapter1.builder.CodelistBuilder;
import demo.sdmxsource.webservice.main.chapter1.builder.ConceptSchemeBuilder;
import demo.sdmxsource.webservice.main.chapter1.builder.DataStructureBuilder;
import demo.sdmxsource.webservice.main.chapter1.builder.DataflowBuilder;

@Service
public class SampleStructureWriter  {

	@Autowired
	private StructureWriterManager structureWritingManager;

	@Autowired
	private AgencySchemeBuilder agencySchemeBuilder;

	@Autowired
	private CodelistBuilder codelistBuilder;
	
	@Autowired
	private ConceptSchemeBuilder conceptSchemeBuilder;
	
	@Autowired
	private DataStructureBuilder dataStructureBuilder;

	@Autowired
	private DataflowBuilder dataflowBuilder;
	
	public void writeStructureToFile(StructureFormat outputFormat, OutputStream out) {
		SdmxBeans beans = new SdmxBeansImpl();
		beans.addAgencyScheme(agencySchemeBuilder.buildAgencyScheme());
		beans.addCodelist(codelistBuilder.buildCountryCodelist());
		beans.addIdentifiable(codelistBuilder.buildIndicatorCodelist());
		beans.addIdentifiable(conceptSchemeBuilder.buildConceptScheme());
		DataStructureBean dsd = dataStructureBuilder.buildDataStructure();
		beans.addIdentifiable(dsd);
		beans.addIdentifiable(dataflowBuilder.buildDataflow("DF_WDI", "World Development Indicators", dsd));
		
		structureWritingManager.writeStructures(beans, outputFormat, out);
	}
}


