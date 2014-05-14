package io.lumify.gtd;


import static io.lumify.core.model.properties.EntityLumifyProperties.GEO_LOCATION;
import static io.lumify.core.model.properties.EntityLumifyProperties.SOURCE;

import com.bericotech.clavin.ClavinException;
import com.bericotech.clavin.gazetteer.FeatureClass;
import com.bericotech.clavin.gazetteer.FeatureCode;
import com.bericotech.clavin.gazetteer.Gazetteer;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.gazetteer.LuceneGazetteer;
import com.bericotech.clavin.resolver.MultipartLocationName;
import com.bericotech.clavin.resolver.MultipartLocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.bericotech.clavin.resolver.ResolvedMultipartLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import io.lumify.core.cmdline.CommandLineBase;
import io.lumify.core.ingest.term.extraction.TermExtractionResult;
import io.lumify.core.ingest.term.extraction.TermMention;
import io.lumify.core.ingest.term.extraction.TermRelationship;
import io.lumify.core.model.ontology.Concept;
import io.lumify.core.model.ontology.OntologyLumifyProperties;
import io.lumify.core.model.ontology.OntologyRepository;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.model.workQueue.WorkQueueRepository;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.mapping.DocumentMapping;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.securegraph.Graph;
import org.securegraph.Vertex;
import org.securegraph.VertexBuilder;
import org.securegraph.Visibility;
import org.securegraph.type.GeoPoint;

/**
 * This application reads a specified Global Terrorism Database CSV file and loads
 * the entities and relationships it contains into the configured Lumify instance.
 */
public class GtdIngest extends CommandLineBase {
    /**
     * The logger.
     */
    private static final LumifyLogger LOG = LumifyLoggerFactory.getLogger(GtdIngest.class);

    /**
     * The multi-value key.
     */
    private static final String MULTI_VALUE_KEY = GtdIngest.class.getName();

    /**
     * The GTD input file option.
     */
    private static final String GTD_FILE_OPTION = "inputFile";

    /**
     * The GTD mapping path option.
     */
    private static final String GTD_MAPPING_OPTION = "mapping";

    /**
     * The visibility option.
     */
    private static final String VISIBILITY_OPTION = "visibility";

    /**
     * The GTD document mapping.
     */
    private static final String DEFAULT_GTD_MAPPING_FILE = "gtd.mapping.json";

    /**
     * The configuration option for the CLAVIN index directory.
     */
    private static final String CLAVIN_INDEX_DIRECTORY_CFG = "clavin.indexDirectory";

    /**
     * The IRI of the root Location concept.
     */
    private static final String LOCATION_ROOT_IRI = "http://lumify.io/dev#location";

    /**
     * The city property of location terms.
     */
    private static final String CITY_PROPERTY = "http://lumify.io/gtd#city";

    /**
     * The administrative district property of location terms.
     */
    private static final String ADMIN_PROPERTY = "http://lumify.io/gtd#adminDistrict";

    /**
     * The country property of administrative terms.
     */
    private static final String COUNTRY_PROPERTY = "http://lumify.io/gtd#country";

    /**
     * The default visibilty.
     */
    private static final String DEFAULT_VISIBILITY = "";

    /**
     * The shared object mapper.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * The configured input file.
     */
    private File inputFile;

    /**
     * The configured DocumentMapping.
     */
    private DocumentMapping mapping;

    /**
     * The CLAVIN gazetteer.
     */
    private Gazetteer gazetteer;

    /**
     * The location resolver.
     */
    private MultipartLocationResolver locationResolver;

    /**
     * The ontology repository.
     */
    private OntologyRepository ontologyRepository;

    /**
     * The set of Location concepts that should be normalized with CLAVIN.
     */
    private Set<String> locationConcepts;

    /**
     * The visibility for all ingested elements and properties.
     */
    private Visibility visibility;

