package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.TextFormatBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx.converter.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

@Service
public class DimensionConverter extends AbstractConverter<DimensionBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.DIMENSION;
	}

	@Override
	public Resource convert(DimensionBean bean, Model model) {
		// returns a qb:ComponentSpecification
		logger.debug("Converting " + bean);

		String dimensionUri = uriFactory.getURI(bean.getUrn());
		Resource componentSpecification = model.createResource(dimensionUri + "/qbcomponent");
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);

		Resource dimensionProperty = model.createResource(dimensionUri);
		componentSpecification.addProperty(Cube.dimension, dimensionProperty);
		componentSpecification.addLiteral(Cube.order, bean.getPosition());
		// IC-6 The only components of a qb:DataStructureDefinition that may be marked as optional, using qb:componentRequired are attributes.
		componentSpecification.addLiteral(Cube.componentRequired, true);

		dimensionProperty.addProperty(RDF.type, RDF.Property);
		dimensionProperty.addProperty(RDF.type, Cube.DimensionProperty);
		dimensionProperty.addProperty(Skos.notation, bean.getId());
		dimensionProperty.addProperty(Skos.prefLabel, bean.getId());
		dimensionProperty.addProperty(RDFS.label, bean.getId());

		CrossReferenceBean conceptRef = bean.getConceptRef();
		if (conceptRef != null) {
			Resource conceptRdf = model.createResource(uriFactory.getURI(conceptRef.getTargetUrn()));
			dimensionProperty.addProperty(Cube.concept, conceptRdf);
		}

		if (bean.hasCodedRepresentation()) {
			CrossReferenceBean codelist = bean.getRepresentation().getRepresentation();
			Resource referencedCodelistRdf = model.createResource(uriFactory.getURI(codelist.getTargetUrn()));
			dimensionProperty.addProperty(Cube.codeList, referencedCodelistRdf);
			// IC-5. Concept dimensions have code lists
			dimensionProperty.addProperty(RDFS.range, Skos.Concept);
		} else {
			// IC-4 Every dimension declared in a qb:DataStructureDefinition must have a declared rdfs:range
			TextFormatBean textFormatBean = bean.getRepresentation().getTextFormat();
			Resource xsdFormatResource = null;
			if (textFormatBean != null) {
				// convert between TextTypeType.* to rdfs
				xsdFormatResource = converterFactory.convert(textFormatBean, model);
			}
			if (xsdFormatResource != null) {
				dimensionProperty.addProperty(RDFS.range, xsdFormatResource);
			} else {
				dimensionProperty.addProperty(RDFS.range, XSD.anyURI);
			}
		}

		return componentSpecification;
	}

}
