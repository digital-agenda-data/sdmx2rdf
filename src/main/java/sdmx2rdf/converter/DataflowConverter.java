package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.TextTypeWrapper;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataflowBean;
import org.springframework.stereotype.Service;

import sdmx2rdf.vocabularies.Cube;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class DataflowConverter extends AbstractConverter<DataflowBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.DATAFLOW;
	}

	@Override
	public Resource convert(DataflowBean bean, Model model) {
		Resource dataset = model.createResource(uriFactory.getURI(bean.getUrn()));
		dataset.addProperty(RDF.type, Cube.dataSet);
		dataset.addProperty(DCTerms.identifier, bean.getId());
		if ( bean.getName() == null ) {
			dataset.addProperty(DCTerms.title, bean.getId());
		}

		for (TextTypeWrapper name : bean.getNames()) {
			dataset.addProperty(RDFS.label, name.getValue(), name.getLocale());
			dataset.addProperty(DCTerms.title, name.getValue(), name.getLocale());
		}
		dataset.addProperty(Cube.structure, uriFactory.getURI(bean.getDataStructureRef().getTargetUrn()));
		// TODO: other info from header

		return dataset;
	}
}
