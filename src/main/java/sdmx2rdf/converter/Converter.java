package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public interface Converter {

	public Resource convert(IdentifiableBean bean, Model model);
}
