package demo.sdmxsource.webservice.main.chapter2;

import java.io.File;

import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.manager.retrieval.SdmxBeanRetrievalManager;
import org.sdmxsource.sdmx.api.model.ResolutionSettings;
import org.sdmxsource.sdmx.api.model.ResolutionSettings.RESOLVE_CROSS_REFERENCES;
import org.sdmxsource.sdmx.api.model.ResolutionSettings.RESOLVE_EXTERNAL_SETTING;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.superbeans.base.MaintainableSuperBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.structureretrieval.manager.InMemoryRetrievalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class Chapter2ResolveReferences {


	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private ReadableDataLocationFactory rdlFactory;

	private void readStructures(File codelistConceptFile, File dsdDataflowFile) {
		
		//Step 1 - Create a retrieval Manager wrapper around the code lists and concepts
		ReadableDataLocation rdlCodelistsConcepts = rdlFactory.getReadableDataLocation(codelistConceptFile);
		SdmxBeanRetrievalManager retrievalManager = new InMemoryRetrievalManager(rdlCodelistsConcepts);
		
		//Step 2 - Specify that all references are to be resolved
		ResolutionSettings resolutionSettions = new ResolutionSettings(RESOLVE_EXTERNAL_SETTING.RESOLVE, 
																	   RESOLVE_CROSS_REFERENCES.RESOLVE_ALL);
		
		//Step 3 - Parse dataflow & data structure file, resolve all references
		ReadableDataLocation rdlDataflow = rdlFactory.getReadableDataLocation(dsdDataflowFile);
		StructureWorkspace workspace = structureParsingManager.parseStructures(rdlDataflow, resolutionSettions, retrievalManager);
	
		for(MaintainableSuperBean maintsb : workspace.getSuperBeans().getAllMaintainables()) {
			System.out.println(maintsb.getBuiltFrom().getStructureType().getType() + ": "+ maintsb.getName());
		}
	}

	public static void main(String[] args) {
		//Step 1 - Get the Application Context
		ClassPathXmlApplicationContext applicationContext = 
				new ClassPathXmlApplicationContext("spring/spring-beans-chapter1.xml");

		//Step 2 - Get the main class from the Spring beans container
		Chapter2ResolveReferences main = 
				applicationContext.getBean(Chapter2ResolveReferences.class);

		//Step 3 - Create a Readable Data Location from the File
		File codelistConceptFile = new File("src/main/resources/structures/chapter2/structures_codelists_concepts.xml");
		File dsdDataflowFile = new File("src/main/resources/structures/chapter2/structures_dsd_dataflow.xml");

		main.readStructures(codelistConceptFile, dsdDataflowFile);
		
		applicationContext.close();
	}
	
}
