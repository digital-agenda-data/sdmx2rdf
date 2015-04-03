package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptSchemeBean;
import org.springframework.stereotype.Service;

import sdmx.converter.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


@Service
public class ConceptSchemeConverter extends AbstractConverter<ConceptSchemeBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.CONCEPT_SCHEME;
	}

	@Override
	public Resource convert(ConceptSchemeBean bean, Model model) {
		logger.debug("Converting " + bean);
		
		Resource conceptScheme = model.createResource(uriFactory.getURI(bean.getUrn()));
		conceptScheme.addProperty(RDF.type, Skos.ConceptScheme);
		conceptScheme.addProperty(Skos.notation, bean.getId());
		conceptScheme.addProperty(Skos.prefLabel, bean.getName());
		conceptScheme.addProperty(RDFS.label, bean.getName());
		if ( bean.getDescription() != null ) {
			conceptScheme.addProperty(Skos.definition, bean.getDescription());
		}
		
		for (ConceptBean conceptBean : bean.getItems()) {
			Resource conceptResource = model.createResource(uriFactory.getURI(conceptBean.getUrn()));
			conceptResource.addProperty(RDF.type, Skos.Concept);
			conceptResource.addProperty(Skos.notation, conceptBean.getId());
			conceptResource.addProperty(Skos.prefLabel, conceptBean.getName());
			conceptResource.addProperty(RDFS.label, conceptBean.getName());
			if ( conceptBean.getDescription() != null ) {
				conceptResource.addProperty(Skos.definition, conceptBean.getDescription());
			}
			conceptResource.addProperty(RDFS.label, conceptBean.getName());
			conceptResource.addProperty(Skos.inScheme, conceptScheme);
			conceptScheme.addProperty(Skos.hasTopConcept, conceptResource);
		}
		return conceptScheme;
	}

}
