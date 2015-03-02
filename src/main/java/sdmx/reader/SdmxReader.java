package sdmx.reader;




import java.io.File;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.engine.DataReaderEngine;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.manager.retrieval.SdmxBeanRetrievalManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.data.Keyable;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.dataparser.manager.DataReaderManager;
import org.sdmxsource.sdmx.structureretrieval.manager.InMemoryRetrievalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SdmxReader {

	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private ReadableDataLocationFactory rdlFactory;
	
	@Autowired
	private DataReaderManager dataReaderManager;

	private void readData(File structureFile, File dataFile) {
		//Parse Structures into SdmxBeans and build a SdmxBeanRetrievalManager
		ReadableDataLocation rdl = rdlFactory.getReadableDataLocation(structureFile);
		StructureWorkspace workspace = structureParsingManager.parseStructures(rdl);
		SdmxBeans beans = workspace.getStructureBeans(false);
		SdmxBeanRetrievalManager retreivalManager = new InMemoryRetrievalManager(beans);
		
		//Get the DataLocation, and from this the DataReaderEngine
		ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(dataFile);
		DataReaderEngine dre = dataReaderManager.getDataReaderEngine(dataLocation, retreivalManager);
		
		//Iterate through all the datasets, keys, and observations per key
		while(dre.moveNextDataset()) {
			DataStructureBean dsd = dre.getDataStructure();
			System.out.println(dsd.getName());
			
			while(dre.moveNextKeyable()) {
				Keyable currentKey = dre.getCurrentKey();
				System.out.println(currentKey);
				while(dre.moveNextObservation()) {
					Observation obs = dre.getCurrentObservation();
					System.out.println(obs);
				}
			}
		}
		
		//Close Reader, close off resources
		dre.close();
		
	}

	public static void main(String[] args) {
		// Step 1 - Get the Application Context
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"spring/sdmx-reader.xml");

		// Step 2 - Get the main class from the Spring beans container
		SdmxReader main = applicationContext.getBean(SdmxReader.class);

		// Step 3 - Create a Readable Data Location from the File
		File structureFile = new File("src/main/resources/structures/chapter2/structures_full.xml");
		File dataFile = new File("src/main/resources/structures/test/sample_data.xml");
		
		main.readData(structureFile, dataFile);

		applicationContext.close();
	}
}
