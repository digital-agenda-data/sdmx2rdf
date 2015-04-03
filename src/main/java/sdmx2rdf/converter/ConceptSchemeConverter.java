package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.ItemBean;
import org.sdmxsource.sdmx.api.model.beans.base.ItemSchemeBean;
import org.sdmxsource.sdmx.api.model.beans.base.TextTypeWrapper;
import org.springframework.stereotype.Service;

import sdmx2rdf.vocabularies.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class ConceptSchemeConverter extends AbstractConverter<ItemSchemeBean<ItemBean>> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.CONCEPT_SCHEME;
	}

	@Override
	public Resource convert(ItemSchemeBean<ItemBean> bean, Model model) {
		logger.debug("Converting " + bean);

		Resource conceptScheme = model.createResource(uriFactory.getURI(bean.getUrn()));
		conceptScheme.addProperty(RDF.type, Skos.ConceptScheme);
		conceptScheme.addProperty(Skos.notation, bean.getId());
		for (TextTypeWrapper name : bean.getNames()) {
			conceptScheme.addProperty(Skos.prefLabel, name.getValue(), name.getLocale());
			conceptScheme.addProperty(RDFS.label, name.getValue(), name.getLocale());
		}
		for (TextTypeWrapper description : bean.getDescriptions()) {
			conceptScheme.addProperty(Skos.definition, description.getValue(), description.getLocale());
			conceptScheme.addProperty(RDFS.comment, description.getValue(), description.getLocale());
		}

		for (ItemBean conceptBean : bean.getItems()) {
			Resource conceptResource = converterFactory.convert(conceptBean, model);
			conceptResource.addProperty(Skos.inScheme, conceptScheme);
			conceptScheme.addProperty(Skos.hasTopConcept, conceptResource);
		}
		return conceptScheme;
	}

}
