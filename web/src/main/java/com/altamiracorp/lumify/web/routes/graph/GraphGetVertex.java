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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphGetVertex implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        String graphVertexId = (String) request.getAttribute("graphVertexId");
        GraphVertex vertex = graphRepository.findVertex(session.getGraphSession(), graphVertexId);
        new Responder(response).respondWith(vertex.toJson());
    }
}

