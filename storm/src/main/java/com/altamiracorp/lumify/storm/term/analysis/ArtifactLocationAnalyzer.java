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

@Singleton
public class ArtifactLocationAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactLocationAnalyzer.class);

    private final GraphRepository graphRepository;

    @Inject
    public ArtifactLocationAnalyzer(final GraphRepository graphRepo) {
        graphRepository = graphRepo;
    }

    public void analyzeLocation(final GraphVertex vertex, final List<TermMention> termMentions, final User user) {
        TermMention largest = null;

        for (TermMention termMention : termMentions) {
            if (termMention.getMetadata().getGeoLocation() != null) {
                if (largest == null) {
                    largest = termMention;
                    continue;
                }
                if (termMention.getMetadata().getGeoLocationPopulation() > largest.getMetadata().getGeoLocationPopulation()) {
                    largest = termMention;
                    continue;
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

        if (vertex != null && termMetadata != null) {
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
        } else {
            LOGGER.info("Unable to update vertex");
        }
    }
}
