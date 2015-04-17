package sdmx2rdf.converter;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.base.TextFormatBean;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

@Service
public class TextFormatConverter extends AbstractConverter<TextFormatBean> {

	@Override
	protected SDMX_STRUCTURE_TYPE getStructureType() {
		return SDMX_STRUCTURE_TYPE.TEXT_FORMAT;
	}

	@Override
	public Resource convert(TextFormatBean bean, Model model) {
		// returns a xsd:Resource (data type)
		logger.debug("Converting " + bean);
		switch (bean.getTextType()) {
		case BIG_INTEGER:
		case LONG:
			return XSD.xlong;
		case INTEGER:
			return XSD.xint;
		case SHORT:
			return XSD.xshort;
		case DECIMAL:
			return XSD.decimal;
		case FLOAT:
			return XSD.xfloat;
		case DOUBLE:
			return XSD.xdouble;
		case BOOLEAN:
			return XSD.xboolean;
		case DATE:
			return XSD.date;
		case DATE_TIME:
		case TIME:
			return XSD.dateTime;
			// case ALPHA_NUMERIC:
			// case STRING:
			// case ALPHA:
		case OBSERVATIONAL_TIME_PERIOD:
		case BASIC_TIME_PERIOD:
			return null;
		case YEAR_MONTH:
			return XSD.gYearMonth;
		case MONTH_DAY:
			return XSD.gMonthDay;
			// case DURATION:
			// case URI:
			// case TIMESPAN:
			// case COUNT:
			// case DATA_SET_REFERENCE:
			// case INCLUSIVE_VALUE_RANGE:
			// case EXCLUSIVE_VALUE_RANGE:
			// case INCREMENTAL:
			// case KEY_VALUES:
		case TIME_PERIOD:
			return null;
		case GREGORIAN_DAY:
			return XSD.gDay;
		case GREGORIAN_TIME_PERIOD:
			return null;
		case GREGORIAN_YEAR:
			return XSD.gYear;
		case GREGORIAN_YEAR_MONTH:
			return XSD.gYearMonth;
		case REPORTING_DAY:
		case REPORTING_MONTH:
		case REPORTING_QUARTER:
		case REPORTING_SEMESTER:
		case REPORTING_TIME_PERIOD:
		case REPORTING_TRIMESTER:
		case REPORTING_WEEK:
		case REPORTING_YEAR:
		case STANDARD_TIME_PERIOD:
			return null;
			//case TIMES_RANGE:
			// case IDENTIFIABLE_REFERENCE:
		default:
			return XSD.xstring;
		}
	}
}
