package com.altamiracorp.lumify.storm.term.analysis;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.location.SimpleTermLocationExtractor;
import com.altamiracorp.lumify.model.geoNames.GeoNamePostalCodeRepository;
import com.altamiracorp.lumify.model.geoNames.GeoNameRepository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.storm.term.extraction.TermMentionWithGraphVertex;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thinkaurelius.titan.core.attribute.Geoshape;

@Singleton
public class LocationTermAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationTermAnalyzer.class);

    private final SimpleTermLocationExtractor simpleTermLocationExtractor;
    private final GeoNamePostalCodeRepository geoNamePostalCodeRepository;
    private final GeoNameRepository geoNameRepository;
    private final GraphRepository graphRepository;

    @Inject
    public LocationTermAnalyzer(final SimpleTermLocationExtractor extractor, final GeoNamePostalCodeRepository postalCodeRepo,
            final GeoNameRepository geoNameRepo, final GraphRepository graphRepo) {
        simpleTermLocationExtractor = extractor;
        geoNamePostalCodeRepository = postalCodeRepo;
        geoNameRepository = geoNameRepo;
        graphRepository = graphRepo;
    }

    public void analyzeTermData(final TermMentionWithGraphVertex data, final User user) {
        checkNotNull(data);
        checkNotNull(data.getTermMention());
        checkNotNull(user);

        final TermMention termMention = data.getTermMention();

        LOGGER.info("Analyzing term: " + termMention.getMetadata().getSign());
        TermMention updatedTerm;
        if (simpleTermLocationExtractor.isPostalCode(termMention)) {
            LOGGER.info("Analyzing postal code for term: " + termMention.getRowKey().toString());
            updatedTerm = simpleTermLocationExtractor.getTermWithPostalCodeLookup(geoNamePostalCodeRepository, termMention, user);
        } else {
            LOGGER.info("Analyzing location for term: " + termMention.getRowKey().toString());
            updatedTerm = simpleTermLocationExtractor.getTermWithLocationLookup(geoNameRepository, termMention, user);
        }

        if (updatedTerm != null) {
            LOGGER.info("Updating associated graph vertex");
            updateGraphVertex(updatedTerm, data.getGraphVertex(), user);
        }
    }

    private void updateGraphVertex(final TermMention termMention, final GraphVertex vertex, final User user) {
        final TermMentionMetadata termMetadata = termMention.getMetadata();

        if( vertex != null && termMetadata != null ) {
            final Double latitude = termMetadata.getLatitude();
            final Double longitude = termMetadata.getLongitude();

            if( latitude != null && longitude != null ) {
                vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(latitude, longitude));
                graphRepository.saveVertex(vertex, user);

                LOGGER.debug("Updated vertex geolocation");
            } else {
                LOGGER.warn("Could not operate on invalid geolocation coordinate");
            }
        } else {
            LOGGER.info("Unable to update vertex");
        }
    }
}
