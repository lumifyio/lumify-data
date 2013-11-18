package com.altamiracorp.lumify.storm.term.analysis;

import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class ArtifactLocationAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactLocationAnalyzer.class);

    private final GraphRepository graphRepository;

    @Inject
    public ArtifactLocationAnalyzer(final GraphRepository graphRepo) {
        graphRepository = graphRepo;
    }

    public void analyzeLocation(final GraphVertex vertex, final List<TermMention> termMentions, final User user) {
        checkNotNull(vertex);
        checkNotNull(termMentions);
        checkNotNull(user);

        TermMention largest = null;
        for (TermMention termMention : termMentions) {
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

    private void updateGraphVertex(final TermMention mention, final GraphVertex vertex, final User user) {
        final TermMentionMetadata termMetadata = mention.getMetadata();
        boolean vertexUpdated = false;
        final String geolocationTitle = termMetadata.getGeoLocationTitle();
        final Double latitude = termMetadata.getLatitude();
        final Double longitude = termMetadata.getLongitude();

        if (geolocationTitle != null) {
            vertex.setProperty(PropertyName.GEO_LOCATION_DESCRIPTION, geolocationTitle);
            vertexUpdated = true;
        } else {
            LOGGER.warn("Could not set geolocation title on vertex");
        }

        if (latitude != null && longitude != null) {
            vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(latitude, longitude));
            vertexUpdated = true;
        } else {
            LOGGER.warn("Could not operate on invalid geolocation coordinate");
        }

        if (vertexUpdated) {
            graphRepository.saveVertex(vertex, user);
            LOGGER.debug("Updated artifact vertex");
        }
    }
}
