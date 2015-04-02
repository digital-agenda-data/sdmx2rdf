package sdmx2rdf;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.sdmxsource.sdmx.api.model.beans.reference.MaintainableRefBean;
import org.sdmxsource.sdmx.api.model.beans.reference.StructureReferenceBean;
import org.sdmxsource.sdmx.util.beans.reference.StructureReferenceBeanImpl;
import org.springframework.stereotype.Component;

import sdmx.converter.Cube;
import sdmx.converter.Sdmx;
import sdmx.converter.Skos;

@Component
public class URIFactory {
	
	private Map<String, String> nsPrefixMap;
	
	public String getURI(String structureUrn) {
		StructureReferenceBean sRef = new StructureReferenceBeanImpl(structureUrn);
		MaintainableRefBean mRef = sRef.getMaintainableReference();
		String structureType = sRef.getMaintainableStructureType().getUrnClass();
		String agencyId = mRef.getAgencyId();
		String maintId = mRef.getMaintainableId();
		String version = mRef.getVersion();
		StringBuilder result = new StringBuilder(getBaseURI(agencyId)).append("qb/").append(structureType).append("/").append(maintId)
				.append("/").append(version);
		String suffix = "";
		if (sRef.getIdentifiableIds() != null) {
			//String identStructureType = sRef.getTargetReference().getUrnClass();
			for (String srefId : sRef.getIdentifiableIds()) {
				result.append("/").append(srefId);
			}
		}
		return result.append(suffix).toString();
	}
	
	public Map<String, String> getNSMap() {
		return nsPrefixMap;
	}
	
	public String getBaseURI(String agencyId) {
		String prefix = nsPrefixMap.get(agencyId.toLowerCase());
		if ( prefix != null ) {
			return prefix;
		}
		return nsPrefixMap.get("defaultNS");
	}

	@PostConstruct
	public void init() {
		nsPrefixMap = new HashMap<String, String>();
		nsPrefixMap.put("sdmx", Sdmx.NS);
		nsPrefixMap.put("qb", Cube.NS);
		nsPrefixMap.put("skos", Skos.NS);
		nsPrefixMap.put("owl", "http://www.w3.org/2002/07/owl#");
		nsPrefixMap.put("estat", "http://ec.europa.eu/eurostat/");
		nsPrefixMap.put("defaultNS", "http://semantic.digital-agenda-data.eu/");
	}
}
