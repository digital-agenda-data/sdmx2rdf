package sdmx2rdf.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.manager.retrieval.SdmxBeanRetrievalManager;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.sdmxsource.sdmx.api.model.data.KeyValue;
import org.sdmxsource.sdmx.api.model.data.Keyable;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx2rdf.URIFactory;
import sdmx2rdf.vocabularies.Cube;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

@Service
public class ObservationConverter {
	
	@Autowired
	protected URIFactory uriFactory;

	@Autowired
	protected ConverterFactory converterFactory;
	
	@Autowired
	protected TimeFormatConverter timeConverter;

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	public Resource convert(Resource dataset, Observation observation, Model model, SdmxBeanRetrievalManager retreivalManager) {
		Keyable seriesKey = observation.getSeriesKey();
		
		String uri = dataset.getURI() + "/data/";
		for (KeyValue key : seriesKey.getKey()) {
			uri += key.getCode() + "/";
		}
		
		if (seriesKey.isTimeSeries()) {
			uri += observation.getObsTime();
		}
		
		Resource rdfObs = model.createResource(uri);
		rdfObs.addProperty(Cube.dataSet, dataset);
		rdfObs.addProperty(RDF.type, Cube.Observation);

		DataStructureBean dsd = seriesKey.getDataStructure();
		// add series keys
		for (KeyValue keyValue : seriesKey.getKey()) {
			DimensionBean dimensionBean = dsd.getDimension(keyValue.getConcept());
			if (dimensionBean.hasCodedRepresentation()) {
				CrossReferenceBean codelistRef = dimensionBean.getRepresentation().getRepresentation();
				CodelistBean codelistBean = (CodelistBean) retreivalManager.getMaintainableBean(codelistRef);
				CodeBean codeBean = codelistBean.getCodeById(keyValue.getCode());
				
				Property dimensionProperty = model.createProperty(uriFactory.getURI(dimensionBean.getUrn()));
				Resource codeResource = model.createResource(uriFactory.getURI(codeBean.getUrn()));
				rdfObs.addProperty(dimensionProperty, codeResource);
			} else {
				logger.error("unsupported dimension representation");
			}
		}
		
		if (seriesKey.isTimeSeries()) {
			DimensionBean timeDimension = dsd.getTimeDimension();
			Property timeDimensionProperty = model.createProperty(uriFactory.getURI(timeDimension.getUrn()));

			String timeURI = timeConverter.getTimeBaseURI(observation.getObsTime());
			rdfObs.addProperty(timeDimensionProperty, model.createResource(timeURI));
		} else {
			logger.error("Only time series supported.");
		}
		
		for (KeyValue keyValue : observation.getAttributes()) {
			AttributeBean attributeBean = dsd.getAttribute(keyValue.getConcept());
			Property attributeProperty = model.createProperty(uriFactory.getURI(attributeBean.getUrn()));
			if (attributeBean.hasCodedRepresentation()) {
				CrossReferenceBean codelistRef = attributeBean.getRepresentation().getRepresentation();
				CodelistBean codelistBean = (CodelistBean) retreivalManager.getMaintainableBean(codelistRef);
				CodeBean codeBean = codelistBean.getCodeById(keyValue.getCode());
				
				if (codeBean != null) {
					Resource codeResource = model.createResource(uriFactory.getURI(codeBean.getUrn()));
					rdfObs.addProperty(attributeProperty, codeResource);
				} else {
					logger.error("Invalid code id: " + keyValue.getCode());
				}
			} else {
				logger.error("unsupported attribute representation");
			}

		}
		
		// get primary measure
		PrimaryMeasureBean primaryMeasureBean = dsd.getPrimaryMeasure();
		Property measureProperty = model.createProperty(uriFactory.getURI(primaryMeasureBean.getUrn()));
		Literal value = model.createTypedLiteral(observation.getObservationValue());
		rdfObs.addProperty(measureProperty, value);
		
		return rdfObs;
	}
}
