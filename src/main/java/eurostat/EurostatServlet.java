package eurostat;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

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
        if(requested.trim().isEmpty()) {
            resp.getOutputStream().print("Please set the path.");
        }



        // get the resource
        // defaults to URL source; if needed it can be changed to the built-in file using the "?file" request parameter
        Resource resource = null;

        if(req.getParameter("file") != null){
            resource = ctx.getResource("classpath:eurostat_dataflows/latest");
        } else {
            resource = ctx.getResource("url:http://www.ec.europa.eu/eurostat/SDMX/diss-web/rest/dataflow/ESTAT/all/latest");
        }

        logger.info("Resource " + resource);

        // invoke the converter
        EurostatApp ea = ctx.getBean(EurostatApp.class);

        resp.setContentType("application/rdf+xml");
        // todo may not be needed for smaller files
        resp.setHeader("Content-Disposition","attachment; filename=" + requested + ".rdf");

        try {
            InputStream is = resource.getInputStream();
            ea.downloadAllIsoc(is, requested, resp.getOutputStream());
        } catch (Exception e) {
            logger.warn(e,e);
        }
    }
}
