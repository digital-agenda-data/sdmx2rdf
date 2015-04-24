package sdmx2rdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.factory.WriteableDataLocationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestSdmxFactory {

	@Configuration
	@EnableSpringConfigured
	@ComponentScan({ "org.sdmxsource", "sdmx2rdf" })
	static class ContextConfiguration {

		@Bean
		public DatasetFactory datasetFactory() {
			return new TestDatasetFactory();
		}

		@Bean
		public ReadableDataLocationFactory readableDataLocationFactory() {
			return new org.sdmxsource.util.factory.SdmxSourceReadableDataLocationFactory();
		}

		@Bean
		public WriteableDataLocationFactory writeableDataLocationFactory() {
			return new org.sdmxsource.util.factory.SdmxSourceWriteableDataLocationFactory();
		}
	}

	@Autowired
	private DatasetFactory datasetFactory;

	@Autowired
	private Sdmx2Rdf sdmx2rdf;

	@Autowired
	private URIFactory uriFactory;

	private final Log logger = LogFactory.getLog(getClass());

	@Test
	public void test1() throws Exception {
		String dataset = "isoc_ic_biski";

		sdmx2rdf.parse(
				new InputStream[] { datasetFactory.getDataflow(dataset), datasetFactory.getDSD(dataset) },
				datasetFactory.getData(dataset));

		FileOutputStream out = new FileOutputStream(dataset + ".rdf");
		sdmx2rdf.writeTo(out);
		out.close();
	}
	
	@Test
	public void test_date_formats() throws Exception {
		String dataset = "isoc_custom_dates";

		sdmx2rdf.parse(
				new InputStream[] { datasetFactory.getDataflow(dataset), datasetFactory.getDSD(dataset) },
				datasetFactory.getData(dataset));

		FileOutputStream out = new FileOutputStream(dataset + ".rdf");
		sdmx2rdf.writeTo(out);
		out.close();
	}

	@Test
	public void test_isoc_ic_bisec() throws Exception {
		String dataset = "isoc_ic_biski";

		sdmx2rdf.parse(
				new InputStream[] { datasetFactory.getDSD(dataset) },
				datasetFactory.getData(dataset));
		FileOutputStream out = new FileOutputStream(dataset + ".rdf");
		sdmx2rdf.writeTo(out);
		out.close();
	}

	public void test2() throws IOException {
		InputStream inputStream = this.getClass().getResourceAsStream("/sdmx_test/sdmx_test.xml");
		sdmx2rdf.parse(new InputStream[] { inputStream }, null);
		FileOutputStream out = new FileOutputStream("sdmx_test.rdf");
		sdmx2rdf.writeTo(out);
		out.close();
	}

	public void testURIMapper() {
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.datastructure.DataStructure=ESTAT:DSD_isoc_ic_biski(1.0)"));
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.datastructure.Dimension=ESTAT:DSD_isoc_ic_biski(1.0).FREQ"));
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.datastructure.TimeDimension=ESTAT:DSD_isoc_ic_biski(1.0).TIME_PERIOD"));
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.codelist.Codelist=ESTAT:CL_INDIC_IS(1.0)"));
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.codelist.Code=ESTAT:CL_INDIC_IS(1.0).IC_SK"));
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.conceptscheme.ConceptScheme=ESTAT:CS_DSD_isoc_ic_biski(1.0)"));
		logger.info(uriFactory.getURI("urn:sdmx:org.sdmx.infomodel.conceptscheme.Concept=ESTAT:CS_DSD_isoc_ic_biski(1.0).FREQ"));
	}
}
