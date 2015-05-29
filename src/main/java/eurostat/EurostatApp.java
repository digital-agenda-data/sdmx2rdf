package eurostat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.exception.SdmxSyntaxException;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import sdmx2rdf.DatasetFactory;
import sdmx2rdf.Sdmx2Rdf;

@Component
public class EurostatApp {
	
	public enum Result {
		FOUND,
		NOT_FOUND,
		DATASET_TOO_LARGE,
		ERROR
	};

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
	private Map<String, MaintainableBean> knownDatasets = new HashMap<String, MaintainableBean>();
	private String cache_dir = "sdmx_cache";

	public Result fetchAndConvertDataset(InputStream dataflows, String dataset, boolean forceRefresh, OutputStream os) {
		
		if (forceRefresh || knownDatasets.isEmpty()) {
			logger.info("Repopulating dataset map!");
			knownDatasets.clear();
			
			ReadableDataLocation dataLocation = rdlFactory.getReadableDataLocation(dataflows);
			StructureWorkspace workspace = structureParsingManager.parseStructures(dataLocation);
			SdmxBeans beans = workspace.getStructureBeans(true);
			
			for (MaintainableBean bean : beans.getAllMaintainables()) {
				if (bean.getStructureType() != SDMX_STRUCTURE_TYPE.DATAFLOW) {
					continue;
				}
				
				logger.info(MessageFormat.format("Found {0}, id={1}, name={2}", bean.getStructureType(), bean.getId(), bean.getName()));
				knownDatasets.put(bean.getId(), bean);
			}
		}

		MaintainableBean targetDataflow = knownDatasets.get(dataset);
		if (targetDataflow == null) {
			return Result.NOT_FOUND;
		}
		
		if (!forceRefresh && getCachedDataset(dataset, os)) {
			return Result.FOUND;
		}
		
		try {
			logger.info("Dataset: " + targetDataflow.getId());
			sdmx2rdf.initModel();
	        sdmx2rdf.parse(new InputStream[] { datasetFactory.getDSD(targetDataflow.getId(), forceRefresh) },
	        			   datasetFactory.getData(targetDataflow.getId(), forceRefresh),
	        			   targetDataflow);
	        sdmx2rdf.validate();
	        sdmx2rdf.writeTo(os);
		} catch (ConcurrentModificationException e) {
			// this can happen when downloading the same file multiple times because we have only one
			// instance of sdmx2rdf converter.
			logger.warn(e, e);
			return Result.ERROR;
		} catch (MalformedURLException e) {
			logger.warn(e, e);
			return Result.DATASET_TOO_LARGE;
		} catch (SdmxSyntaxException syntaxException) {
			// sdmx parser. Check if we have a footer
			try {
				EurostatErrorParser errorParser = new EurostatErrorParser(datasetFactory.getData(targetDataflow.getId(), forceRefresh));
				errorParser.parse();
				logger.warn(MessageFormat.format("Code={0}. Severity={1}. Description={2}",
						errorParser.getCode(), errorParser.getSeverity(), errorParser.getCodeDescription()));
				for (String text : errorParser.getText()) {
					logger.warn(text);
				}
				
				return Result.ERROR;
			} catch (Exception e) {
				logger.warn(e, e);
				return Result.ERROR;
			}
		} catch (Exception e) {
			logger.warn(e, e);
			return Result.ERROR;
		}
		
		try {
			// write to cache
			File cacheFile = new File(cache_dir, dataset + ".rdf");
			sdmx2rdf.writeTo(new FileOutputStream(cacheFile));
		} catch (Exception e) {
			logger.warn(e, e);
		}
		     
        return Result.FOUND;
	}
	
	public boolean getCachedDataset(String dataset, OutputStream os) {
		File file = new File(cache_dir, dataset + ".rdf");
		if (!file.exists()) {
			return false;
		}
		if (file.length() == 0) {
			logger.warn("Dataset cache file is empty.");
			return false;
		}
		
		try {
			IOUtils.copy(new FileInputStream(file), os);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	@PostConstruct
	public void init() {
		File sdmxCache = new File(cache_dir);
		if (!sdmxCache.exists()) {
			if (!sdmxCache.mkdir()) {
				logger.error("Cannot create sdmx cache dir: " + sdmxCache.getAbsolutePath());
			}
		}
	}
}
