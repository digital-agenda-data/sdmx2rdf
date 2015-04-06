package eurostat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import sdmx2rdf.DatasetFactory;

@Component
public class EurostatBulkDatasetFactory implements DatasetFactory {

	String cache_dir = "data";
	String bulk_data_pattern = "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing?sort=1&file=data/{0}.sdmx.zip";

	private final Log logger = LogFactory.getLog(getClass());

	@Override
	public InputStream getDSD(String dataset) throws IOException {
		File file = new File(cache_dir, dataset + ".dsd.xml");
		if (!file.exists()) {
			downloadDataset(dataset);
		}
		return new FileInputStream(file);
	}

	@Override
	public InputStream getData(String dataset) throws IOException {
		File file = new File(cache_dir, dataset + ".sdmx.xml");
		if (!file.exists()) {
			downloadDataset(dataset);
		}
		return new FileInputStream(file);
	}

	protected void downloadDataset(String dataset) throws IOException {
		File file = new File(cache_dir, dataset + ".sdmx.zip");
		URL source = new URL(MessageFormat.format(bulk_data_pattern, dataset));
		FileUtils.copyURLToFile(source, file);
		// unzip
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File entryDestination = new File(cache_dir, entry.getName());
			entryDestination.getParentFile().mkdirs();
			if (entry.isDirectory())
				entryDestination.mkdirs();
			else {
				InputStream in = zipFile.getInputStream(entry);
				OutputStream out = new FileOutputStream(entryDestination);
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
		zipFile.close();
	}

	@Override
	public InputStream getDataflow(String dataset) {
		throw new RuntimeException("Not implemented");
	}

	@PostConstruct
	public void init() {
		File cache_dir_file = new File(cache_dir);
		if (!cache_dir_file.exists()) {
			boolean created = cache_dir_file.mkdirs();
			if (!created) {
				logger.error("Cannot create cache dir: " + cache_dir_file.getAbsolutePath());
			}
		}
	}
}
