package sdmx2rdf.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
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
public class AttributeConverter implements Converter {

	@Autowired
	private URIFactory uriFactory;

	@Autowired
	ConverterFactory converterFactory;

	private static final Log logger = LogFactory.getLog(DimensionConverter.class);

	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
			// returns a qb:ComponentSpecification
			logger.debug("Converting " + bean);

			String attributeUri = uriFactory.getURI(bean.getUrn());
			Resource componentSpecification = model.createResource(attributeUri + "/qbcomponent");
			componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);

			AttributeBean attributeBean = (AttributeBean) bean;

			Resource attributeProperty = model.createResource(attributeUri);
			componentSpecification.addProperty(Cube.attribute, attributeProperty);

			attributeProperty.addProperty(RDF.type, RDF.Property);
			attributeProperty.addProperty(RDF.type, Cube.AttributeProperty);
			attributeProperty.addProperty(Skos.notation, attributeBean.getId());
			attributeProperty.addProperty(RDFS.label, attributeBean.getId());

			CrossReferenceBean conceptRef = attributeBean.getConceptRef();
			if (conceptRef != null) {
				logger.debug("Concept: " + conceptRef);
				Resource conceptRdf = model.createResource(uriFactory.getURI(conceptRef.getTargetUrn()));
				// conceptRdf.addProperty(RDF.type, Skos.Concept);
				// conceptRdf.addProperty(RDF.type, Sdmx.Concept);
				// conceptRdf.addProperty(RDF.type, Sdmx.IdentityRole);
				attributeProperty.addProperty(Cube.concept, conceptRdf);
			}

			if (attributeBean.hasCodedRepresentation()) {
				CrossReferenceBean codelist = attributeBean.getRepresentation().getRepresentation();
				Resource referencedCodelistRdf = model.createResource(uriFactory.getURI(codelist.getTargetUrn()));
				attributeProperty.addProperty(Cube.codeList, referencedCodelistRdf);
			} else {
				logger.warn("Representation type:" + attributeBean.getRepresentation().getTextFormat().getTextType());
				// TODO(catalinb): handle non coded representation
			}
			
			return componentSpecification;
	}

}
