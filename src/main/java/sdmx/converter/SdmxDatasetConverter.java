package sdmx.converter;

import java.util.Map.Entry;

import org.sdmxsource.sdmx.api.engine.DataReaderEngine;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
import org.sdmxsource.sdmx.api.model.data.KeyValue;
import org.sdmxsource.sdmx.api.model.data.Keyable;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.sdmxsource.sdmx.api.model.header.DatasetStructureReferenceBean;
import org.sdmxsource.sdmx.api.model.header.HeaderBean;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

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
		
		for (DatasetStructureReferenceBean structure : header.getStructures()) {
			System.out.println("Dimension at observation: " + structure.getDimensionAtObservation());
			System.out.println("FIXED ID: " + PrimaryMeasureBean.FIXED_ID);
			System.out.println("Structure id:" + structure.getId());
		}
	}
		
	public void parseDataSet() throws Exception {
		
		initResources();
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
						
						// FIXME(catalinb): this must match the URL from the DSD entry
						Property property = model.createProperty(uriMapper.getRoot() + "dimension/" + seriesKey.getConcept());
						rdfObs.addProperty(property, code);
					}
					
					if (currentKey.isTimeSeries()) {
						//FIXME(catalinb)
						Property obsX = model.createProperty("http://sdmx-dimension:timePeriod");
						rdfObs.addProperty(obsX, obs.getObsTime());
						BaseDatatype datatType;
					} else {
						throw new Exception("Not a time series.");
					}
					
					Literal value = model.createTypedLiteral(new Double(Double.parseDouble(obs.getObservationValue())));
					rdfObs.addProperty(Cube.measure, value);
					dataset.addProperty(Cube.observation, rdfObs);
				}
			}
		}
	}
	
	public Model GetModel() {
		return model;
	}
}
