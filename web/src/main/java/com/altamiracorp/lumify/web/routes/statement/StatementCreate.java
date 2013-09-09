package com.altamiracorp.lumify.web.routes.statement;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

public class StatementCreate implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatementCreate.class.getName());
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        // validate parameters
        String sourceGraphVertexId = URLDecoder.decode(getRequiredParameter(request, "sourceGraphVertexId"), "UTF-8");
        String destGraphVertexId = URLDecoder.decode(getRequiredParameter(request, "destGraphVertexId"), "UTF-8");
        String predicateLabel = getRequiredParameter(request, "predicateLabel");

        GraphRelationship relationship = graphRepository.saveRelationship(session.getGraphSession(), sourceGraphVertexId, destGraphVertexId, predicateLabel);

        LOGGER.info("Statement created:\n" + relationship.toJson().toString(2));

        new Responder(response).respondWith(relationship.toJson());
    }

    public static String getRequiredParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            throw new RuntimeException("'" + parameterName + "' is required.");
        }
        return parameter;
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
