package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.springframework.stereotype.Service;

import sdmx2rdf.vocabularies.Cube;
import sdmx2rdf.vocabularies.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class MeasureConverter extends AbstractConverter<PrimaryMeasureBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.PRIMARY_MEASURE;
	}

	@Override
	public Resource convert(PrimaryMeasureBean bean, Model model) {
		// returns a qb:ComponentSpecification
		logger.debug("Converting " + bean);

		String measureUri = uriFactory.getURI(bean.getUrn());
		Resource componentSpecification = model.createResource(measureUri + "/qbcomponent");
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);
		// IC-6 The only components of a qb:DataStructureDefinition that may be marked as optional, using qb:componentRequired are attributes.
		componentSpecification.addLiteral(Cube.componentRequired, true);

		Resource measureProperty = model.createResource(measureUri);
		componentSpecification.addProperty(Cube.measure, measureProperty);

		measureProperty.addProperty(RDF.type, RDF.Property);
		measureProperty.addProperty(RDF.type, Cube.MeasureProperty);
		measureProperty.addProperty(Skos.notation, bean.getId());
		measureProperty.addProperty(RDFS.label, bean.getId());
		measureProperty.addProperty(Skos.prefLabel, bean.getId());

		CrossReferenceBean conceptRef = bean.getConceptRef();
		if (conceptRef != null) {
			logger.debug("Concept: " + conceptRef);
			Resource conceptRdf = model.createResource(uriFactory.getURI(conceptRef.getTargetUrn()));
			measureProperty.addProperty(Cube.concept, conceptRdf);
		}

		if (bean.hasCodedRepresentation()) {
			CrossReferenceBean codelist = bean.getRepresentation().getRepresentation();
			Resource referencedCodelistRdf = model.createResource(uriFactory.getURI(codelist.getTargetUrn()));
			measureProperty.addProperty(Cube.codeList, referencedCodelistRdf);
		}
		return componentSpecification;
	}

}
