package com.altamiracorp.lumify.web.routes.entity;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

public class EntitySearch extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String query = getRequiredParameter(request, "q");

        AppSession session = app.getAppSession(request);
        List<GraphVertex> vertices = graphRepository.searchVerticesByTitleAndType(session.getGraphSession(), query, VertexType.ENTITY);

        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));

        respondWithJson(response, results);
    }
}
