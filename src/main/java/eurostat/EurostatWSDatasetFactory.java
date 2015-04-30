package eurostat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	public InputStream getDSD(String dataset) throws Exception {
		File file = new File(cache_dir, dataset + "_dsd.xml");
		if (!file.exists()) {
			URL source = new URL(MessageFormat.format(dsd_pattern, dataset));
			FileUtils.copyURLToFile(source, file);
		}
		return new FileInputStream(file);
	}

	@Override
	public InputStream getData(String dataset) throws Exception {
		File file = new File(cache_dir, dataset + "_data.sdmx.xml");
		
		if (!file.exists()) {
			URL source = new URL(MessageFormat.format(data_pattern, dataset));

			FileUtils.copyURLToFile(source, file);
		}

		URL redirectURL = getRedirectURL(file);
		
		if (redirectURL != null) {
			downloadZIPAfterRedirect(cache_dir, dataset, redirectURL, file);
		}

		return new FileInputStream(file);
	}
	
	protected void downloadZIPAfterRedirect(String cache_dir, String dataset, URL source, File destination) throws Exception {
		File file = new File(cache_dir, dataset + ".zip");
		logger.debug(file);
		logger.debug(source);
		for (int i = 0; i < 60; i++) {
			try {
				FileUtils.copyURLToFile(source, file);
				break;
			} catch (FileNotFoundException e) {
				logger.warn("Failed to download. Retrying..");
				Thread.sleep(1000);
			}
		}
		
		// unzip
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();

			// We expect just one file here
			InputStream in = zipFile.getInputStream(entry);
			OutputStream out = new FileOutputStream(destination);
			IOUtils.copy(in, out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			break;
		}
		zipFile.close();
	}
	
	private URL getRedirectURL(File file) throws FileNotFoundException, XMLStreamException, MalformedURLException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(file));
		
		Stack<QName> nestingStack = new Stack<QName>();
		String code = "";
		String severity = "";
		int messageIndex = 0;
		String redirectURL;
		
		while (parser.hasNext()) {
			int event = parser.next();
			
			if (event == XMLStreamConstants.START_ELEMENT) {
				QName nodeName = parser.getName();

				if (nodeName.getPrefix().equals("footer") && nodeName.getLocalPart().equals("Message")) {
					code = parser.getAttributeValue(null, "code");
					severity = parser.getAttributeValue(null, "severity");
					logger.debug("code=" + code);
					logger.debug("severity=" + severity);
					messageIndex = 0;
				} else if (nodeName.getLocalPart().equals("Text")) {
					// XXXcatalinb: We are looking for the second text entry in the footer, because
					// it seems it's the one always holding the url.
					messageIndex++;				
				}
				
				nestingStack.push(nodeName);
			} else if (event == XMLStreamConstants.END_ELEMENT) {
				nestingStack.pop();
			} else if (event == XMLStreamConstants.CHARACTERS) {
				// TODO(catalinb): check that we are in the correct context here
				if (!code.equals("413")) {
					continue;
				}
				if (messageIndex == 2) {
					logger.debug("REDIRECT URL: " + parser.getText());
					URL url = new URL(parser.getText());
					return url;
				}
			}
		}
		
		return null;
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
