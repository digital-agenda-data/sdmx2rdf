package sdmx2rdf.converter;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.SDMXBean;
import org.sdmxsource.sdmx.api.model.header.DatasetHeaderBean;
import org.sdmxsource.sdmx.api.model.header.DatasetStructureReferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx2rdf.URIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

@Service
public class DatasetHeaderConverter {
	
	@Autowired
	protected URIFactory uriFactory;

	@Autowired
	protected ConverterFactory converterFactory;

	protected final Log logger = LogFactory.getLog(this.getClass());

	public Resource convert(DatasetHeaderBean bean, Model model) {
		logger.debug("Converting " + bean);
		// returns a skos:ConceptScheme and associated concepts

		logger.error(bean.getDatasetId());
		Resource dataset = model.createResource(uriFactory.getURI(bean.getDatasetId()));
		
		dataset.addProperty(Cube.dataSet, Cube.DataSet);
		dataset.addProperty(Cube.dataSet, Cube.DataSet);
		DatasetStructureReferenceBean dsdRefBean = bean.getDataStructureReference();

		return dataset;
	}

}
