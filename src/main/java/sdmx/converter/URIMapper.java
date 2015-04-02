package sdmx.converter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class URIMapper {
	Map<String, String> prefixMap;
	Map<String, String> nsPrefixMap;

	String rootNS;
	
	public URIMapper(String defaultRoot) {
		prefixMap = new TreeMap<String, String>();
		nsPrefixMap = new TreeMap<String, String>();
		
		this.rootNS = defaultRoot;
		
		// adding some defaults for now
		// TODO(catalinb): load from json
		
		nsPrefixMap.put("sdmx", Sdmx.NS);
		nsPrefixMap.put("qb", Cube.NS);
		nsPrefixMap.put("skos", Skos.NS);
		nsPrefixMap.put("owl", "http://www.w3.org/2002/07/owl#");

		nsPrefixMap.put("dad", rootNS);
		nsPrefixMap.put("dad-dimension", rootNS + "dimension/");
		nsPrefixMap.put("dad-measure", rootNS + "measure/");
	}
	
	public String getRoot() {
		return rootNS;
	}
	
	public Set<Entry<String, String>> getNSPrefixes() {
		return nsPrefixMap.entrySet();
	}
}
