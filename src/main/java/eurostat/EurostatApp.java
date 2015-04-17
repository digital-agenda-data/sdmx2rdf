package eurostat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import sdmx2rdf.DatasetFactory;
import sdmx2rdf.Sdmx2Rdf;

@Component
public class EurostatApp {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ReadableDataLocationFactory rdlFactory;

	@Autowired
	StructureParsingManager structureParsingManager;

	@Autowired
	Sdmx2Rdf sdmx2rdf;

	@Autowired
	DatasetFactory datasetFactory;

	private final Log logger = LogFactory.getLog(getClass());

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(EurostatAppContextConfiguration.class);
		EurostatApp app = ctx.getBean(EurostatApp.class);
		app.downloadAllIsoc();
	}

	public void downloadAllIsoc() throws Exception {
		// TODO: get this from http://www.ec.europa.eu/eurostat/SDMX/diss-web/rest/dataflow/ESTAT/all/latest
		// get all dataflows from main file
		InputStream dataflows = EurostatApp.class.getResourceAsStream("/eurostat_dataflows/latest");
		ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(dataflows);
		StructureWorkspace workspace = structureParsingManager.parseStructures(dataLocation);
		SdmxBeans beans = workspace.getStructureBeans(true);
		for (MaintainableBean bean : beans.getAllMaintainables()) {
			SDMX_STRUCTURE_TYPE beanType = bean.getStructureType();
			if (bean.getStructureType() == SDMX_STRUCTURE_TYPE.DATAFLOW) {
				if ( bean.getId().startsWith("isoc_bde15dip")) {
					logger.info(MessageFormat.format("Found {0}, id={1}, name={2}", beanType, bean.getId(), bean.getName()));
					// replace this with downloadDataset if you want to download only
					try {
						convertDataset(bean.getId());
					} catch (MalformedURLException e) {
						logger.error("Failed to download data:" + e.getMessage());
					}
				}
			}
		}
	}

	public void downloadDataset(String dataset) throws Exception {
		// download only
		datasetFactory.getDSD(dataset);
		datasetFactory.getData(dataset);
	}
	
	public void convertDataset(String dataset) throws Exception {
		sdmx2rdf.parse(new InputStream[] { datasetFactory.getDSD(dataset) }, datasetFactory.getData(dataset));
		FileOutputStream out = new FileOutputStream(dataset + ".rdf");
		sdmx2rdf.writeTo(out);
		out.close();
	}
}
