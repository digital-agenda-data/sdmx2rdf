package sdmx2rdf.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.sdmxsource.sdmx.api.model.data.KeyValue;
import org.sdmxsource.sdmx.api.model.data.Keyable;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import sdmx.converter.Cube;
import sdmx2rdf.URIFactory;

@Service
public class ObservationConverter {
	
	@Autowired
	protected URIFactory uriFactory;

	@Autowired
	protected ConverterFactory converterFactory;

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	public Resource convert(Resource dataset, Observation observation, Model model) {
		Keyable seriesKey = observation.getSeriesKey();
		
		String uri = dataset.getURI();
		for (KeyValue key : seriesKey.getKey()) {
			uri += key.getCode() + "/";
		}
		
		if (seriesKey.isTimeSeries()) {
			uri += observation.getObsTime();
		}
		
		Resource rdfObs = model.createResource(uri);
		for (KeyValue keyValue : seriesKey.getKey()) {
			DimensionBean dimensionBean = seriesKey.getDataStructure().getDimension(keyValue.getConcept());
			logger.error(dimensionBean);
			if (dimensionBean.hasCodedRepresentation()) {
				CrossReferenceBean codelistRef = dimensionBean.getRepresentation().getRepresentation();
				CodelistBean codelistBean = (CodelistBean) codelistRef.getChildReference();
				logger.error(codelistBean);
				logger.error(keyValue.getCode());
				CodeBean codeBean = codelistBean.getCodeById(keyValue.getCode());
				
				Property dimensionProperty = model.createProperty(uriFactory.getURI(dimensionBean.getUrn()));
				Resource codeResource = model.createResource(uriFactory.getURI(codeBean.getUrn()));
				rdfObs.addProperty(dimensionProperty, codeResource);
			} else {
				logger.warn("unsupported dimension representation");
			}
		}
		
		if (seriesKey.isTimeSeries()) {
			//FIXME(catalinb)
			DimensionBean timeDimension = observation.getSeriesKey().getDataStructure().getTimeDimension();
			Property timeDimensionProperty = model.createProperty(uriFactory.getURI(timeDimension.getUrn()));
			rdfObs.addProperty(timeDimensionProperty, observation.getObsTime());
		} else {
			logger.warn("Only time series supported.");
		}
		
		for (KeyValue entry : observation.getAttributes()) {
			// TODO(catalinb): map this beter

			String attributeID = entry.getConcept();
			AttributeBean attributeBean = seriesKey.getDataStructure().getAttribute(attributeID);
			
			rdfObs.addProperty(Cube.attribute, model.createResource(uriFactory.getURI(attributeBean.getUrn())));
		}
		
		Literal value = model.createTypedLiteral(new Double(Double.parseDouble(observation.getObservationValue())));
		rdfObs.addProperty(Cube.measure, value);
		dataset.addProperty(Cube.observation, rdfObs);
		
		return rdfObs;
	}
}
