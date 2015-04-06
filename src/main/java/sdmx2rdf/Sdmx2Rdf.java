package sdmx2rdf;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
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
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataflowBean;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.DataflowMutableBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.dataparser.manager.DataReaderManager;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.metadatastructure.DataflowMutableBeanImpl;
import org.sdmxsource.sdmx.structureretrieval.manager.InMemoryRetrievalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sdmx2rdf.converter.ConverterFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

@Component
public class Sdmx2Rdf {

	@Autowired
	private ReadableDataLocationFactory rdlFactory;

	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private DataReaderManager dataReaderManager;

	@Autowired
	private URIFactory uriFactory;

	@Autowired
	ConverterFactory converterFactory;

	private Model model;

	private final Log logger = LogFactory.getLog(getClass());

	public void parse(InputStream[] structuresInputStreams, InputStream dataInputStream) {
		SdmxBeans beans = null;

		for (InputStream structureInputStream : structuresInputStreams) {
			ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(structureInputStream);
			StructureWorkspace workspace = structureParsingManager.parseStructures(dataLocation);
			if ( beans == null ) {
				beans = workspace.getStructureBeans(true);
			} else {
				beans.merge(workspace.getStructureBeans(true));
			}
		}
		SdmxBeanRetrievalManager retreivalManager = new InMemoryRetrievalManager(beans);

		Map<String, Resource> datasetMap = new HashMap<String, Resource>();
		Map<String, Resource> dsdMap = new HashMap<String, Resource>();
		for (MaintainableBean bean : beans.getAllMaintainables()) {
			SDMX_STRUCTURE_TYPE beanType = bean.getStructureType();
			logger.info(MessageFormat.format("Found {0}, id={1}, name={2}", beanType, bean.getId(), bean.getName()));
			Resource resource = converterFactory.convert(bean, model);
			switch (bean.getStructureType()) {
			case DATAFLOW:
				datasetMap.put(bean.getId(), resource);
				break;
			case DSD:
				dsdMap.put(bean.getId(), resource);
				// model.setNsPrefix(bean.getId()+"-dimension", resource.getURI() + "/Dimension/");
				break;
			default:
				break;
			}
		}

		// read data
		if (dataInputStream != null) {
			ReadableDataLocation drdl = rdlFactory.getReadableDataLocation(dataInputStream);
			DataReaderEngine dre = dataReaderManager.getDataReaderEngine(drdl, retreivalManager);
			String dataSetId = dre.getHeader().getDatasetId();
			Resource datasetRdf = datasetMap.get(dataSetId);

			while (dre.moveNextDataset()) {
				if ( datasetRdf == null ) {
					// TODO: refactor this
					// when we had no dataflow, try to create the dataset on the fly
					DataflowMutableBean dataFlow = new DataflowMutableBeanImpl();
					dataFlow.setAgencyId(dre.getDataStructure().getAgencyId());
					dataFlow.setId(dataSetId);
					dataFlow.addName("en", dataSetId);
					dataFlow.setDataStructureRef(dre.getDataStructure().asReference());
					DataflowBean dataFlowBean = dataFlow.getImmutableInstance();
					datasetRdf = converterFactory.convert(dataFlowBean, model);
				}
				while (dre.moveNextKeyable()) {
					while (dre.moveNextObservation()) {
						Observation obs = dre.getCurrentObservation();
						// String dimensionAtObservation =
						// dre.getCurrentDatasetHeaderBean().getDataStructureReference().getDimensionAtObservation();
						converterFactory.getObservationConverter().convert(datasetRdf, obs, model, retreivalManager);
					}
				}
			}
		}
	}

	@PostConstruct
	private void initModel() {
		model = ModelFactory.createDefaultModel();
		for (Entry<String, String> entry : uriFactory.getNSMap().entrySet()) {
			model.setNsPrefix(entry.getKey(), entry.getValue());
		}
	}

	public void writeTo(OutputStream out) {
		model.write(out, "RDF/XML-ABBREV");
	}
}
