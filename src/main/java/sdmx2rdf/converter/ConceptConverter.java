package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptBean;
import org.springframework.stereotype.Service;

import sdmx.converter.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class ConceptConverter extends AbstractConverter<ConceptBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.CONCEPT;
	}

	@Override
	public Resource convert(ConceptBean conceptBean, Model model) {
		Resource conceptResource = model.createResource(uriFactory.getURI(conceptBean.getUrn()));
		conceptResource.addProperty(RDF.type, Skos.Concept);
		conceptResource.addProperty(Skos.notation, conceptBean.getId());
		if (conceptBean.getName() != null) {
			conceptResource.addProperty(Skos.prefLabel, conceptBean.getName());
			conceptResource.addProperty(RDFS.label, conceptBean.getName());
		}
		// conceptResource.addProperty(RDF.type, Sdmx.IdentityRole);
		if (conceptBean.getDescription() != null) {
			conceptResource.addProperty(Skos.definition, conceptBean.getDescription());
		}
		return conceptResource;
	}
}
