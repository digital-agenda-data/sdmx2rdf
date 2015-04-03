package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx.converter.Sdmx;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


@Service
public class DSDConverter extends AbstractConverter<DataStructureBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.DSD;
	}

	@Override
	public Resource convert(DataStructureBean bean, Model model) {
		//returns a qb:DataStructureDefinition
		logger.debug("Converting " + bean);
		Resource dsdResource = model.createResource(uriFactory.getURI(bean.getUrn()));
		//dsdResource.addProperty(RDF.type, Sdmx.DataStructureDefinition);
		dsdResource.addProperty(RDF.type, Cube.DataStructureDefinition);
		dsdResource.addProperty(RDF.type, Sdmx.DataStructureDefinition);

		for (DimensionBean dimension : bean.getDimensionList().getDimensions()) {
			Resource componentSpecification = converterFactory.convert(dimension, model);
			if ( componentSpecification != null ) {
				dsdResource.addProperty(Cube.component, componentSpecification);
			}
		}
		
		for (AttributeBean attribute : bean.getAttributeList().getAttributes()) {
			Resource componentSpecification = converterFactory.convert(attribute, model);
			if ( componentSpecification != null ) {
				dsdResource.addProperty(Cube.component, componentSpecification);
			}
		}
		
		PrimaryMeasureBean primaryMeasure = bean.getMeasureList().getPrimaryMeasure();
		Resource componentSpecification = converterFactory.convert(primaryMeasure, model);
		if ( componentSpecification != null ) {
			dsdResource.addProperty(Cube.component, componentSpecification);
		}
		
		return dsdResource;
	}
}
