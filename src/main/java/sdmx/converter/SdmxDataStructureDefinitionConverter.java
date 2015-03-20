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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

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
		
			Resource component;
			if (structureType == SDMX_STRUCTURE_TYPE.CODE_LIST) {
				component = parseCodelist((CodelistBean) currentMaintainable);
			} else {
				System.out.println("UNKOWN: " + structureType);	
				component = model.createResource(dsdResource.getURI() + "unknown/" + currentMaintainable.hashCode());
			}
			dsdResource.addProperty(Cube.component, component);
		}
		
		try {
			File file = new File("DSD_output2");
			FileOutputStream output = new FileOutputStream(file);
			model.write(output, "RDF/XML-ABBREV");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseDataStructure(DataStructureBean bean) {
		
	}

	private Resource parseCodelist(CodelistBean bean) {
		SDMX_STRUCTURE_TYPE structureType = bean.getStructureType();
		
		Resource componentSpecification = model.createResource(dsdResource.getURI() + "codelist/" + bean.hashCode());
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);
		
		return componentSpecification;
	}
	
	public Model GetModel() {
		return model;
	}

	public Resource getDSD() {
		return dsdResource;
	}
	
}
