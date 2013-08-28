package com.altamiracorp.reddawn.web.routes.map;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;

import com.altamiracorp.reddawn.web.config.MapConfig;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class MapTileHandler implements Handler {
    private final MapConfig config;


    @Inject
    public MapTileHandler(final MapConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String hostName = config.getMapTileServerHostname();
        int port = config.getMapTileServerPort();

        String x = (String) request.getAttribute("x");
        String y = (String) request.getAttribute("y");
        String z = (String) request.getAttribute("z");
        String path = "/" + z + "/" + x + "/" + y + ".png";
        URL url = new URL("http", hostName, port, path);
        InputStream in = url.openStream();
        OutputStream out = response.getOutputStream();
        response.setContentType("image/png");
        IOUtils.copy(in, out);
        out.close();
    }
}
