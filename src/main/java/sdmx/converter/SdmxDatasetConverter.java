package sdmx.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map.Entry;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.engine.DataReaderEngine;
import org.sdmxsource.sdmx.api.manager.retrieval.SdmxBeanRetrievalManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.ConceptBaseBean;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionListBean;
import org.sdmxsource.sdmx.api.model.data.KeyValue;
import org.sdmxsource.sdmx.api.model.data.Keyable;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.sdmxsource.sdmx.api.model.header.DatasetStructureReferenceBean;
import org.sdmxsource.sdmx.api.model.header.HeaderBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.structureretrieval.manager.InMemoryRetrievalManager;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class SdmxDatasetConverter {
	private DataReaderEngine dre;
	
	private Model model;
	private Resource dataStructureDefinition;
	private Resource dataset;
	private Resource dataFlow;
	URIMapper uriMapper;
		
	public SdmxDatasetConverter(DataReaderEngine dre, URIMapper uriMap, Resource dsd) {
		this.dre = dre;
		this.dataStructureDefinition = dsd;
		this.uriMapper = uriMap;
		model = ModelFactory.createDefaultModel();
		
		String id = "UNKNOWN_ID";
		HeaderBean header = dre.getHeader();
		id = header.getDatasetId();
		System.out.println("ID: " + id);
	
		
		
		dataset = model.createResource(uriMap.getRoot() + "dataset#" + id);
		
		for (Entry<String, String> entry : uriMap.getNSPrefixes()) {
			model.setNsPrefix(entry.getKey(), entry.getValue());
		}
		
		dataset.addProperty(Cube.dataSet, Cube.DataSet);
	}
	
	public void initResources() {
		
		// TODO
		HeaderBean header = dre.getHeader();
		
		//dataset = model.createResource("http://purl.org/linked-data/sdmx/2009/dataset#" + header.getDatasetId());
		//dataStructureDefinition = model.createResource("http://purl.org/linked-data/sdmx/2009/dsd#" + header.getDatasetId());
		dataFlow = model.createResource("http://purl.org/linked-data/sdmx/2009/dataflow#" + header.getDatasetId());
	}
		
	public void parseDataSet() {
		
		
		// TODO add type dcat:Dataset
		
		dataset.addProperty(Cube.structure, dataStructureDefinition);
		
		while(dre.moveNextDataset()) {
			DataStructureBean bean = dre.getDataStructure();
			System.out.println("Catalin:" + bean);
			while(dre.moveNextKeyable()) {
				Keyable currentKey = dre.getCurrentKey();
				while(dre.moveNextObservation()) {
					Observation obs = dre.getCurrentObservation();
					// TODO generate unique URIs here
					
					String uri = uriMapper.getRoot() + "data/";
					for (KeyValue seriesKey : currentKey.getKey()) {
						uri += seriesKey.getCode() + "/";
					}
					if (currentKey.isTimeSeries()) {
						uri += obs.getObsTime();
					}
					
					Resource rdfObs = model.createResource(uri + obs.hashCode());
					for (KeyValue seriesKey : currentKey.getKey()) {
						
						String codeUri = uriMapper.getRoot() + "def/code/" + seriesKey.getConcept() + "#" + seriesKey.getCode();
						Resource code = model.createResource(codeUri);
						
						Property property = model.createProperty("http://smdx-dimension:" + seriesKey.getConcept());
						rdfObs.addProperty(property, code);
						
					}
					if (currentKey.isTimeSeries()) {
						Property obsX = model.createProperty("http://sdmx-dimension:timePeriod");
						rdfObs.addProperty(obsX, obs.getObsTime());
						BaseDatatype datatType;
					}
					Property obsY = model.createProperty("http://sdmx-measure:obsValue");
					Literal value = model.createTypedLiteral(new Double(Double.parseDouble(obs.getObservationValue())));
					rdfObs.addProperty(obsY, value);
					rdfObs.addProperty(obsY, value);
					dataset.addProperty(Cube.observation, rdfObs);
				}
			}
		}
	}
	
	public Model GetModel() {
		return model;
	}
}
