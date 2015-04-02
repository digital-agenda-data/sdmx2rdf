
package sdmx2rdf.converter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx2rdf.TestSdmxFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

@Service
public class ConverterFactory implements Converter {

	@Autowired
	DSDConverter dsdConverter;
	
	@Autowired
	CodeListConverter codeListConverter;
	
	@Autowired
	ConceptSchemeConverter conceptSchemeConverter;
	
	@Autowired
	DimensionConverter dimensionConverter;
	
	@Autowired
	AttributeConverter attributeConverter;
	
	@Autowired
	MeasureConverter measureConverter;
	

	private static final Log logger = LogFactory.getLog(TestSdmxFactory.class);

	public Converter getConverter(SDMX_STRUCTURE_TYPE beanType) {

		switch (beanType) {
			case DSD:
				return dsdConverter;
			case CODE_LIST:
				return codeListConverter;
			case CONCEPT_SCHEME:
				return conceptSchemeConverter;
			case TIME_DIMENSION:
			case DIMENSION:
				return dimensionConverter;
			case DATA_ATTRIBUTE:
				return attributeConverter;
			case MEASURE_DIMENSION:
			case PRIMARY_MEASURE:
				return measureConverter;
			default:
		}
		logger.warn("Converter not found for bean type " + beanType);
		return null;
	}
	
	public Converter getConverter(IdentifiableBean bean) {
		return getConverter(bean.getStructureType());
	}
	
	public Resource convert(IdentifiableBean bean, Model model) {
		Converter converter = getConverter(bean);
		if ( converter != null ) {
			return converter.convert(bean, model);
		}
		return null;
	}


}
