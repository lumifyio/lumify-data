package io.lumify.web.routes.artifact;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import io.lumify.core.EntityHighlighter;
import io.lumify.core.config.Configuration;
import io.lumify.core.ingest.video.VideoTranscript;
import io.lumify.core.model.properties.MediaLumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.model.termMention.TermMentionModel;
import io.lumify.core.model.termMention.TermMentionRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.user.User;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import org.securegraph.Authorizations;
import org.securegraph.Graph;
import org.securegraph.Vertex;
import org.securegraph.property.StreamingPropertyValue;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ArtifactHighlightedText extends BaseRequestHandler {
    private final Graph graph;
    private final TermMentionRepository termMentionRepository;
    private final EntityHighlighter entityHighlighter;
    private final UserRepository userRepository;

    @Inject
    public ArtifactHighlightedText(
            final Graph graph,
            final UserRepository userRepository,
            final TermMentionRepository termMentionRepository,
            final EntityHighlighter entityHighlighter,
            final Configuration configuration) {
        super(userRepository, configuration);
        this.graph = graph;
        this.termMentionRepository = termMentionRepository;
        this.entityHighlighter = entityHighlighter;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);
        String workspaceId = getActiveWorkspaceId(request);
        ModelUserContext modelUserContext = userRepository.getModelUserContext(authorizations, workspaceId);

        String graphVertexId = getAttributeString(request, "graphVertexId");
        Vertex artifactVertex = graph.getVertex(graphVertexId, authorizations);
        if (artifactVertex == null) {
            respondWithNotFound(response);
            return;
        }

        String highlightedText;
        String text = getText(artifactVertex);
        if (text == null) {
            highlightedText = "";
        } else {
            Iterable<TermMentionModel> termMentions = termMentionRepository.findByGraphVertexId(artifactVertex.getId().toString(), modelUserContext);
            highlightedText = entityHighlighter.getHighlightedText(text, termMentions);
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        IOUtils.write(highlightedText, response.getOutputStream(), "UTF-8");
    }

    private String getText(Vertex artifactVertex) throws IOException {
        StringBuilder result = new StringBuilder();
        Iterable<StreamingPropertyValue> textPropertyValues = RawLumifyProperties.TEXT.getPropertyValues(artifactVertex);
        for (StreamingPropertyValue textPropertyValue : textPropertyValues) {
            result.append(IOUtils.toString(textPropertyValue.getInputStream(), "UTF-8"));
        }

        Iterable<VideoTranscript> videoTranscripts = MediaLumifyProperties.VIDEO_TRANSCRIPT.getPropertyValues(artifactVertex);
        for (VideoTranscript videoTranscript : videoTranscripts) {
            result.append(videoTranscript.toString());
        }

        return result.toString();
    }
}
