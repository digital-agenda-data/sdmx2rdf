package sdmx2rdf.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx.converter.Skos;
import sdmx2rdf.URIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class DimensionConverter implements Converter {

	@Autowired
	private URIFactory uriFactory;

	@Autowired
	ConverterFactory converterFactory;

	private static final Log logger = LogFactory.getLog(DimensionConverter.class);

	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
		// returns a qb:ComponentSpecification
		logger.debug("Converting " + bean);

		String dimensionUri = uriFactory.getURI(bean.getUrn());
		Resource componentSpecification = model.createResource(dimensionUri + "/qbcomponent");
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);

		DimensionBean dimensionBean = (DimensionBean) bean;

		Resource dimensionProperty = model.createResource(dimensionUri);
		componentSpecification.addProperty(Cube.dimension, dimensionProperty);
		componentSpecification.addLiteral(Cube.order, dimensionBean.getPosition());

		dimensionProperty.addProperty(RDF.type, RDF.Property);
		dimensionProperty.addProperty(RDF.type, Cube.DimensionProperty);
		dimensionProperty.addProperty(Skos.notation, dimensionBean.getId());
		dimensionProperty.addProperty(RDFS.label, dimensionBean.getId());

		CrossReferenceBean conceptRef = dimensionBean.getConceptRef();
		if (conceptRef != null) {
			logger.debug("Concept: " + conceptRef);
			Resource conceptRdf = model.createResource(uriFactory.getURI(conceptRef.getTargetUrn()));
			// conceptRdf.addProperty(RDF.type, Skos.Concept);
			// conceptRdf.addProperty(RDF.type, Sdmx.Concept);
			// conceptRdf.addProperty(RDF.type, Sdmx.IdentityRole);
			dimensionProperty.addProperty(Cube.concept, conceptRdf);
		}

		if (dimensionBean.hasCodedRepresentation()) {
			CrossReferenceBean codelist = dimensionBean.getRepresentation().getRepresentation();
			Resource referencedCodelistRdf = model.createResource(uriFactory.getURI(codelist.getTargetUrn()));
			dimensionProperty.addProperty(Cube.codeList, referencedCodelistRdf);
		} else {
			logger.warn("Representation type:" + dimensionBean.getRepresentation().getTextFormat().getTextType());
			// TODO(catalinb): handle non coded representation
		}
		return componentSpecification;
	}
}
