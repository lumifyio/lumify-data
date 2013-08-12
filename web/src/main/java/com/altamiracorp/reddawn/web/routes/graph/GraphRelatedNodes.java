package com.altamiracorp.reddawn.web.routes.graph;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class GraphRelatedNodes implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String graphNodeId = (String) request.getAttribute("graphNodeId");

        List<GraphNode> graphNodes = graphRepository.getRelatedNodes(session.getGraphSession(), graphNodeId);

        JSONObject json = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        for (GraphNode graphNode : graphNodes) {
            JSONObject graphNodeJson = graphNode.toJson();
            nodesJson.put(graphNodeJson);
        }
        json.put("nodes", nodesJson);
        new Responder(response).respondWith(json);

        chain.next(request, response);
    }
}

