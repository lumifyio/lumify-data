package com.altamiracorp.lumify.storm.term.analysis;

import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.geoNames.GeoNamePostalCodeRepository;
import com.altamiracorp.lumify.core.model.geoNames.GeoNameRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.location.SimpleTermLocationExtractor;
import com.altamiracorp.lumify.storm.term.extraction.TermMentionWithGraphVertex;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thinkaurelius.titan.core.attribute.Geoshape;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class LocationTermAnalyzer {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LocationTermAnalyzer.class);

    private final SimpleTermLocationExtractor simpleTermLocationExtractor;
    private final GeoNamePostalCodeRepository geoNamePostalCodeRepository;
    private final GeoNameRepository geoNameRepository;
    private final GraphRepository graphRepository;
    private final AuditRepository auditRepository;
    private final TermMentionRepository termRepository;


    @Inject
    public LocationTermAnalyzer(final SimpleTermLocationExtractor extractor, final GeoNamePostalCodeRepository postalCodeRepo,
                                final GeoNameRepository geoNameRepo, final GraphRepository graphRepo,
                                final AuditRepository auditRepo, final TermMentionRepository termRepo) {
        simpleTermLocationExtractor = extractor;
        geoNamePostalCodeRepository = postalCodeRepo;
        geoNameRepository = geoNameRepo;
        graphRepository = graphRepo;
        auditRepository = auditRepo;
        termRepository = termRepo;
    }

    public TermMentionModel analyzeTermData(final TermMentionWithGraphVertex data, final User user) {
        checkNotNull(data);
        checkNotNull(data.getTermMention());
        checkNotNull(data.getTermMention().getMetadata());
        checkNotNull(user);

        final TermMentionModel termMention = data.getTermMention();

        LOGGER.info("Analyzing term: %s", termMention.getMetadata().getSign());
        TermMentionModel updatedTerm;
        if (simpleTermLocationExtractor.isPostalCode(termMention)) {
            LOGGER.info("Analyzing postal code for term: %s", termMention.getRowKey().toString());
            updatedTerm = simpleTermLocationExtractor.getTermWithPostalCodeLookup(geoNamePostalCodeRepository, termMention, user);
        } else {
            LOGGER.info("Analyzing location for term: %s", termMention.getRowKey().toString());
            updatedTerm = simpleTermLocationExtractor.getTermWithLocationLookup(geoNameRepository, termMention, user);
        }

        if (updatedTerm != null) {
            LOGGER.info("Updating associated graph vertex");
            updateGraphVertex(updatedTerm, data.getGraphVertex(), user);
            termRepository.save(updatedTerm, user.getModelUserContext());
            return updatedTerm;
        }
        return null;
    }

    private void updateGraphVertex(final TermMentionModel termMention, final GraphVertex vertex, final User user) {
        final TermMentionMetadata termMetadata = termMention.getMetadata();

        if (vertex != null) {
            final Double latitude = termMetadata.getLatitude();
            final Double longitude = termMetadata.getLongitude();

            if (latitude != null && longitude != null) {
                vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(latitude, longitude));
                graphRepository.saveVertex(vertex, user);

                auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), vertex, PropertyName.GEO_LOCATION.toString(), this.getClass().getName(), "", user);

                LOGGER.debug("Updated vertex geolocation");
            } else {
                LOGGER.warn("Could not operate on invalid geolocation coordinate");
            }
        } else {
            LOGGER.info("Unable to update vertex");
        }
    }
}
