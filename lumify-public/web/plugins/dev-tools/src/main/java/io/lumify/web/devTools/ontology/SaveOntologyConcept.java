package io.lumify.web.devTools.ontology;

import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.ontology.Concept;
import io.lumify.core.model.ontology.OntologyRepository;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.user.User;
import io.lumify.web.BaseRequestHandler;
import org.securegraph.Authorizations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SaveOntologyConcept extends BaseRequestHandler {
    private OntologyRepository ontologyRepository;

    @Inject
    public SaveOntologyConcept(UserRepository userRepository, WorkspaceRepository workspaceRepository, Configuration configuration, OntologyRepository ontologyRepository) {
        super(userRepository, workspaceRepository, configuration);
        this.ontologyRepository = ontologyRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain handlerChain) throws Exception {
        String conceptIRI = getRequiredParameter(request, "concept");
        String displayName = getRequiredParameter(request, "displayName");
        String color = getRequiredParameter(request, "color");
        String titleFormula = getRequiredParameter(request, "titleFormula");
        String subtitleFormula = getRequiredParameter(request, "subtitleFormula");
        String timeFormula = getRequiredParameter(request, "timeFormula");

        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);

        Concept concept = ontologyRepository.getConceptByIRI(conceptIRI);
        if (concept == null) {
            respondWithNotFound(response, "concept " + conceptIRI + " not found");
            return;
        }

        if (displayName.length() != 0) {
            concept.setProperty(LumifyProperties.DISPLAY_NAME.getPropertyName(), displayName, authorizations);
        }

        if (color.length() != 0) {
            concept.setProperty(LumifyProperties.COLOR.getPropertyName(), color, authorizations);
        }

        if (timeFormula.length() != 0) {
            concept.setProperty(LumifyProperties.TITLE_FORMULA.getPropertyName(), titleFormula, authorizations);
        } else {
            concept.removeProperty(LumifyProperties.TITLE_FORMULA.getPropertyName(), authorizations);
        }

        if (subtitleFormula.length() != 0) {
            concept.setProperty(LumifyProperties.SUBTITLE_FORMULA.getPropertyName(), subtitleFormula, authorizations);
        } else {
            concept.removeProperty(LumifyProperties.SUBTITLE_FORMULA.getPropertyName(), authorizations);
        }

        if (timeFormula.length() != 0) {
            concept.setProperty(LumifyProperties.TIME_FORMULA.getPropertyName(), timeFormula, authorizations);
        } else {
            concept.removeProperty(LumifyProperties.TIME_FORMULA.getPropertyName(), authorizations);
        }

        ontologyRepository.clearCache();

        respondWithHtml(response, "OK");
    }
}
