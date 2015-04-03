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
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptSchemeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.AttributeListBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DimensionBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.PrimaryMeasureBean;
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
				
				// TODO(catalinb): MeasureListBean doesn't seem to hold a list of measures
				PrimaryMeasureBean primaryMeasure = bean.getMeasureList().getPrimaryMeasure();
				parsePrimaryMeasure(primaryMeasure);
				
				// TODO(catalinb): attributes
				parseAttributeBean(bean.getAttributeList());
				
			} else if (structureType == SDMX_STRUCTURE_TYPE.CONCEPT_SCHEME) {
				parseConceptScheme((ConceptSchemeBean) currentMaintainable);
			} else {
				System.out.println("UNKOWN: " + structureType);	
				Resource component = model.createResource(dsdResource.getURI() + "unknown/" + currentMaintainable.hashCode());
				dsdResource.addProperty(Cube.component, component);
			}	
		}
	}
	
	private void parseConceptScheme(ConceptSchemeBean bean) {

		Resource conceptScheme = model.createResource(dsdResource.getURI() + "def/scheme/" + bean.getId());
		// TODO(catalinb): do we need other properties?
		conceptScheme.addProperty(RDF.type, Skos.ConceptScheme);
		
		for (ConceptBean conceptBean : bean.getItems()) {
			Resource conceptResource = model.createResource(dsdResource.getURI() + "def/concept/" + conceptBean.getId());
			conceptResource.addProperty(RDF.type, Skos.Concept);
			conceptResource.addProperty(RDF.type, Sdmx.IdentityRole);
			conceptResource.addProperty(RDF.type, Sdmx.Concept);
			
			conceptResource.addProperty(RDFS.label, conceptBean.getName());
			if (conceptBean.getDescription() != null) {
				conceptResource.addProperty(RDFS.comment, conceptBean.getDescription());
			}
			
			conceptResource.addProperty(Skos.inScheme, conceptScheme);
			conceptScheme.addProperty(Skos.hasTopConcept, conceptResource);
		}
		
		dsdResource.addProperty(Cube.component, conceptScheme);
	}
	
	private void parseAttributeBean(AttributeListBean attributeListBean) {
		/*
		<qb:component>
		<qb:ComponentSpecification rdf:about="http://semantic.digital-agenda-data.eu/def/dsd/scoreboard/attribute/flag">
			<qb:attribute>
				<!-- Flag property -->
				<rdf:Description rdf:about="http://semantic.digital-agenda-data.eu/def/property/flag">
					<rdf:type rdf:resource="http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"/>
					<rdf:type rdf:resource="http://purl.org/linked-data/cube#AttributeProperty"/>
					<rdf:type rdf:resource="http://purl.org/linked-data/cube#CodedProperty"/>
					<rdfs:label xml:lang="en">Flag</rdfs:label>
					<rdfs:comment xml:lang="en">Values from Eurostat codelist: http://eurostat.linked-statistics.org/dic/flags</rdfs:comment>
					<skos:notation>flag</skos:notation>
					<skos:inScheme rdf:resource="http://semantic.digital-agenda-data.eu/def/scheme"/>
					<qb:concept>
						<rdf:Description rdf:about="http://semantic.digital-agenda-data.eu/def/concept/flag">
							<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
							<rdf:type rdf:resource="http://purl.org/linked-data/sdmx#Concept"/>
							<rdf:type rdf:resource="http://purl.org/linked-data/sdmx#ConceptRole"/>
							<rdfs:label xml:lang="en">Flag</rdfs:label>
							<rdfs:comment xml:lang="en">Optional flag for the measured value</rdfs:comment>
							<skos:inScheme rdf:resource="http://semantic.digital-agenda-data.eu/def/scheme"/>
							<rdfs:seeAlso rdf:resource="http://semantic.digital-agenda-data.eu/def/property/flag"/>	
						</rdf:Description>
					</qb:concept>
					<qb:codeList rdf:resource="http://eurostat.linked-statistics.org/dic/flags"/>
				</rdf:Description>
			</qb:attribute>
		</qb:ComponentSpecification>
	</qb:component>
	<qb:component>
	*/
		for (AttributeBean attributeBean : attributeListBean.getAttributes()) {
			Resource componentSpecification = model.createResource(dsdResource.getURI() + "def/attribute/" + attributeBean.getId());
			componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);
			
			Resource property = model.createResource(dsdResource.getURI() + "def/property/" + attributeBean.getId());
			property.addProperty(RDF.type, RDF.Property);
			property.addProperty(RDF.type, Cube.AttributeProperty);
			if (attributeBean.hasCodedRepresentation()) {
				property.addProperty(RDF.type, Cube.CodedProperty);
				
				//CrossReferenceBean codelist = property.getRepresentation().getRepresentation();
				Resource codelistRdf = model.createResource();
			} else {
				// TODO is there a Cube.TextProperty?
			}
			
			CrossReferenceBean concept = attributeBean.getConceptRef();
			Resource conceptRdf = model.createResource(dsdResource.getURI() + "def/concept/" + concept.getFullId());
			property.addProperty(Cube.concept, conceptRdf);
			
			componentSpecification.addProperty(Cube.attribute, property);
			dsdResource.addProperty(Cube.component, componentSpecification);
		}
	}
	
	private void parsePrimaryMeasure(PrimaryMeasureBean primaryMeasure) {
		Resource componentSpecification  = model.createResource(dsdResource.getURI() + "measure/");
		componentSpecification.addProperty(RDF.type, Cube.ComponentSpecification);
		Resource measureRdf = model.createResource(dsdResource.getURI() + "measure/" + primaryMeasure.getId());
		measureRdf.addProperty(RDF.type, Cube.MeasureProperty);
		measureRdf.addProperty(RDF.type, RDF.Property);
		
		// TODO(catalinb): better resourceGetter
		CrossReferenceBean concept = primaryMeasure.getConceptRef();
		Resource conceptRdf = model.createResource(dsdResource.getURI() + "def/concept/" + concept.getFullId());
		
		measureRdf.addProperty(Cube.concept, conceptRdf);
		
		componentSpecification.addProperty(Cube.measure, measureRdf);
		dsdResource.addProperty(Cube.component, componentSpecification);
	}
	
	private void parseDimension(DimensionBean dimension) {
		
		Resource componentSpecification = model.createResource(dsdResource.getURI() + "dimension/" + dimension.getId());
		// TODO(catalinb):we can probably skip this one
		Resource dimensionRdf = model.createResource(dsdResource.getURI() + "property/" + dimension.getId());
		dimensionRdf.addProperty(RDF.type, RDF.Property);
		dimensionRdf.addProperty(RDF.type, Cube.DimensionProperty);
		
		CrossReferenceBean concept = dimension.getConceptRef();
		Resource conceptRdf = model.createResource(dsdResource.getURI() + "def/concept/" + concept.getFullId());
		

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
