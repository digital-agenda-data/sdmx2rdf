package sdmx2rdf.converter;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.ExceptionCode;
import org.sdmxsource.sdmx.api.constants.TIME_FORMAT;
import org.sdmxsource.sdmx.api.exception.SdmxSemmanticException;
import org.springframework.stereotype.Service;

/*
 * Duplicating functionality from sdmxsource DateUtil to
 * fix some small parsing errors.
 */
@Service
public class TimeFormatConverter {
	
	//Regular expressions 
	private static final String xmlDatePatternString =
			"[0-9][0-9][0-9][0-9]-" +
					"((01|03|05|07|08|10|12)-((0[1-9])|(1[0-9])|(2[0-9])|3[0-1])" +
					"|02-((0[1-9])|(1[0-9])|(2[0-9]))" +
					"|(04|06|09|11)-((0[1-9])|(1[0-9])|(2[0-9])|30))";
	private static final String xmlTimePatternString = "([0-1][0-9]|2[0-3]):([0-5][0-9])(:[0-5][0-9])?(.[0-9]*)?(Z|((\\+|-)([0-1][0-9]|2[0-3]):([0-5][0-9])))?";
	
	private final Log logger = LogFactory.getLog(getClass());
	
	public enum FORMAT {
		GREGORIAN_YEAR("[0-9][0-9][0-9][0-9]"),
		GREGORIAN_MONTH("[0-9][0-9][0-9][0-9]-(0[1-9]|1[0-2])"),
		GREGORIAN_DAY(xmlDatePatternString),
		DATE_TIME(xmlDatePatternString + "T" + xmlTimePatternString),
		REPORTING_YEAR("[0-9][0-9][0-9][0-9]-A1"), // only one possible value: A1
		REPORTING_SEMSTER("[0-9][0-9][0-9][0-9]-S[1-2]"),
		REPORTING_TRIMESTER("[0-9][0-9][0-9][0-9]-T[1-3]"),
		REPORTING_QUARTER("[0-9][0-9][0-9][0-9]-Q[1-4]"),
		REPORTING_MONTH("[0-9][0-9][0-9][0-9]-M(0[1-9]|1[0-2])"),
		REPORTING_WEEK("[0-9][0-9][0-9][0-9]-W(0[1-9]|[1-4][0-9]|5[0-3])"),
		REPORTING_DAY("[0-9][0-9][0-9][0-9]-D[0-9][0-9][0-9]"); // Note: This format accepts invalid inputs.
		
		FORMAT(String fmt) {
			this.pattern = Pattern.compile(fmt);
		}
		
		private Pattern pattern;
		
		public Pattern getPattern() {
			return pattern;
		}
	}

	public FORMAT getTimeFormatOfDate(String dateStr) {
		if (dateStr==null) {
			throw new IllegalArgumentException("Could not determine date format, date null");	
		} 

		if(dateStr.endsWith("Z")) {
			dateStr = dateStr.substring(0, dateStr.length()-1);
		}
		
		for (FORMAT format : FORMAT.values()) {
			boolean match = format.getPattern().matcher(dateStr).matches();
			if (match) {
				if (format == FORMAT.REPORTING_DAY) {
					String[] parts = dateStr.split("-D");
					try {
						int day = Integer.parseInt(parts[1]);
						if (day < 0 || day > 365) {
							continue;
						}
					} catch (NumberFormatException e) {
						logger.warn("Invalid day number format.");
						continue;
					}
				}
				
				return format;
			}
		}
		
		throw new SdmxSemmanticException(ExceptionCode.INVALID_DATE_FORMAT, dateStr);
	}
	String[] parts;
	public String getTimeBaseURI(String value) {
		String baseURI;
		switch(getTimeFormatOfDate(value)) {
		case GREGORIAN_YEAR:
			baseURI = "http://reference.data.gov.uk/id/year/";
			break;
		case GREGORIAN_MONTH:
			baseURI = "http://reference.data.gov.uk/id/month/";
			break;
		case GREGORIAN_DAY:
			baseURI = "http://reference.data.gov.uk/id/day/";
			break;
		case DATE_TIME:
			baseURI = "http://reference.data.gov.uk/id/second/";
			break;
		case REPORTING_SEMSTER:
			parts = value.split("-S");
			value = parts[0] + "-H" + parts[1];
			baseURI = "http://reference.data.gov.uk/id/half/";
			
			break;
		case REPORTING_QUARTER:
			baseURI = "http://reference.data.gov.uk/id/quarter/";
			break;
		case REPORTING_YEAR:
			baseURI = "http://reference.data.gov.uk/id/year/";
			value = value.split("-A")[0];
			break;
		case REPORTING_MONTH:
			baseURI = "http://reference.data.gov.uk/id/month/";
			parts = value.split("-M");
			value = parts[0] + "-" + parts[1];
			break;
		case REPORTING_TRIMESTER:
		case REPORTING_WEEK:
		case REPORTING_DAY:
		default:
			logger.error("Unsupported format for :" + value);
			return null;
		}
		return baseURI + value;
	}
}
