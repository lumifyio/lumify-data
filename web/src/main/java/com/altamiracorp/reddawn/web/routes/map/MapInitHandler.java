package com.altamiracorp.reddawn.web.routes.map;

import com.altamiracorp.web.MustacheTemplateHandler;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

public class MapInitHandler extends MustacheTemplateHandler {
    public MapInitHandler() throws IOException {
        super();
    }

    @Override
    protected String getTemplateText() throws IOException {
        InputStream templateStream = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream("map-init.mustache");
        return IOUtils.toString(templateStream);
    }

    @Override
    protected Object getModel(HttpServletRequest request) {
        MapInitModel model = new MapInitModel();
        ServletContext servletContext = request.getServletContext();

        model.mapProvider = servletContext.getInitParameter("map.provider");
        if (model.mapProvider == null) {
            model.mapProvider = "leaflet";
        }
        model.apiKey = servletContext.getInitParameter("map.apiKey");
        return model;
    }

    @Override
    protected String getContentType() {
        return "text/javascript";
    }

    private class MapInitModel {
        public String mapProvider;
        public String apiKey;
    }
}
