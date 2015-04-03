package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.springframework.stereotype.Service;

import sdmx2rdf.vocabularies.Cube;
import sdmx2rdf.vocabularies.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class AttributeConverter extends AbstractConverter<AttributeBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.DATA_ATTRIBUTE;
	}

	@Override
	public Resource convert(AttributeBean bean, Model model) {
		// returns a qb:ComponentSpecification
		logger.debug("Converting " + bean);

		String attributeUri = uriFactory.getURI(bean.getUrn());
		Resource componentSpecification = model.createResource(attributeUri + "/qbcomponent");
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);
		if ("Mandatory".equals(bean.getAssignmentStatus())) {
			componentSpecification.addLiteral(Cube.componentRequired, true);
		} else {
			componentSpecification.addLiteral(Cube.componentRequired, false);
		}

		Resource attributeProperty = model.createResource(attributeUri);
		componentSpecification.addProperty(Cube.attribute, attributeProperty);

		attributeProperty.addProperty(RDF.type, RDF.Property);
		attributeProperty.addProperty(RDF.type, Cube.AttributeProperty);
		attributeProperty.addProperty(Skos.notation, bean.getId());
		attributeProperty.addProperty(Skos.prefLabel, bean.getId());
		attributeProperty.addProperty(RDFS.label, bean.getId());

		CrossReferenceBean conceptRef = bean.getConceptRef();
		if (conceptRef != null) {
			logger.debug("Concept: " + conceptRef);
			Resource conceptRdf = model.createResource(uriFactory.getURI(conceptRef.getTargetUrn()));
			attributeProperty.addProperty(Cube.concept, conceptRdf);
		}

		if (bean.hasCodedRepresentation()) {
			CrossReferenceBean codelist = bean.getRepresentation().getRepresentation();
			Resource referencedCodelistRdf = model.createResource(uriFactory.getURI(codelist.getTargetUrn()));
			attributeProperty.addProperty(Cube.codeList, referencedCodelistRdf);
		} else {
			logger.warn("Representation type:" + bean.getRepresentation().getTextFormat().getTextType());
			// TODO(catalinb): handle non coded representation
		}

		switch (bean.getAttachmentLevel()) {
		case DATA_SET:
			componentSpecification.addProperty(Cube.componentAttachment, Cube.DataSet);
			break;
		case GROUP:

			// TODO
			break;
		case OBSERVATION:
			break;
		case DIMENSION_GROUP:
			bean.getDimensionReferences();
			break;
		}
		// TODO: does AttributeRelationship has a correspondent in qb ?
		return componentSpecification;
	}
}
