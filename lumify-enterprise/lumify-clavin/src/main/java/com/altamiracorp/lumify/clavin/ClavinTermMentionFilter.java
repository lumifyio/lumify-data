package com.altamiracorp.lumify.clavin;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.ingest.graphProperty.TermMentionFilter;
import com.altamiracorp.lumify.core.ingest.graphProperty.TermMentionFilterPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.type.GeoPoint;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.resolver.LuceneLocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.google.inject.Inject;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.*;
import static com.altamiracorp.securegraph.util.IterableUtils.count;

/**
 * This TermResolutionWorker uses the CLAVIN processor to refine
 * identification of location entities.
 */
public class ClavinTermMentionFilter extends TermMentionFilter {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ClavinTermMentionFilter.class);

    /**
     * TODO: Don't hard-code this
     */
    private static final String TARGET_ONTOLOGY_URI = "http://lumify.io/dev#location";

    /**
     * The CLAVIN index directory configuration key.
     */
    public static final String CLAVIN_INDEX_DIRECTORY = "clavin.indexDirectory";

    /**
     * The CLAVIN max hit depth configuration key.
     */
    public static final String CLAVIN_MAX_HIT_DEPTH = "clavin.maxHitDepth";

    /**
     * The CLAVIN max context window configuration key.
     */
    public static final String CLAVIN_MAX_CONTEXT_WINDOW = "clavin.maxContextWindow";

    /**
     * The CLAVIN use fuzzy matching configuration key.
     */
    public static final String CLAVIN_USE_FUZZY_MATCHING = "clavin.useFuzzyMatching";

    /**
     * The default max hit depth.
     */
    public static final int DEFAULT_MAX_HIT_DEPTH = 5;

    /**
     * The default max context window.
     */
    public static final int DEFAULT_MAX_CONTENT_WINDOW = 5;

    /**
     * The default fuzzy matching.
     */
    public static final boolean DEFAULT_FUZZY_MATCHING = false;

    private File indexDirectory;
    private LuceneLocationResolver resolver;
    private boolean fuzzy;
    private ClavinOntologyMapper ontologyMapper;
    private Set<String> targetConcepts;
    private OntologyRepository ontologyRepository;

    @Override
    public void prepare(TermMentionFilterPrepareData termMentionFilterPrepareData) throws Exception {
        super.prepare(termMentionFilterPrepareData);

        Configuration config = new Configuration(termMentionFilterPrepareData.getStormConf());

        LOGGER.info("Configuring CLAVIN Location Resolution.");

        String idxDirPath = config.get(CLAVIN_INDEX_DIRECTORY, null);
        if (idxDirPath == null || idxDirPath.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("%s must be configured.", CLAVIN_INDEX_DIRECTORY));
        }
        LOGGER.debug("Configuring CLAVIN index [%s]: %s", CLAVIN_INDEX_DIRECTORY, idxDirPath);
        indexDirectory = new File(idxDirPath);
        if (!indexDirectory.exists() || !indexDirectory.isDirectory()) {
            throw new IllegalArgumentException(String.format("CLAVIN index cannot be found at configured (%s) location: %s",
                    CLAVIN_INDEX_DIRECTORY, idxDirPath));
        }

        int maxHitDepth = config.getInt(CLAVIN_MAX_HIT_DEPTH);
        if (maxHitDepth < 1) {
            LOGGER.debug("Found %s of %d. Using default: %d", CLAVIN_MAX_HIT_DEPTH, maxHitDepth, DEFAULT_MAX_HIT_DEPTH);
            maxHitDepth = DEFAULT_MAX_HIT_DEPTH;
        }
        int maxContextWindow = config.getInt(CLAVIN_MAX_CONTEXT_WINDOW);
        if (maxContextWindow < 1) {
            LOGGER.debug("Found %s of %d. Using default: %d", CLAVIN_MAX_CONTEXT_WINDOW, maxContextWindow, DEFAULT_MAX_CONTENT_WINDOW);
            maxContextWindow = DEFAULT_MAX_CONTENT_WINDOW;
        }
        String fuzzyStr = config.get(CLAVIN_USE_FUZZY_MATCHING, null);
        if (fuzzyStr != null) {
            fuzzyStr = fuzzyStr.trim();
        }
        if (fuzzyStr != null && Boolean.TRUE.toString().equalsIgnoreCase(fuzzyStr) ||
                Boolean.FALSE.toString().equalsIgnoreCase(fuzzyStr)) {
            fuzzy = Boolean.parseBoolean(fuzzyStr);
            LOGGER.debug("Found %s: %s. fuzzy=%s", CLAVIN_USE_FUZZY_MATCHING, fuzzyStr, fuzzy);
        } else {
            LOGGER.debug("%s not configured. Using default: %s", CLAVIN_USE_FUZZY_MATCHING, DEFAULT_FUZZY_MATCHING);
            fuzzy = DEFAULT_FUZZY_MATCHING;
        }
        resolver = new LuceneLocationResolver(indexDirectory, maxHitDepth, maxContextWindow);

        Set<String> tCon = new HashSet<String>();
        Concept rootConcept = ontologyRepository.getConceptByVertexId(TARGET_ONTOLOGY_URI);
        List<Concept> concepts = ontologyRepository.getAllLeafNodesByConcept(rootConcept);
        for (Concept con : concepts) {
            tCon.add(con.getTitle());
        }
        targetConcepts = Collections.unmodifiableSet(tCon);
    }

    @Override
    public Iterable<TermMention> apply(Vertex artifactGraphVertex, Iterable<TermMention> termMentions) throws IOException, ParseException {
        List<LocationOccurrence> locationOccurrences = getLocationOccurrencesFromTermMentions(termMentions);
        LOGGER.info("Found %d Locations in %d terms.", locationOccurrences.size(), count(termMentions));
        List<ResolvedLocation> resolvedLocationNames = resolver.resolveLocations(locationOccurrences, fuzzy);
        LOGGER.info("Resolved %d Locations", resolvedLocationNames.size());

        if (resolvedLocationNames.isEmpty()) {
            return termMentions;
        }

        Map<Integer, ResolvedLocation> resolvedLocationOffsetMap = new HashMap<Integer, ResolvedLocation>();
        for (ResolvedLocation resolvedLocation : resolvedLocationNames) {
            // assumes start/end positions are real, i.e., unique start positions for each extracted term
            resolvedLocationOffsetMap.put(resolvedLocation.getLocation().getPosition(), resolvedLocation);
        }

        ResolvedLocation loc;
        String processId = getClass().getName();
        TermMention resolvedMention;
        List<TermMention> results = new ArrayList<TermMention>();
        for (TermMention termMention : termMentions) {
            loc = resolvedLocationOffsetMap.get(termMention.getStart());
            if (isLocation(termMention) && loc != null) {
                String id = String.format("CLAVIN-%d", loc.getGeoname().getGeonameID());
                resolvedMention = new TermMention.Builder(termMention)
                        .id(id)
                        .resolved(true)
                        .useExisting(true)
                        .sign(toSign(loc))
                        .ontologyClassUri(ontologyMapper.getOntologyClassUri(loc, termMention.getOntologyClassUri()))
                        .setProperty(GEO_LOCATION.getKey(),
                                GEO_LOCATION.wrap(new GeoPoint(loc.getGeoname().getLatitude(), loc.getGeoname().getLongitude())))
                        .setProperty(GEO_LOCATION_DESCRIPTION.getKey(), GEO_LOCATION_DESCRIPTION.wrap(termMention.getSign()))
                        .setProperty(SOURCE.getKey(), "CLAVIN")
                        .process(processId)
                        .build();
                LOGGER.debug("Replacing original location [%s] with resolved location [%s]", termMention, resolvedMention);
                results.add(resolvedMention);
            } else {
                results.add(termMention);
            }
        }
        return results;
    }

    private String toSign(final ResolvedLocation location) {
        GeoName geoname = location.getGeoname();
        return String.format("%s (%s, %s)", geoname.getName(), geoname.getPrimaryCountryCode(), geoname.getAdmin1Code());
    }

    @Inject
    public void setOntologyMapper(final ClavinOntologyMapper mapper) {
        this.ontologyMapper = mapper;
    }

    private boolean isLocation(final TermMention mention) {
        return targetConcepts.contains(mention.getOntologyClassUri());
    }

    private List<LocationOccurrence> getLocationOccurrencesFromTermMentions(final Iterable<TermMention> termMentions) {
        List<LocationOccurrence> locationOccurrences = new ArrayList<LocationOccurrence>();

        for (TermMention termMention : termMentions) {
            if (isLocation(termMention)) {
                locationOccurrences.add(new LocationOccurrence(termMention.getSign(), termMention.getStart()));
            }
        }
        return locationOccurrences;
    }

    @Inject
    public void setOntologyRepository(OntologyRepository ontologyRepository) {
        this.ontologyRepository = ontologyRepository;
    }
}
