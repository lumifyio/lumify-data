package com.altamiracorp.reddawn.web.routes.vertex;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class VertexMultiple implements Handler, AppAware {
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = this.app.getRedDawnSession(request);
        String[] vertexIds = request.getParameterValues("vertexIds[]");

        List<GraphVertex> graphVertices = graphRepository.findVertices(session.getGraphSession(), vertexIds);

        new Responder(response).respondWith(GraphVertex.toJson(graphVertices));
    }
}
