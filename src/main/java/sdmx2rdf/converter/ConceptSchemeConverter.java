package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


@Service
public class ConceptSchemeConverter implements Converter {

	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
		return null;
	}

}
