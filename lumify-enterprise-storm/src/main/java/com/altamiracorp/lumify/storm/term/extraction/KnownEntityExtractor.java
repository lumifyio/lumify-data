package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
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
import java.util.List;

public class KnownEntityExtractor {
    private FileSystem fs;
    private String pathPrefix;

    private static final String PATH_PREFIX_CONFIG = "termextraction.knownEntities.pathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private final AhoCorasick tree = new AhoCorasick();

    public void prepare(Configuration configuration, User user) throws IOException {
        pathPrefix = configuration.get(PATH_PREFIX_CONFIG, DEFAULT_PATH_PREFIX);
        fs = FileSystem.get(configuration);
        loadDictionaries();
    }

    public TermExtractionResult extract(InputStream textInputStream) throws IOException {
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        String text = IOUtils.toString(textInputStream); // TODO convert AhoCorasick to use InputStream
        List<OutputResult> searchResults = tree.completeSearch(text, false, true);
        for (OutputResult searchResult : searchResults) {
            termExtractionResult.add(outputResultToTermMention(searchResult));
        }
        return termExtractionResult;
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

    private void loadDictionaries() throws IOException {
        Path hdfsDirectory = new Path(getPathPrefix() + "dictionaries");
        if (!getFS().exists(hdfsDirectory)) {
            getFS().mkdirs(hdfsDirectory);
        }
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
        return pathPrefix;
    }

    protected FileSystem getFS() {
        return fs;
    }

    private class Match {

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
