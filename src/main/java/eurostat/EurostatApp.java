package eurostat;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
        InputStream dataflows = EurostatApp.class.getResourceAsStream("/eurostat_dataflows/latest");
        // TODO: get this from http://www.ec.europa.eu/eurostat/SDMX/diss-web/rest/dataflow/ESTAT/all/latest
        // get all dataflows from main file
        downloadAllIsoc(dataflows, "isoc_bde15dip", null);
    }

	public void downloadAllIsoc(InputStream dataflows, String filter, OutputStream os) throws Exception {

		ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(dataflows);
		StructureWorkspace workspace = structureParsingManager.parseStructures(dataLocation);
		SdmxBeans beans = workspace.getStructureBeans(true);
		for (MaintainableBean bean : beans.getAllMaintainables()) {
			SDMX_STRUCTURE_TYPE beanType = bean.getStructureType();
			if (bean.getStructureType() == SDMX_STRUCTURE_TYPE.DATAFLOW) {
				if ( bean.getId().startsWith(filter)) {
					logger.info(MessageFormat.format("Found {0}, id={1}, name={2}", beanType, bean.getId(), bean.getName()));
					// replace this with downloadDataset if you want to download only
					try {
					    if(os != null){
						    convertDataset(bean.getId(), os);
                        } else {
                            convertDataset(bean.getId());
                        }
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
        FileOutputStream out = new FileOutputStream(dataset + ".rdf");
	    convertDataset(dataset, out);
		out.close();
	}

	public void convertDataset(String dataset, OutputStream os) throws Exception {
        logger.info("Dataset: " + dataset);
        sdmx2rdf.parse(new InputStream[] { datasetFactory.getDSD(dataset) }, datasetFactory.getData(dataset));
        sdmx2rdf.writeTo(os);
	}
}
