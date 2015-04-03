package sdmx2rdf.converter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.SDMXBean;
import org.sdmxsource.sdmx.api.model.header.DatasetHeaderBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

@Service
public class ConverterFactory {

	@Autowired
	protected DatasetHeaderConverter datasetHeaderConverter;
	
	@Autowired
	protected ObservationConverter observationConverter;
	
	protected Map<SDMX_STRUCTURE_TYPE, Converter<? extends SDMXBean>> converterMap = new HashMap<SDMX_STRUCTURE_TYPE, Converter<? extends SDMXBean>>();

	protected final Log logger = LogFactory.getLog(this.getClass());

	public Converter getConverter(SDMX_STRUCTURE_TYPE beanType) {
		Converter converter = converterMap.get(beanType);
		if ( converter != null ) {
			return converter;
		}
		logger.warn("Converter not found for bean type " + beanType);
		return null;
	}

	public Converter getConverter(SDMXBean bean) {
		return getConverter(bean.getStructureType());
	}
	
	// hack: instantiate converter for the dataset header bean
	public DatasetHeaderConverter getDatasetHeaderConverter() {
		return datasetHeaderConverter;
	}
	
	public ObservationConverter getObservationConverter() {
		return observationConverter;
	}

	public Resource convert(SDMXBean bean, Model model) {
		Converter converter = getConverter(bean.getStructureType());
		if (converter != null) {
			return converter.convert(bean, model);
		}
		logger.warn("Converter not found for bean type " + bean.getStructureType());
		return null;
	}

	public void register(SDMX_STRUCTURE_TYPE type, Converter<? extends SDMXBean> converter) {
		this.converterMap.put(type, converter);
	}
}
