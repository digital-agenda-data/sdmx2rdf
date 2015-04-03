package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.SDMXBean;
import org.sdmxsource.sdmx.api.model.header.DatasetHeaderBean;
import org.sdmxsource.sdmx.api.model.header.DatasetStructureReferenceBean;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

@Service
public class DatasetHeaderConverter extends AbstractConverter<SDMXBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.DATASET;
	}


	@Override
	public Resource convert(SDMXBean sdmxbean, Model model) {
		logger.debug("Converting " + sdmxbean);
		DatasetHeaderBean bean = (DatasetHeaderBean) sdmxbean; 
		// returns a skos:ConceptScheme and associated concepts

		Resource dataset = model.createResource(uriFactory.getURI(bean.getDatasetId()));
		dataset.addProperty(Cube.dataSet, Cube.DataSet);
		dataset.addProperty(Cube.dataSet, Cube.DataSet);
		DatasetStructureReferenceBean dsdRefBean = bean.getDataStructureReference();
		//dataset.addProperty(Cube.structure, uriFactory.getURI(dsdRefBean.get));

		return dataset;
	}

}
