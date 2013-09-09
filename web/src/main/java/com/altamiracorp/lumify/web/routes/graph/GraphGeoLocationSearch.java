package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class GraphGeoLocationSearch implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        double latitude = Double.parseDouble(request.getParameter("lat"));
        double longitude = Double.parseDouble(request.getParameter("lon"));
        double radius = Double.parseDouble(request.getParameter("radius"));

        List<GraphVertex> vertices = graphRepository.findByGeoLocation(session.getGraphSession(), latitude, longitude, radius);

        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));
        new Responder(response).respondWith(results);
    }
}
