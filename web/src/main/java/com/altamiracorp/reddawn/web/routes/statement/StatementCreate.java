package com.altamiracorp.reddawn.web.routes.statement;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
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
        RedDawnSession session = app.getRedDawnSession(request);

        // validate parameters
        String sourceGraphNodeId = request.getParameter("sourceGraphNodeId");
        String destGraphNodeId = request.getParameter("destGraphNodeId");
        String predicateLabel = request.getParameter("predicateLabel");

        if (sourceGraphNodeId == null) {
            throw new RuntimeException("'sourceGraphNodeId' is required.");
        }
        sourceGraphNodeId = URLDecoder.decode(sourceGraphNodeId, "UTF-8");

        if (destGraphNodeId == null) {
            throw new RuntimeException("'objectRowKey' is required.");
        }
        destGraphNodeId = URLDecoder.decode(destGraphNodeId, "UTF-8");

        if (predicateLabel == null) {
            throw new RuntimeException("'predicateLabel' is required.");
        }

        GraphRelationship relationship = graphRepository.saveRelationship(session.getGraphSession(), sourceGraphNodeId, destGraphNodeId, "termToTerm");

        LOGGER.info("Statement created:\n" + relationship.toJson().toString(2));

        new Responder(response).respondWith(relationship.toJson());
    }

    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
