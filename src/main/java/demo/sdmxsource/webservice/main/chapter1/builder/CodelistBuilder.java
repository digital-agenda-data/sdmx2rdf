package demo.sdmxsource.webservice.main.chapter1.builder;

import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.mutable.codelist.CodelistMutableBean;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.codelist.CodelistMutableBeanImpl;
import org.springframework.stereotype.Service;

/**
 * Create the codelists used for the demo project
 */
@Service
public class CodelistBuilder {

	public CodelistBean buildCountryCodelist() {
		CodelistMutableBean codelistMutable = new CodelistMutableBeanImpl();
		codelistMutable.setAgencyId("SDMXSOURCE");
		codelistMutable.setId("CL_COUNTRY");
		codelistMutable.setVersion("1.0");
		codelistMutable.addName("en", "Country");
		
		codelistMutable.createItem("UK", "United Kingdom");
		codelistMutable.createItem("FR", "France");
		codelistMutable.createItem("DE", "Germany");
		
		return codelistMutable.getImmutableInstance();
	}

	
	public CodelistBean buildIndicatorCodelist() {
		CodelistMutableBean codelistMutable = new CodelistMutableBeanImpl();
		codelistMutable.setAgencyId("SDMXSOURCE");
		codelistMutable.setId("CL_INDICATOR");
		codelistMutable.setVersion("1.0");
		codelistMutable.addName("en", "World Development Indicators");
		
		codelistMutable.createItem("E", "Environment");
		codelistMutable.createItem("E_A", "Aggriculture land").setParentCode("E");
		codelistMutable.createItem("E_P", "Population").setParentCode("E");
		

		codelistMutable.createItem("H", "Health");
		codelistMutable.createItem("H_B", "Birth Rate").setParentCode("H");
		codelistMutable.createItem("H_C", "Children (0-14) living with HIV").setParentCode("H");
		
		return codelistMutable.getImmutableInstance();
	}
}
