package sdmx2rdf;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.engine.DataReaderEngine;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.manager.retrieval.SdmxBeanRetrievalManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.data.KeyValue;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.dataparser.manager.DataReaderManager;
import org.sdmxsource.sdmx.structureretrieval.manager.InMemoryRetrievalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sdmx2rdf.converter.ConverterFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@Component
public class Sdmx2RdfConverter {

	@Autowired
	private ReadableDataLocationFactory rdlFactory;

	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private DataReaderManager dataReaderManager;

	@Autowired
	private URIFactory uriManager;

	@Autowired
	ConverterFactory converterFactory;

	private Model model;

	private static final Log logger = LogFactory.getLog(TestSdmxFactory.class);

	public void parse(InputStream structuresInputStream, InputStream dataInputStream) {
		SdmxBeans beans = null;
		// first read structures
		if (structuresInputStream != null) {
			ReadableDataLocation srdl = rdlFactory.getReadableDataLocation(structuresInputStream);
			StructureWorkspace workspace = structureParsingManager.parseStructures(srdl);
			beans = workspace.getStructureBeans(true);

			for (MaintainableBean bean : beans.getAllMaintainables()) {
				SDMX_STRUCTURE_TYPE beanType = bean.getStructureType();
				logger.info(MessageFormat.format("Found {0}, id={1}, name={2}", beanType, bean.getId(), bean.getName()));
				converterFactory.convert(bean, model);
			}
		}

		// read data
		if (dataInputStream != null) {
			SdmxBeanRetrievalManager retreivalManager = new InMemoryRetrievalManager(beans);
			ReadableDataLocation drdl = rdlFactory.getReadableDataLocation(dataInputStream);
			DataReaderEngine dre = dataReaderManager.getDataReaderEngine(drdl, retreivalManager);

			while (dre.moveNextDataset()) {
				List<KeyValue> dataSetAttributes = dre.getDatasetAttributes();
				dre.getCurrentDatasetHeaderBean();
				DataStructureBean dsd = dre.getDataStructure();
				logger.info(dsd.getId());
				while (dre.moveNextKeyable()) {
					dre.getCurrentKey();
					while (dre.moveNextObservation()) {
						dre.getCurrentObservation();
					}
				}
			}
		}
	}

	@PostConstruct
	private void initModel() {
		model = ModelFactory.createDefaultModel();
		for (Entry<String, String> entry : uriManager.getNSMap().entrySet()) {
			model.setNsPrefix(entry.getKey(), entry.getValue());
		}
	}

	public void writeTo(OutputStream out) {
		model.write(out, "RDF/XML-ABBREV");
	}
}
