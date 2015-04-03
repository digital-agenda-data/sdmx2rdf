package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.ItemBean;
import org.sdmxsource.sdmx.api.model.beans.base.TextTypeWrapper;
import org.springframework.stereotype.Service;

import sdmx2rdf.vocabularies.Sdmx;
import sdmx2rdf.vocabularies.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class ConceptConverter extends AbstractConverter<ItemBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.CONCEPT;
	}

	@Override
	public Resource convert(ItemBean conceptBean, Model model) {
		Resource conceptResource = model.createResource(uriFactory.getURI(conceptBean.getUrn()));
		conceptResource.addProperty(RDF.type, Sdmx.Concept);
		conceptResource.addProperty(RDF.type, Skos.Concept);

		conceptResource.addProperty(Skos.notation, conceptBean.getId());
		
		for (TextTypeWrapper name : conceptBean.getNames()) {
			conceptResource.addProperty(Skos.prefLabel, name.getValue(), name.getLocale());
			conceptResource.addProperty(RDFS.label, name.getValue(), name.getLocale());
		}
		for (TextTypeWrapper description : conceptBean.getDescriptions()) {
			conceptResource.addProperty(Skos.definition, description.getValue(), description.getLocale());
			conceptResource.addProperty(RDFS.comment, description.getValue(), description.getLocale());
		}
		return conceptResource;
	}
}
