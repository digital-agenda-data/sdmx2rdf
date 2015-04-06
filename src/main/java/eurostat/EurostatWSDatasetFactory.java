package eurostat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import sdmx2rdf.DatasetFactory;

@Component
public class EurostatWSDatasetFactory implements DatasetFactory {
	
	String cache_dir = "data";
	String dsd_pattern = "http://ec.europa.eu/eurostat/SDMX/diss-web/rest/datastructure/ESTAT/DSD_{0}";
	// When using WS, some of the data files are not provided synchronously, see TODO below
	String data_pattern = "http://ec.europa.eu/eurostat/SDMX/diss-web/rest/data/{0}";
	
	private final Log logger = LogFactory.getLog(getClass());
	
	
	@Override
	public InputStream getDSD(String dataset) throws IOException {
		File file = new File(cache_dir, dataset + "_dsd.xml");
		if (!file.exists()) {
			URL source = new URL(MessageFormat.format(dsd_pattern, dataset));
			FileUtils.copyURLToFile(source, file);
		}
		return new FileInputStream(file);
	}

	@Override
	public InputStream getData(String dataset) throws IOException {
		File file = new File(cache_dir, dataset + "_data.sdmx.zip");
		if (!file.exists()) {
			URL source = new URL(MessageFormat.format(data_pattern, dataset));
			FileUtils.copyURLToFile(source, file);
			
			// TODO: handle this 
			/*
			<footer:Footer>
				<footer:Message code="413" severity="Infomation">
					<common:Text xml:lang="en">Due to the large query the response will be written to a file which will be locatedunder URL: http://ec.europa.eu/eurostat/SDMX/diss-web/file/T7AMEFPLIzx2MSLh</common:Text>
					<common:Text xml:lang="en">http://ec.europa.eu/eurostat/SDMX/diss-web/file/T7AMEFPLIzx2MSLh</common:Text>
					<common:Text xml:lang="en">Please check the location periodic every 5 minutes or at your preference.</common:Text>
				</footer:Message>
			</footer:Footer>
			*/
		}
		return new FileInputStream(file);
	}

	@Override
	public InputStream getDataflow(String dataset) {
		throw new RuntimeException("Not implemented");
	}

	@PostConstruct
	public void init() {
		File cache_dir_file = new File(cache_dir);
		if ( !cache_dir_file.exists() ) {
			boolean created = cache_dir_file.mkdirs();
			if (!created) {
				logger.error("Cannot create cache dir: " + cache_dir_file.getAbsolutePath());
			}
		}
	}
}
