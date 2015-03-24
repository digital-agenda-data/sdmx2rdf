package sdmx.reader;




import java.io.File;
import java.io.FileOutputStream;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.engine.DataReaderEngine;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.manager.retrieval.SdmxBeanRetrievalManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.ComponentBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.data.Keyable;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.dataparser.manager.DataReaderManager;
import org.sdmxsource.sdmx.structureretrieval.manager.InMemoryRetrievalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx.converter.Sdmx;
import sdmx.converter.SdmxDataStructureDefinitionConverter;
import sdmx.converter.SdmxDatasetConverter;
import sdmx.converter.URIMapper;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

@Service
public class SdmxReader {

	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private ReadableDataLocationFactory rdlFactory;
	
	@Autowired
	private DataReaderManager dataReaderManager;

	

	private void readData(File structureFile, File dataflowFile, File dataFile) {
		//Parse Structures into SdmxBeans and build a SdmxBeanRetrievalManager
		ReadableDataLocation rdl = rdlFactory.getReadableDataLocation(structureFile);
		StructureWorkspace workspace = structureParsingManager.parseStructures(rdl);
		SdmxBeans beans = workspace.getStructureBeans(false);
		SdmxBeanRetrievalManager retreivalManager = new InMemoryRetrievalManager(beans);
		
		//Get the DataLocation, and from this the DataReaderEngine
		ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(dataFile);
		DataReaderEngine dre = dataReaderManager.getDataReaderEngine(dataLocation, retreivalManager);
		
		
		URIMapper uriMap = new URIMapper("http://semantic.digitial-agenda-data.eu/");
		
		SdmxDataStructureDefinitionConverter dsdConverter = new SdmxDataStructureDefinitionConverter(beans, uriMap);
		SdmxDatasetConverter dataConverter = new SdmxDatasetConverter(dre, uriMap, dsdConverter.getDSD());

		
		try {
			dsdConverter.parseDataStructure();
			dataConverter.parseDataSet();
			FileOutputStream dataOutputFile = new FileOutputStream("data_output");
			dataConverter.GetModel().write(dataOutputFile, "RDF/XML-ABBREV");
			dataOutputFile.close();
			
			FileOutputStream dsdOutputFile = new FileOutputStream("dsd_output");
			dsdConverter.GetModel().write(dsdOutputFile, "RDF/XML-ABBREV");
		} catch (Exception e) {
			e.printStackTrace();
		}

		dre.close();	
	}


	public static void main(String[] args) {
		// Step 1 - Get the Application Context
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"spring/sdmx-reader.xml");

		// Step 2 - Get the main class from the Spring beans container
		SdmxReader main = applicationContext.getBean(SdmxReader.class);

		// Step 3 - Create a Readable Data Location from the File
		File structureFile = new File("src/main/resources/eurostat/DSD_isoc_ic_biski.xml");
		File dataflowFile = new File("src/main/resources/eurostat/isoc_ic_biski_dataflow.xml");
		File dataFile = new File("src/main/resources/eurostat/isoc_ic_biski_data.xml");
		
		main.readData(structureFile, dataflowFile, dataFile);

		applicationContext.close();
	}
}
