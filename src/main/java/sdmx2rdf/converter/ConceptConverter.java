package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx.converter.Sdmx;
import sdmx.converter.Skos;
import sdmx2rdf.URIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class ConceptConverter implements Converter {
	
	@Autowired
	private URIFactory uriFactory;
	
	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
		ConceptBean conceptBean = (ConceptBean) bean;
		Resource conceptResource = model.createResource(uriFactory.getURI(bean.getUrn()));
		
		conceptResource.addProperty(RDF.type, Skos.Concept);
		conceptResource.addProperty(RDF.type, Sdmx.IdentityRole);
		conceptResource.addProperty(RDF.type, Sdmx.Concept);
		
		conceptResource.addProperty(RDFS.label, conceptBean.getName());
		if (conceptBean.getDescription() != null) {
			conceptResource.addProperty(RDFS.comment, conceptBean.getDescription());
		}
		
		return conceptResource;
	}

}
