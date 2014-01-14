package com.altamiracorp.lumify.storm.term.analysis;

import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thinkaurelius.titan.core.attribute.Geoshape;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class ArtifactLocationAnalyzer {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ArtifactLocationAnalyzer.class);

    private final GraphRepository graphRepository;
    private final AuditRepository auditRepository;

    @Inject
    public ArtifactLocationAnalyzer(final GraphRepository graphRepo, final AuditRepository auditRepo) {
        graphRepository = graphRepo;
        auditRepository = auditRepo;
    }

    public void analyzeLocation(final GraphVertex vertex, final List<TermMentionModel> termMentions, final User user) {
        checkNotNull(vertex);
        checkNotNull(termMentions);
        checkNotNull(user);

        TermMentionModel largest = null;
        for (TermMentionModel termMention : termMentions) {
            TermMentionMetadata termMentionMetadata = termMention.getMetadata();
            if (termMentionMetadata != null && termMentionMetadata.getGeoLocation() != null) {
                if (largest == null) {
                    largest = termMention;
                    continue;
                }
                if (termMentionMetadata.getGeoLocationPopulation() > largest.getMetadata().getGeoLocationPopulation()) {
                    largest = termMention;
                }
            }
        }

        if (largest != null) {
            updateGraphVertex(largest, vertex, user);
        }
    }

    private void updateGraphVertex(final TermMentionModel mention, final GraphVertex vertex, final User user) {
        final TermMentionMetadata termMetadata = mention.getMetadata();
        boolean vertexUpdated = false;
        final String geolocationTitle = termMetadata.getGeoLocationTitle();
        final Double latitude = termMetadata.getLatitude();
        final Double longitude = termMetadata.getLongitude();
        List<String> modifiedProperties = new ArrayList<String>();

        if (geolocationTitle != null) {
            vertex.setProperty(PropertyName.GEO_LOCATION_DESCRIPTION, geolocationTitle);
            vertexUpdated = true;
            modifiedProperties.add(PropertyName.GEO_LOCATION_DESCRIPTION.toString());
        } else {
            LOGGER.warn("Could not set geolocation title on vertex");
        }

        if (latitude != null && longitude != null) {
            vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(latitude, longitude));
            vertexUpdated = true;
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        } else {
            LOGGER.warn("Could not operate on invalid geolocation coordinate");
        }

        if (vertexUpdated) {
            graphRepository.saveVertex(vertex, user);
            LOGGER.debug("Updated artifact vertex");

            for (String property : modifiedProperties) {
                auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), vertex, property, this.getClass().getName(), "", user);
            }
        }
    }
}
