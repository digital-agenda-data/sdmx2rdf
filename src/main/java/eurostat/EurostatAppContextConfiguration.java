package eurostat;

import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.factory.WriteableDataLocationFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.web.client.RestTemplate;

import sdmx2rdf.DatasetFactory;

@Configuration
@EnableSpringConfigured
@ComponentScan({ "org.sdmxsource", "eurostat", "sdmx2rdf" })
public class EurostatAppContextConfiguration {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public ReadableDataLocationFactory readableDataLocationFactory() {
		return new org.sdmxsource.util.factory.SdmxSourceReadableDataLocationFactory();
	}

	@Bean
	public WriteableDataLocationFactory writeableDataLocationFactory() {
		return new org.sdmxsource.util.factory.SdmxSourceWriteableDataLocationFactory();
	}
	
	@Bean
	public DatasetFactory datasetFactory() {
		return new EurostatBulkDatasetFactory();
	}
}
