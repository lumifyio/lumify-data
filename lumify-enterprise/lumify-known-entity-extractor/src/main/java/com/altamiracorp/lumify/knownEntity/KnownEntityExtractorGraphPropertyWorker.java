package com.altamiracorp.lumify.knownEntity;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkResult;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.OutputResult;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.List;

public class KnownEntityExtractorGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(KnownEntityExtractorGraphPropertyWorker.class);
    private static final String PATH_PREFIX_CONFIG = "termextraction.knownEntities.pathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private AhoCorasick tree;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);
        String pathPrefix = (String) workerPrepareData.getStormConf().get(PATH_PREFIX_CONFIG);
        if (pathPrefix == null) {
            pathPrefix = DEFAULT_PATH_PREFIX;
        }
        FileSystem fs = workerPrepareData.getHdfsFileSystem();
        this.tree = loadDictionaries(fs, pathPrefix);
    }

    @Override
    public GraphPropertyWorkResult execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        String text = IOUtils.toString(in); // TODO convert AhoCorasick to use InputStream
        List<OutputResult> searchResults = tree.completeSearch(text, false, true);
        for (OutputResult searchResult : searchResults) {
            TermMention termMention = outputResultToTermMention(searchResult);
            saveTermMention(data.getVertex(), termMention, getUser(), data.getVertex().getVisibility(), getAuthorizations());
            getGraph().flush();
        }

        return new GraphPropertyWorkResult();
    }

    private TermMention outputResultToTermMention(OutputResult searchResult) {
        Match match = (Match) searchResult.getOutput();
        int start = searchResult.getStartIndex();
        int end = searchResult.getLastIndex();
        return new TermMention.Builder()
                .start(start)
                .end(end)
                .sign(match.getEntityTitle())
                .ontologyClassUri(match.getConceptTitle())
                .resolved(true)
                .useExisting(true)
                .process(getClass().getName())
                .build();
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        return !(mimeType == null || !mimeType.startsWith("text"));
    }

    private static AhoCorasick loadDictionaries(FileSystem fs, String pathPrefix) throws IOException {
        AhoCorasick tree = new AhoCorasick();
        Path hdfsDirectory = new Path(pathPrefix, "dictionaries");
        if (!fs.exists(hdfsDirectory)) {
            fs.mkdirs(hdfsDirectory);
        }
        for (FileStatus dictionaryFileStatus : fs.listStatus(hdfsDirectory)) {
            Path hdfsPath = dictionaryFileStatus.getPath();
            if (hdfsPath.getName().startsWith(".") || !hdfsPath.getName().endsWith(".dict")) {
                continue;
            }
            LOGGER.info("Loading known entity dictionary %s", hdfsPath.toString());
            String conceptName = FilenameUtils.getBaseName(hdfsPath.getName());
            conceptName = URLDecoder.decode(conceptName, "UTF-8");
            InputStream dictionaryInputStream = fs.open(hdfsPath);
            try {
                addDictionaryEntriesToTree(tree, conceptName, dictionaryInputStream);
            } finally {
                dictionaryInputStream.close();
            }
        }
        tree.prepare();
        return tree;
    }

    private static void addDictionaryEntriesToTree(AhoCorasick tree, String type, InputStream dictionaryInputStream) throws IOException {
        CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
        CsvListReader csvReader = new CsvListReader(new InputStreamReader(dictionaryInputStream), csvPrefs);
        List<String> line;
        while ((line = csvReader.read()) != null) {
            if (line.size() != 2) {
                throw new RuntimeException("Invalid number of entries on a line. Expected 2 found " + line.size());
            }
            tree.add(line.get(0), new Match(type, line.get(0), line.get(1)));
        }
    }

    private static class Match {
        private final String conceptTitle;
        private final String entityTitle;
        private final String matchText;

        public Match(String type, String matchText, String entityTitle) {
            conceptTitle = type;
            this.matchText = matchText;
            this.entityTitle = entityTitle;
        }

        private String getConceptTitle() {
            return conceptTitle;
        }

        private String getEntityTitle() {
            return entityTitle;
        }

        private String getMatchText() {
            return matchText;
        }

        @Override
        public String toString() {
            return matchText;
        }
    }
}