    /**
     * The cache of created term vertices.
     */
    private Cache<Object, Vertex> termCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    public static void main(String[] args) throws Exception {
        int res = new GtdIngest().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(final CommandLine cmd) throws Exception {
        init();

        LOG.info("Ingesting GTD Records from: %s", inputFile.getAbsolutePath());
        long start = System.currentTimeMillis();

        Reader gtdIn = new InputStreamReader(new FileInputStream(inputFile), Charset.forName("UTF-8"));
        Iterator<TermExtractionResult> termIter = mapping.mapDocumentElements(gtdIn, "gtd-ingest", MULTI_VALUE_KEY, visibility);
        int row = 0;
        int errorCount = 0;
        while (termIter.hasNext()) {
            try {
                TermExtractionResult ter = termIter.next();
                // copy list from result so we can modify in-place
                List<TermMention> mentions = new ArrayList<TermMention>(ter.getTermMentions());
                for (TermMention term : mentions) {
                    // resolve any discovered locations
                    if (locationConcepts.contains(term.getOntologyClassUri())) {
                        TermMention newLoc = resolveLocation(term, "gtd-ingest");
                        if (newLoc != null) {
                            ter.replace(term, newLoc);
                            LOG.debug("[%d]: Replaced %s with %s", row, term.getSign(), newLoc.getSign());
                        }
                    }
                }
                Map<TermMention, Vertex> vertexMap = new HashMap<TermMention, Vertex>();
                // convert the TermMentions to GraphVertices
                for (TermMention term : ter.getTermMentions()) {
                    vertexMap.put(term, toVertex(term));
                }
                Graph graph = getGraph();
                // add discovered relationships to the graph
                for (TermRelationship rel : ter.getRelationships()) {
                    Vertex src = vertexMap.get(rel.getSourceTermMention());
                    Vertex dest = vertexMap.get(rel.getDestTermMention());
                    if (src != null && dest != null) {
                        graph.addEdge(src, dest, rel.getLabel(), rel.getVisibility(), getAuthorizations());
                    }
                }
                graph.flush();
            } catch (Exception e) {
                errorCount++;
                LOG.error("[%d]: Error ingesting row", row, e);
            } finally {
                row++;
                if ((row % 1000) == 0) {
                    LOG.info("Ingested %d rows (%d errors)", row, errorCount);
                }
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("GTD Ingest Complete.");
        LOG.info("Ingested %d Records in %.3f seconds with %d errors", row, (double)(end - start)/1000.0d, errorCount);

        return 0;
    }

    protected Vertex toVertex(final TermMention term) {
        // check to see if this Vertex exists in the cache
        Vertex vertex = term.getId() != null ? termCache.getIfPresent(term.getId()) : null;
        if (vertex != null) {
            // vertex was cached; return it
            return vertex;
        }

        // vertex was not found in cache; check the Graph to see if it already exists
        vertex = getGraph().getVertex(term.getId(), getAuthorizations());
        if (vertex == null) {
            // vertex does not exist; create it
            Visibility vis = term.getVisibility();
            VertexBuilder v = getGraph().prepareVertex(term.getId(), vis, getAuthorizations());
            OntologyLumifyProperties.CONCEPT_TYPE.addPropertyValue(v, MULTI_VALUE_KEY, term.getOntologyClassUri(), vis);
            LumifyProperties.TITLE.addPropertyValue(v, MULTI_VALUE_KEY, term.getSign(), vis);
            for (Map.Entry<String, Object> prop : term.getPropertyValue().entrySet()) {
                v.addPropertyValue(MULTI_VALUE_KEY, prop.getKey(), prop.getValue(), vis);
            }
            vertex = v.save();
            getGraph().flush();
            WorkQueueRepository queueRepo = getWorkQueueRepository();
            queueRepo.pushGraphPropertyQueue(vertex, LumifyProperties.TITLE.getProperty(vertex));
            for (String key : term.getPropertyValue().keySet()) {
                queueRepo.pushGraphPropertyQueue(vertex, vertex.getProperty(key));
            }
        }
        termCache.put(vertex.getId(), vertex);
        return vertex;
    }

    protected TermMention resolveLocation(final TermMention location, final String processId) {
        TermMention resolved = null;

        try {
            Map<String, Object> props = location.getPropertyValue();
            String cityStr = (String) props.get(CITY_PROPERTY);
            String adminStr = (String) props.get(ADMIN_PROPERTY);
            String countryStr = (String) props.get(COUNTRY_PROPERTY);
            MultipartLocationName mpName = new MultipartLocationName(cityStr, adminStr, countryStr);
            ResolvedMultipartLocation resMpLoc = locationResolver.resolveMultipartLocation(mpName, false);

            // select the most specific resolved location for use as the TermMention
            ResolvedLocation resolvedLoc = resMpLoc.getCity();
            if (resolvedLoc == null) {
                resolvedLoc = resMpLoc.getState();
            }
            if (resolvedLoc == null) {
                resolvedLoc = resMpLoc.getCountry();
            }
            // if we successfully resolved a location, configure the resolved mention
            if (resolvedLoc != null) {
                GeoName geoname = resolvedLoc.getGeoname();
                String id = String.format("CLAVIN-%d", geoname.getGeonameID());
                GeoPoint geoPoint = new GeoPoint(geoname.getLatitude(), geoname.getLongitude(), location.getSign());
                resolved = new TermMention.Builder(location)
                        .id(id)
                        .resolved(true)
                        .useExisting(true)
                        .sign(toSign(geoname))
                        .ontologyClassUri(toOntologyClassURI(geoname, location.getOntologyClassUri()))
                        .setProperty(GEO_LOCATION.getKey(), GEO_LOCATION.wrap(geoPoint))
                        .setProperty(SOURCE.getKey(), "CLAVIN")
                        .process(processId)
                        .build();
            }
        } catch (ClavinException ce) {
            LOG.error("Unable to resolve location for %s.", location.getSign(), ce);
        }

        return resolved;
    }

    protected String toSign(final GeoName geoname) {
        GeoName admin1 = geoname;
        while (admin1 != null && !admin1.getFeatureCode().name().startsWith("ADM1")) {
            admin1 = admin1.getParent();
        }
        String admin1Arg = admin1 != null && !admin1.getName().trim().isEmpty() ? String.format("%s, ", admin1.getName().trim()) : "";
        return String.format("%s (%s%s)", geoname.getName(), admin1Arg, geoname.getPrimaryCountryCode());
    }

    protected String toOntologyClassURI(final GeoName geoname, final String defaultValue) {
        FeatureCode code = geoname.getFeatureCode();
        String uri = defaultValue;
        if (geoname.isTopLevelAdminDivision()) {
            uri = "http://lumify.io/dev#country";
        } else if (code == FeatureCode.ADM1 || code == FeatureCode.ADM1H) {
            uri = "http://lumify.io/dev#state";
        } else if (code != null && code.getFeatureClass() == FeatureClass.P) {
            uri = "http://lumify.io/dev#city";
        }
        return uri;
    }

    protected void init() throws Exception {
        // initialize the CLAVIN index
        String clavinPath = getConfiguration().get(CLAVIN_INDEX_DIRECTORY_CFG);
        if (clavinPath != null) {
            try {
                gazetteer = new LuceneGazetteer(new File(clavinPath));
                locationResolver = new MultipartLocationResolver(gazetteer);
            } catch (ClavinException ce) {
                LOG.error("Unable to initialize CLAVIN with index at [%s]. Location resolution will be skipped.",
                        clavinPath, ce);
            }
        }

        // determine the set of concepts we will be resolving with CLAVIN, but only
        // if the CLAVIN index was successfully configured
        locationConcepts = new HashSet<String>();
        if (locationResolver != null) {
            List<Concept> targetConcepts = ontologyRepository.getConceptAndChildrenByIRI(LOCATION_ROOT_IRI);
            if (targetConcepts != null && !targetConcepts.isEmpty()) {
                for (Concept con : targetConcepts) {
                    locationConcepts.add(con.getTitle());
                }
            } else {
                LOG.warn("Unable to find target concepts for root concept [%s]. Location resolution will be skipped.",
                        "foo");
            }
        }
    }

    @Override
    protected Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption(OptionBuilder
                .withLongOpt(GTD_FILE_OPTION)
                .withDescription("The GTD CSV file to import.")
                .hasArg()
                .isRequired()
                .create('i'));

        opts.addOption(OptionBuilder
                .withLongOpt(GTD_MAPPING_OPTION)
                .withDescription("The path to the GTD mapping description.")
                .hasArg()
                .create('m'));

        opts.addOption(OptionBuilder
                .withLongOpt(VISIBILITY_OPTION)
                .withDescription("The visiblity string for all ingested elements. Default: \"\"")
                .hasArg()
                .create('v'));

        return opts;
    }

    @Override
    protected void processOptions(final CommandLine cmd) throws Exception {
        super.processOptions(cmd);

        String path = cmd.getOptionValue(GTD_FILE_OPTION);
        if (path == null) {
            throw new IllegalStateException("Input file (--inputFile) is required.");
        }
        inputFile = new File(path);
        if (!(inputFile.isFile() && inputFile.canRead())) {
            throw new IllegalStateException(String.format("Unable to read input file: %s", inputFile.getAbsolutePath()));
        }

        String mappingPath = cmd.getOptionValue(GTD_MAPPING_OPTION);
        InputStream mappingIn;
        if (mappingPath != null) {
            mappingIn = new FileInputStream(mappingPath);
        } else {
            mappingIn = ClassLoader.getSystemResourceAsStream(DEFAULT_GTD_MAPPING_FILE);
        }
        mapping = JSON_MAPPER.readValue(mappingIn, DocumentMapping.class);

        String vis = cmd.getOptionValue(VISIBILITY_OPTION, DEFAULT_VISIBILITY);
        visibility = new Visibility(vis);
    }

    @Inject
    public void setOntologyRepository(final OntologyRepository ontRepo) {
        this.ontologyRepository = ontRepo;
    }
}
