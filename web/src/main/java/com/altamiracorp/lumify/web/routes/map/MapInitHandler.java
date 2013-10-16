package com.altamiracorp.lumify.web.routes.map;

import com.altamiracorp.lumify.core.config.MapConfig;
import com.altamiracorp.web.MustacheTemplateHandler;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

public class MapInitHandler extends MustacheTemplateHandler {
    private final MapConfig config;


    @Inject
    public MapInitHandler(final MapConfig config) throws IOException {
        super();
        this.config = config;
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

        model.mapProvider = config.getMapProvider();
        if (model.mapProvider == null) {
            model.mapProvider = "leaflet";
        }
        model.apiKey = config.getMapAccessKey();
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
