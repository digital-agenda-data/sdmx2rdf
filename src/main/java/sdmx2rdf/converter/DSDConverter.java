package sdmx2rdf.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx2rdf.URIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


@Service
public class DSDConverter implements Converter {

	private static final Log logger = LogFactory.getLog(DSDConverter.class);

	@Autowired
	private URIFactory uriFactory;
	
	@Autowired
	ConverterFactory converterFactory;  
	
	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
		//returns a qb:DataStructureDefinition
		
		logger.debug("Converting " + bean);
		
		Resource dsdResource = model.createResource(uriFactory.getURI(bean.getUrn()));
		//dsdResource.addProperty(RDF.type, Sdmx.DataStructureDefinition);
		dsdResource.addProperty(RDF.type, Cube.DataStructureDefinition);
		
		DataStructureBean dsd = (DataStructureBean) bean;
		for (DimensionBean dimension : dsd.getDimensionList().getDimensions()) {
			Resource componentSpecification = converterFactory.convert(dimension, model);
			if ( componentSpecification != null ) {
				dsdResource.addProperty(Cube.component, componentSpecification);
			}
		}
		
		for (AttributeBean attribute : dsd.getAttributeList().getAttributes()) {
			Resource componentSpecification = converterFactory.convert(attribute, model);
			if ( componentSpecification != null ) {
				dsdResource.addProperty(Cube.component, componentSpecification);
			}
		}
		
		PrimaryMeasureBean primaryMeasure = dsd.getMeasureList().getPrimaryMeasure();
		Resource componentSpecification = converterFactory.convert(primaryMeasure, model);
		if ( componentSpecification != null ) {
			dsdResource.addProperty(Cube.component, componentSpecification);
		}
		
		return dsdResource;
	}
}
