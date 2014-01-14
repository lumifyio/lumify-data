/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.term.resolution;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermResolutionWorker;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.resolver.LuceneLocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This TermResolutionWorker uses the Clavin processor to refine
 * identification of location entities.
 */
public class ClavinLocationResolutionWorker implements TermResolutionWorker {
    /**
     * The class logger.
     */
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ClavinLocationResolutionWorker.class);
    
    /**
     * TODO: Don't hard-code this
     */
    private static final String TARGET_ONTOLOGY_URI = "location";
    
    /**
     * The Clavin disabled configuration key.
     */
    public static final String CLAVIN_DISABLED = "clavin.disabled";
    
    /**
     * The Clavin index directory configuration key.
     */
    public static final String CLAVIN_INDEX_DIRECTORY = "clavin.indexDirectory";
    
    /**
     * The Clavin max hit depth configuration key.
     */
    public static final String CLAVIN_MAX_HIT_DEPTH = "clavin.maxHitDepth";
    
    /**
     * The Clavin max content window configuration key.
     */
    public static final String CLAVIN_MAX_CONTENT_WINDOW = "clavin.maxContentWindow";
    
    /**
     * The Clavin use fuzzy matching configuration key.
     */
    public static final String CLAVIN_USE_FUZZY_MATCHING = "clavin.useFuzzyMatching";
    
    /**
     * The default max hit depth.
     */
    private static final int DEFAULT_MAX_HIT_DEPTH = 5;
    
    /**
     * The default max content window.
     */
    private static final int DEFAULT_MAX_CONTENT_WINDOW = 5;
    
    /**
     * The default fuzzy matching.
     */
    private static final boolean DEFAULT_FUZZY_MATCHING = true;
    
    private boolean disabled;
    private File indexDirectory;
    private LuceneLocationResolver resolver;
    private boolean fuzzy;
    private ClavinOntologyMapper ontologyMapper;

    @Override
    public void prepare(Map conf, User user) throws Exception {
        Configuration config = new Configuration(conf);
        
        LOGGER.info("Configuring Clavin Location Resolution.");
        disabled = Boolean.parseBoolean(config.get(CLAVIN_DISABLED));
        if (disabled) {
            LOGGER.info("Clavin disabled. Initialization stopped.");
            return;
        }
        
        String idxDirPath = config.get(CLAVIN_INDEX_DIRECTORY, null);
        if (idxDirPath == null || idxDirPath.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("%s must be configured.", CLAVIN_INDEX_DIRECTORY));
        }
        LOGGER.debug("Configuring Clavin index [%s]: %s", CLAVIN_INDEX_DIRECTORY, idxDirPath);
        indexDirectory = new File(idxDirPath);
        if (!indexDirectory.exists() || !indexDirectory.isDirectory()) {
            throw new IllegalArgumentException(String.format("Clavin index cannot be found at configured (%s) location: %s",
                    CLAVIN_INDEX_DIRECTORY, idxDirPath));
        }
        
        int maxHitDepth = config.getInt(CLAVIN_MAX_HIT_DEPTH);
        if (maxHitDepth < 1) {
            LOGGER.debug("Found %s of %d. Using default: %d", CLAVIN_MAX_HIT_DEPTH, maxHitDepth, DEFAULT_MAX_HIT_DEPTH);
            maxHitDepth = DEFAULT_MAX_HIT_DEPTH;
        }
        int maxContentWindow = config.getInt(CLAVIN_MAX_CONTENT_WINDOW);
        if (maxContentWindow < 1) {
            LOGGER.debug("Found %s of %d. Using default: %d", CLAVIN_MAX_CONTENT_WINDOW, maxContentWindow, DEFAULT_MAX_CONTENT_WINDOW);
            maxContentWindow = DEFAULT_MAX_CONTENT_WINDOW;
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
        resolver = new LuceneLocationResolver(indexDirectory, maxHitDepth, maxContentWindow);
    }

    @Override
    public TermExtractionResult resolveTerms(TermExtractionResult termExtractionResult) throws Exception {
        if (disabled) {
            LOGGER.info("Clavin disabled. Processing cancelled.");
            return termExtractionResult;
        }
        List<LocationOccurrence> locationOccurrences = getLocationOccurrencesFromTermMentions(termExtractionResult.getTermMentions());
        LOGGER.info("Found %d Locations in %d terms.", locationOccurrences.size(), termExtractionResult.getTermMentions().size());
        List<ResolvedLocation> resolvedLocationNames = resolver.resolveLocations(locationOccurrences, fuzzy);
        LOGGER.info("Resolved %d Locations", resolvedLocationNames.size());

        Map<Integer, ResolvedLocation> resolvedLocationOffsetMap = new HashMap<Integer, ResolvedLocation>();
        for (ResolvedLocation resolvedLocation : resolvedLocationNames) {
            // assumes start/end positions are real, i.e., unique start positions for each extracted term
            resolvedLocationOffsetMap.put(resolvedLocation.location.position, resolvedLocation);
        }

        Map<TermMention, TermMention> updateMap = new HashMap<TermMention, TermMention>();
        ResolvedLocation loc;
        String processId = getClass().getName();
        TermMention resolvedMention;
        for (TermMention termMention : termExtractionResult.getTermMentions()) {
            loc = resolvedLocationOffsetMap.get(termMention.getStart());
            if (TARGET_ONTOLOGY_URI.equalsIgnoreCase(termMention.getOntologyClassUri()) && loc != null) {
                resolvedMention = new TermMention.Builder(termMention)
                        .resolved(true)
                        .useExisting(true)
                        .sign(toSign(loc))
                        .ontologyClassUri(ontologyMapper.getOntologyClassUri(loc, termMention.getOntologyClassUri()))
                        .setProperty(PropertyName.GEO_LOCATION.toString(),
                                Geoshape.point(loc.geoname.latitude, loc.geoname.longitude))
                        .setProperty(PropertyName.GEO_LOCATION_DESCRIPTION.toString(), termMention.getSign())
                        .process(processId)
                        .build();
                updateMap.put(termMention, resolvedMention);
                LOGGER.debug("Replacing original location [%s] with resolved location [%s]", termMention, resolvedMention);
            }
        }
        for (Map.Entry<TermMention, TermMention> update : updateMap.entrySet()) {
            termExtractionResult.replace(update.getKey(), update.getValue());
        }

        return termExtractionResult;
    }

    private String toSign(final ResolvedLocation location) {
        return String.format("%s (%s, %s)", location.geoname.name, location.geoname.primaryCountryCode, location.geoname.admin1Code);
    }
    
    @Inject
    public void setOntologyMapper(final ClavinOntologyMapper mapper) {
        this.ontologyMapper = mapper;
    }
    
    private static List<LocationOccurrence> getLocationOccurrencesFromTermMentions(List<TermMention> termMentions) {
        List<LocationOccurrence> locationOccurrences = new ArrayList<LocationOccurrence>();

        for (TermMention termMention : termMentions)
            if (TARGET_ONTOLOGY_URI.equalsIgnoreCase(termMention.getOntologyClassUri())) {
                locationOccurrences.add(new LocationOccurrence(termMention.getSign(), termMention.getStart()));
            }

        return locationOccurrences;
    }
}
