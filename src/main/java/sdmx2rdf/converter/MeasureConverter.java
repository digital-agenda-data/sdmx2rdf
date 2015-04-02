package sdmx2rdf.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.model.beans.base.IdentifiableBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sdmx.converter.Cube;
import sdmx.converter.Skos;
import sdmx2rdf.URIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Service
public class MeasureConverter implements Converter {

	@Autowired
	private URIFactory uriFactory;

	@Autowired
	ConverterFactory converterFactory;

	private static final Log logger = LogFactory.getLog(DimensionConverter.class);

	@Override
	public Resource convert(IdentifiableBean bean, Model model) {
		// returns a qb:ComponentSpecification
		logger.debug("Converting " + bean);

		String measureUri = uriFactory.getURI(bean.getUrn());
		Resource componentSpecification = model.createResource(measureUri + "/qbcomponent");
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);

		PrimaryMeasureBean measureBean = (PrimaryMeasureBean) bean;

		Resource measureProperty = model.createResource(measureUri);
		componentSpecification.addProperty(Cube.measure, measureProperty);

		measureProperty.addProperty(RDF.type, RDF.Property);
		measureProperty.addProperty(RDF.type, Cube.MeasureProperty);
		measureProperty.addProperty(Skos.notation, measureBean.getId());
		measureProperty.addProperty(RDFS.label, measureBean.getId());

		CrossReferenceBean conceptRef = measureBean.getConceptRef();
		if (conceptRef != null) {
			logger.debug("Concept: " + conceptRef);
			Resource conceptRdf = model.createResource(uriFactory.getURI(conceptRef.getTargetUrn()));
			// conceptRdf.addProperty(RDF.type, Skos.Concept);
			// conceptRdf.addProperty(RDF.type, Sdmx.Concept);
			// conceptRdf.addProperty(RDF.type, Sdmx.IdentityRole);
			measureProperty.addProperty(Cube.concept, conceptRdf);
		}

		if (measureBean.hasCodedRepresentation()) {
			CrossReferenceBean codelist = measureBean.getRepresentation().getRepresentation();
			Resource referencedCodelistRdf = model.createResource(uriFactory.getURI(codelist.getTargetUrn()));
			measureProperty.addProperty(Cube.codeList, referencedCodelistRdf);
		} else {
			logger.debug("No coded representation for " + measureBean);
		}
		return componentSpecification;
	}

}
