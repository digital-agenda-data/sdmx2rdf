package sdmx.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.model.beans.base.SDMXBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.reference.CrossReferenceBean;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SdmxDataStructureDefinitionConverter {

	private SdmxBeans sdmxBeans;
	private URIMapper uriMap;
	Model model = ModelFactory.createDefaultModel();
	Resource dsdResource;
	
	public SdmxDataStructureDefinitionConverter(SdmxBeans sdmxBeans, URIMapper uriMap) {
		this.sdmxBeans = sdmxBeans;
		this.uriMap = uriMap;
		
		
		String id = "UNKNOWN_ID";
		// we extract the id from the DataStructureBean
		// TODO(catalinb): this is probably not the best choice
		for (DataStructureBean dsd :sdmxBeans.getDataStructures()) {
			id = dsd.getId();
			break;
		}
		System.out.println("ID: " + id);
		
		dsdResource = model.createResource(uriMap.getRoot() + "def/dsd/" + id +"/");
		
		for (Entry<String, String> entry : uriMap.getNSPrefixes()) {
			model.setNsPrefix(entry.getKey(), entry.getValue());
		}
		
		dsdResource.addProperty(RDF.type, Sdmx.DataStructureDefinition);
	}
	
	public void parseDataStructure() {		
		// TODO Primary Measure
		//dsd.addProperty(Sdmx.primaryMeasure, );

		for (MaintainableBean currentMaintainable : sdmxBeans.getAllMaintainables()) {
			SDMX_STRUCTURE_TYPE structureType = currentMaintainable.getStructureType();
		
			if (structureType == SDMX_STRUCTURE_TYPE.CODE_LIST) {
				parseCodelist((CodelistBean) currentMaintainable);
			} else if (structureType == SDMX_STRUCTURE_TYPE.DSD) {
				DataStructureBean bean = (DataStructureBean) currentMaintainable;
				for (DimensionBean dimension : bean.getDimensionList().getDimensions()) {
					parseDimension(dimension);
				}
			} else {
				System.out.println("UNKOWN: " + structureType);	
				Resource component = model.createResource(dsdResource.getURI() + "unknown/" + currentMaintainable.hashCode());
				dsdResource.addProperty(Cube.component, component);
			}	
		}
	}
	
	private void parseDimension(DimensionBean dimension) {
		
		Resource componentSpecification = model.createResource(dsdResource.getURI() + "dimension/" + dimension.getId());
		// TODO(catalinb):we can probably skip this one
		Resource dimensionRdf = model.createResource(dsdResource.getURI() + "property/" + dimension.getId());
		dimensionRdf.addProperty(RDF.type, RDF.Property);
		dimensionRdf.addProperty(RDF.type, Cube.DimensionProperty);
		
		CrossReferenceBean concept = dimension.getConceptRef();
		Resource conceptRdf = model.createResource(dsdResource.getURI() + "concept/" + concept.getFullId());
		

		conceptRdf.addProperty(RDF.type, Skos.Concept);
		conceptRdf.addProperty(RDF.type, Sdmx.Concept);
		conceptRdf.addProperty(RDF.type, Sdmx.IdentityRole);
		dimensionRdf.addProperty(Cube.concept, conceptRdf);
		
		if (dimension.hasCodedRepresentation()) {
			CrossReferenceBean codelist = dimension.getRepresentation().getRepresentation();
			
			// FIXME(catalinb): this needs to match the codelist resource generated when parsing the DSD
			Resource referencedCodelistRdf = model.createResource(dsdResource.getURI() + "codelist/" + codelist.getMaintainableId());
			dimensionRdf.addProperty(Cube.codeList, referencedCodelistRdf);
		} else {
			System.out.println("Representation type:" + dimension.getRepresentation().getTextFormat().getTextType());
			// TODO(catalinb): handle non coded representation
		}
		
		componentSpecification.addProperty(Cube.dimension, dimensionRdf);
		dsdResource.addProperty(Cube.component, componentSpecification);
	}

	private void parseCodelist(CodelistBean bean) {
		SDMX_STRUCTURE_TYPE structureType = bean.getStructureType();
		
		Resource componentSpecification = model.createResource(dsdResource.getURI() + "codelist/" + bean.getId());
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);
		componentSpecification.addProperty(RDF.type, Skos.ConceptScheme);
		
		for (CodeBean codeBean : bean.getItems()) {
			Resource codeRdf = model.createResource(dsdResource.getURI() + "code/" + bean.getId() + "#" + codeBean.getId());
			codeRdf.addProperty(RDF.type, Skos.Concept);
			codeRdf.addProperty(Skos.notation, codeBean.getId());
			codeRdf.addProperty(RDFS.label, codeBean.getName());
			if (codeBean.getDescription() != null) {
				codeRdf.addProperty(RDFS.comment, codeBean.getDescription());
			}
			
			componentSpecification.addProperty(Skos.hasTopConcept, codeRdf);
		}
		
		dsdResource.addProperty(Cube.component, componentSpecification);
	}	
	
	public Model GetModel() {
		return model;
	}

	public Resource getDSD() {
		return dsdResource;
	}
	
}
