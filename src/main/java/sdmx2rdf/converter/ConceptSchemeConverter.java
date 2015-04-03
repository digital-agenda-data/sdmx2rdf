package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptSchemeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx.converter.Skos;
import sdmx2rdf.URIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


@Service
public class ConceptSchemeConverter implements Converter {

	@Autowired
	private URIFactory uriFactory;
	
	@Autowired
	ConverterFactory converterFactory;
	
	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
		String conceptSchemeURI = uriFactory.getURI(bean.getUrn());
		Resource conceptScheme = model.createResource(conceptSchemeURI);
		conceptScheme.addProperty(RDF.type, Skos.ConceptScheme);
		
		ConceptSchemeBean conceptSchemeBean = (ConceptSchemeBean) bean;
		
		for (ConceptBean conceptBean : conceptSchemeBean.getItems()) {
			Resource concept = converterFactory.getConverter(conceptBean).convert(conceptBean, model);
			
			concept.addProperty(Skos.hasTopConcept, conceptScheme);
			conceptScheme.addProperty(Skos.hasTopConcept, concept);
		}
		
		return conceptScheme;
	}
}
