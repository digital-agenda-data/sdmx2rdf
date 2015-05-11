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

import eurostat.EurostatApp.Result;

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

        boolean forceRefresh = req.getParameter("force_refresh") != null;
        boolean ignoreCache = req.getParameter("ignore_cache") != null; 
        resource = ctx.getResource("classpath:eurostat_dataflows/latest");
        //resource = ctx.getResource("url:http://www.ec.europa.eu/eurostat/SDMX/diss-web/rest/dataflow/ESTAT/all/latest");
        
        logger.info("Resource " + resource);
        

        resp.setContentType("application/rdf+xml");
        resp.setHeader("Content-Disposition","attachment; filename=" + requested + ".rdf");
            
        EurostatApp ea = ctx.getBean(EurostatApp.class);
        OutputStream outputStream = resp.getOutputStream();
            
        Result result = ea.fetchAndConvertDataset(resource.getInputStream(), forceRefresh, requested, forceRefresh, outputStream);
            
        if (result == Result.FOUND) {
        	return;
        }
            
        resp.reset();
        switch(result) {
		case DATASET_TOO_LARGE:
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "The requested dataset is to large to download.");
			return;
		case ERROR:
			resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "Unexpected error!");
			return;
		case NOT_FOUND:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested dataset was not found!");
			return;
		default:
			logger.warn("Invalid result!");
        }
    }
}
