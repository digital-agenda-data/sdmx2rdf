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

	public boolean fetchAndConvertDataset(InputStream dataflows, String dataset, OutputStream os) throws Exception {

		ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(dataflows);
		StructureWorkspace workspace = structureParsingManager.parseStructures(dataLocation);
		SdmxBeans beans = workspace.getStructureBeans(true);
		
		for (MaintainableBean bean : beans.getAllMaintainables()) {
			SDMX_STRUCTURE_TYPE beanType = bean.getStructureType();
			if (bean.getStructureType() == SDMX_STRUCTURE_TYPE.DATAFLOW) {
				if ( bean.getId().equals(dataset)) {
					logger.info(MessageFormat.format("Found {0}, id={1}, name={2}", beanType, bean.getId(), bean.getName()));
					convertDataset(bean, os);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void convertDataset(MaintainableBean dataflow, OutputStream os) throws Exception {
        logger.info("Dataset: " + dataflow.getId());
        sdmx2rdf.parse(new InputStream[] { datasetFactory.getDSD(dataflow.getId()) }, datasetFactory.getData(dataflow.getId()), dataflow);
        sdmx2rdf.writeTo(os);
	}
}
