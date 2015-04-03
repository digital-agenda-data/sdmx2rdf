package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.springframework.stereotype.Service;

@Service
public class TimeDimensionConverter extends DimensionConverter {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.TIME_DIMENSION;
	}

}
