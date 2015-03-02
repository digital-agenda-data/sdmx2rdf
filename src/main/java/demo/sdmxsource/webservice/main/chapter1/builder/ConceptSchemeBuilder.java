package demo.sdmxsource.webservice.main.chapter1.builder;

import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptSchemeBean;
import org.sdmxsource.sdmx.api.model.mutable.conceptscheme.ConceptSchemeMutableBean;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.conceptscheme.ConceptSchemeMutableBeanImpl;
import org.springframework.stereotype.Service;

@Service
public class ConceptSchemeBuilder {

	public ConceptSchemeBean buildConceptScheme() {
		ConceptSchemeMutableBean conceptSchemeMutable = new ConceptSchemeMutableBeanImpl();
		conceptSchemeMutable.setAgencyId("SDMXSOURCE");
		conceptSchemeMutable.setId("CONCEPTS");
		conceptSchemeMutable.setVersion("1.0");
		conceptSchemeMutable.addName("en", "Web Service Concepts");
		
		conceptSchemeMutable.createItem("COUNTRY", "Country");
		conceptSchemeMutable.createItem("INDICATOR", "World Development Indicators");
		conceptSchemeMutable.createItem("TIME", "Time");
		conceptSchemeMutable.createItem("OBS_VALUE", "Observation Value");
		
		return conceptSchemeMutable.getImmutableInstance();
	}
	
}
