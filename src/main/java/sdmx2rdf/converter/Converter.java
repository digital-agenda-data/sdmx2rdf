package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.model.beans.base.SDMXBean;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public interface Converter<T extends SDMXBean> {
	public Resource convert(T bean, Model model);
}
