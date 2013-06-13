package com.altamiracorp.reddawn.web.routes.map;

import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.apache.poi.util.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class MapTileHandler implements Handler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String hostName = request.getServletContext().getInitParameter("map.tileServer.hostName");
        int port = Integer.parseInt(request.getServletContext().getInitParameter("map.tileServer.port"));

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
