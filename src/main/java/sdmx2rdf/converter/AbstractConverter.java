package sdmx2rdf.converter;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.SDMXBean;
import org.springframework.beans.factory.annotation.Autowired;

import sdmx2rdf.URIFactory;

public abstract class AbstractConverter<T extends SDMXBean> implements Converter<T> {

	@Autowired
	protected URIFactory uriFactory;

	@Autowired
	protected ConverterFactory converterFactory;

	protected final Log logger = LogFactory.getLog(this.getClass());

	@PostConstruct
	protected void register() {
		converterFactory.register(this.getStructureType(), this);
	}

	protected abstract SDMX_STRUCTURE_TYPE getStructureType();
}
