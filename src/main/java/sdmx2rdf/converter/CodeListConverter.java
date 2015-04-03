package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.springframework.stereotype.Service;

import sdmx.converter.Skos;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class CodeListConverter extends AbstractConverter<CodelistBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.CODE_LIST;
	}

	@Override
	public Resource convert(CodelistBean bean, Model model) {
		logger.debug("Converting " + bean);
		// returns a skos:ConceptScheme and associated concepts

		Resource codelistResource = model.createResource(uriFactory.getURI(bean.getUrn()));
		codelistResource.addProperty(RDF.type, Skos.ConceptScheme);
		codelistResource.addProperty(Skos.prefLabel, bean.getName());
		codelistResource.addProperty(RDFS.label, bean.getName());
		codelistResource.addProperty(Skos.notation, bean.getId());
		if (bean.getDescription() != null) {
			codelistResource.addProperty(Skos.definition, bean.getDescription());
		}

		for (CodeBean codeBean : bean.getItems()) {
			Resource codeResource = model.createResource(uriFactory.getURI(codeBean.getUrn()));
			codeResource.addProperty(RDF.type, Skos.Concept);
			codeResource.addProperty(Skos.notation, codeBean.getId());
			codeResource.addProperty(Skos.prefLabel, codeBean.getName());
			codeResource.addProperty(RDFS.label, codeBean.getName());
			if (codeBean.getDescription() != null) {
				codeResource.addProperty(Skos.definition, codeBean.getDescription());
			}
			codeResource.addProperty(Skos.inScheme, codelistResource);
			codelistResource.addProperty(Skos.hasTopConcept, codeResource);
		}
		return codelistResource;
	}

}
