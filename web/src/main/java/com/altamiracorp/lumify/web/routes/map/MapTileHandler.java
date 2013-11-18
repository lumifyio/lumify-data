package com.altamiracorp.lumify.web.routes.map;

import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.apache.poi.util.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class MapTileHandler extends BaseRequestHandler {
    private final Configuration config;


    @Inject
    public MapTileHandler(final Configuration config) {
        this.config = config;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String hostName = config.get(Configuration.MAP_TILE_SERVER_HOST);
        int port = config.getInt(Configuration.MAP_TILE_SERVER_PORT);

        final String x = getAttributeString(request, "x");
        final String y = getAttributeString(request, "y");
        final String z = getAttributeString(request, "z");

        String path = "/" + z + "/" + x + "/" + y + ".png";
        URL url = new URL("http", hostName, port, path);

        InputStream in = url.openStream();
        OutputStream out = response.getOutputStream();
        response.setContentType("image/png");
        IOUtils.copy(in, out);
        out.close();
    }
}
