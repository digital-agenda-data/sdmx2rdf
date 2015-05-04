package eurostat;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

@Component("eurostatServlet")
public class EurostatServlet implements HttpRequestHandler {

    @Autowired
    private ApplicationContext ctx;

    private final Log logger = LogFactory.getLog(getClass());
    
    private String sdmx_cache_dir = "sdmx_cache";
    private String sdmx_temp_dir = "sdmx_tmp";

    /**
     * Default request handler, serves a RDF from the EurostatApp stream
     * Default data source is the ESTAT latest, can be changed to the project file with the "?file" parameter
     */
    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get the request
        String requested = req.getPathInfo().substring(1);
        logger.error(requested.trim());
        if(requested.trim().isEmpty()) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Please set the path.");
        	return;
        }
        
        // get the resource
        // defaults to URL source; if needed it can be changed to the built-in file using the "?file" request parameter
        Resource resource = null;

        if(req.getParameter("file") != null) {
            resource = ctx.getResource("classpath:eurostat_dataflows/latest");
        } else {
            resource = ctx.getResource("url:http://www.ec.europa.eu/eurostat/SDMX/diss-web/rest/dataflow/ESTAT/all/latest");
        }
        
        logger.info("Resource " + resource);
        
        try {
            resp.setContentType("application/rdf+xml");
            resp.setHeader("Content-Disposition","attachment; filename=" + requested + ".rdf");
            
            InputStream is = resource.getInputStream();      
            boolean found = getDataset(is, requested, resp.getOutputStream());
            if (!found) {
            	resp.reset();
            	resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Dataset not found.");
            }
            
        } catch (MalformedURLException e) {
        	resp.reset();
        	resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "The requested dataset is too large to download.");
        } catch (Exception e) {
            logger.warn(e,e);
        }
    }
    
	private boolean getDataset(InputStream dataflow, String dataset, OutputStream os) throws Exception {
		File file = new File(sdmx_cache_dir, dataset + ".rdf");

		if (file.exists()) {
			IOUtils.copy(new FileInputStream(file), os);
			return true;
		}
		
        FileOutputStream cache = new FileOutputStream(file);
        TeeOutputStream branchedStream = new TeeOutputStream(cache, os);
        
        // invoke the converter
        EurostatApp ea = ctx.getBean(EurostatApp.class);     
        return ea.fetchAndConvertDataset(dataflow, dataset, branchedStream);
	}

	@PostConstruct
	public void init() {
		File sdmx_cache_dir_file = new File(sdmx_cache_dir);

		if ( !sdmx_cache_dir_file.exists() ) {
			boolean created = sdmx_cache_dir_file.mkdirs();
			if (!created) {
				logger.error("Cannot create cache dir: " + sdmx_cache_dir_file.getAbsolutePath());
			}
		}
	}
}
