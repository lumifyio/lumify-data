package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.OutputResult;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KnownEntityExtractor extends EntityExtractor {
    private FileSystem fs;
    private String pathPrefix;

    private static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private AhoCorasick tree = new AhoCorasick();

    @Override
    void setup(Mapper.Context context, User user) throws IOException {
        this.pathPrefix = context.getConfiguration().get(PATH_PREFIX_CONFIG, DEFAULT_PATH_PREFIX);
        this.fs = FileSystem.get(context.getConfiguration());
        loadDictionaries();
    }

    @Override
    List<ExtractedEntity> extract(Artifact artifact, String text) throws Exception {
        ArrayList<ExtractedEntity> result = new ArrayList<ExtractedEntity>();
        List<OutputResult> searchResults = tree.completeSearch(text, false, false);
        for (OutputResult searchResult : searchResults) {
            result.add(searchResultToExtractedEntity(artifact, searchResult));
        }
        return result;
    }

    private ExtractedEntity searchResultToExtractedEntity(Artifact artifact, OutputResult searchResult) {
        Match match = (Match) searchResult.getOutput();

        long start = searchResult.getStartIndex();
        long end = searchResult.getLastIndex();
        TermMention termMention = new TermMention(new TermMentionRowKey(artifact.getRowKey().toString(), start, end));
        termMention.getMetadata().setSign(match.getEntityTitle());
        termMention.getMetadata().setConcept(match.getConceptTitle());

        GraphVertex vertex = new InMemoryGraphVertex();
        vertex.setProperty(PropertyName.TITLE, match.getEntityTitle());

        return new ExtractedEntity(termMention, vertex);
    }

    private void loadDictionaries() throws IOException {
        Path hdfsDirectory = new Path(getPathPrefix() + "/conf/knowEntities/dictionaries");
        for (FileStatus dictionaryFileStatus : getFS().listStatus(hdfsDirectory)) {
            Path hdfsPath = dictionaryFileStatus.getPath();
            if (hdfsPath.getName().startsWith(".") || !hdfsPath.getName().endsWith(".dict")) {
                continue;
            }
            String conceptName = FilenameUtils.getBaseName(hdfsPath.getName());
            InputStream dictionaryInputStream = getFS().open(hdfsPath);
            try {
                addDictionaryEntriesToTree(conceptName, dictionaryInputStream);
            } finally {
                dictionaryInputStream.close();
            }
        }
        tree.prepare();
    }

    private void addDictionaryEntriesToTree(String type, InputStream dictionaryInputStream) throws IOException {
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

    protected String getPathPrefix() {
        return this.pathPrefix;
    }

    protected FileSystem getFS() {
        return this.fs;
    }

    private class Match {

        private final String conceptTitle;
        private final String entityTitle;
        private final String matchText;

        public Match(String type, String matchText, String entityTitle) {
            this.conceptTitle = type;
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
