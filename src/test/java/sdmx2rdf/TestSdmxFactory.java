package sdmx2rdf;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.factory.WriteableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.dataparser.manager.DataReaderManager;
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
	@ComponentScan({ "org.sdmxsource", "sdmx.converter", "sdmx2rdf" })
	static class ContextConfiguration {

		@Bean
		public DataflowFactory dataflowFactory() {
			return new DataflowFactory();
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
	private DataflowFactory dataflowFactory;

	@Autowired
	private ReadableDataLocationFactory rdlFactory;
	
	@Autowired
	private StructureParsingManager structureParsingManager;
	
	@Autowired
	private DataReaderManager dataReaderManager;

	@Autowired
	private Sdmx2RdfConverter mainConverter;
	
	@Autowired
	URIFactory uriManager;

	private static final Log logger = LogFactory.getLog(TestSdmxFactory.class);

	@Test
	public void test1() throws IOException {
		String dataset = "isoc_ic_biski";

		mainConverter.parse(dataflowFactory.getDSD(dataset), dataflowFactory.getData(dataset));
		//mainConverter.parse(dataflowFactory.getDSD(dataset), null);
		
		FileOutputStream out = new FileOutputStream(dataset + ".rdf");
		mainConverter.writeTo(out);
		out.close();
	}

	public void testURIMapper() {
		logger.info(uriManager.getURI("urn:sdmx:org.sdmx.infomodel.datastructure.DataStructure=ESTAT:DSD_isoc_ic_biski(1.0)"));
		logger.info(uriManager.getURI("urn:sdmx:org.sdmx.infomodel.datastructure.Dimension=ESTAT:DSD_isoc_ic_biski(1.0).FREQ"));
		logger.info(uriManager.getURI("urn:sdmx:org.sdmx.infomodel.codelist.Codelist=ESTAT:CL_INDIC_IS(1.0)"));
		logger.info(uriManager.getURI("urn:sdmx:org.sdmx.infomodel.codelist.Code=ESTAT:CL_INDIC_IS(1.0).IC_SK"));
		logger.info(uriManager.getURI("urn:sdmx:org.sdmx.infomodel.conceptscheme.ConceptScheme=ESTAT:CS_DSD_isoc_ic_biski(1.0)"));
		logger.info(uriManager.getURI("urn:sdmx:org.sdmx.infomodel.conceptscheme.Concept=ESTAT:CS_DSD_isoc_ic_biski(1.0).FREQ"));
	}
}
