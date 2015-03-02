package demo.sdmxsource.webservice.main.chapter1.builder;

import org.sdmxsource.sdmx.api.model.beans.base.AgencySchemeBean;
import org.sdmxsource.sdmx.api.model.mutable.base.AgencySchemeMutableBean;
import org.sdmxsource.sdmx.sdmxbeans.model.beans.base.AgencySchemeBeanImpl;
import org.springframework.stereotype.Service;

@Service
public class AgencySchemeBuilder {

	
	

	public AgencySchemeBean buildAgencyScheme() {
		AgencySchemeMutableBean mutableBean = AgencySchemeBeanImpl.createDefaultScheme().getMutableInstance();
		mutableBean.createItem("SDMXSOURCE", "Sdmx Source");
		return mutableBean.getImmutableInstance();
	}
	
}
