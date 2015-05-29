package sdmx2rdf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.constants.TIME_FORMAT;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.reference.MaintainableRefBean;
import org.sdmxsource.sdmx.api.model.beans.reference.StructureReferenceBean;
import org.sdmxsource.sdmx.api.model.data.Observation;
import org.sdmxsource.sdmx.util.beans.reference.StructureReferenceBeanImpl;
import org.springframework.stereotype.Component;

import sdmx2rdf.vocabularies.Cube;
import sdmx2rdf.vocabularies.Sdmx;
import sdmx2rdf.vocabularies.Skos;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.OWL;

@Component
public class URIFactory {

	private Map<String, String> nsPrefixMap;
	
	private final Log logger = LogFactory.getLog(getClass());

	public String getURI(String structureUrn) {
		StructureReferenceBean sRef = new StructureReferenceBeanImpl(structureUrn);
		MaintainableRefBean mRef = sRef.getMaintainableReference();
		String structureType = sRef.getMaintainableStructureType().getUrnClass();
		String agencyId = mRef.getAgencyId();
		String maintId = mRef.getMaintainableId();
		String version = mRef.getVersion();
		StringBuilder result = new StringBuilder(getBaseURI(agencyId)).append("qb/").append(structureType).append("/")
				.append(maintId).append("/").append(version);
		List<String> ignored = Arrays.asList(new String[] { 
				SDMX_STRUCTURE_TYPE.DIMENSION_DESCRIPTOR.getFixedId(),
				SDMX_STRUCTURE_TYPE.ATTRIBUTE_DESCRIPTOR.getFixedId(),
				SDMX_STRUCTURE_TYPE.MEASURE_DESCRIPTOR.getFixedId() });
		if (sRef.getIdentifiableIds() != null) {
			String identStructureType = sRef.getTargetReference().getUrnClass();
			result.append("/").append(identStructureType);
			for (String srefId : sRef.getIdentifiableIds()) {
				if (!ignored.contains(srefId)) {
					result.append("/").append(srefId);
				}
			}
		}
		return result.toString();
	}

	public Map<String, String> getNSMap() {
		return nsPrefixMap;
	}

	public String getBaseURI(String agencyId) {
		String prefix = nsPrefixMap.get(agencyId.toLowerCase());
		if (prefix != null) {
			return prefix;
		}
		String defaultNS = "http://semantic.digital-agenda-data.eu/";
		nsPrefixMap.put("dad", defaultNS);
		return defaultNS;
	}

	@PostConstruct
	public void init() {
		nsPrefixMap = new HashMap<String, String>();
		nsPrefixMap.put("sdmx", Sdmx.NS);
		nsPrefixMap.put("qb", Cube.NS);
		nsPrefixMap.put("skos", Skos.NS);
		nsPrefixMap.put("owl", OWL.NS);
		nsPrefixMap.put("dcterms", DCTerms.NS);
		nsPrefixMap.put("dctypes", DCTypes.NS);
		nsPrefixMap.put("foaf", FOAF.NS);
		nsPrefixMap.put("estat", "http://ec.europa.eu/eurostat/");
		nsPrefixMap.put("time", "http://reference.data.gov.uk/doc/");
	}

	public String getMissingCodeURI(Observation observation, String code) {
		DataStructureBean dataStructure = observation.getSeriesKey().getDataStructure();
		String base = getURI(dataStructure.getUrn());
		base += "qb/Codelist/UNKNOWN_CL/1.0/Code/" + code;
		return base;
	}
}
