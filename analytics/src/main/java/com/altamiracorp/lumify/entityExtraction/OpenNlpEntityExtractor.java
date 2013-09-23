package com.altamiracorp.lumify.entityExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.Artifact;

public abstract class OpenNlpEntityExtractor extends EntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNlpEntityExtractor.class);
    private FileSystem fs;
    private String pathPrefix;

    private User user;
    private Tokenizer tokenizer;
    private List<TokenNameFinder> finders;

    private static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private static final int NEW_LINE_CHARACTER_LENGTH = 1;

    @Override
    public void setup(Context context, User user) throws IOException {
        setPathPrefix(context.getConfiguration().get(PATH_PREFIX_CONFIG,
                DEFAULT_PATH_PREFIX));
        this.fs = FileSystem.get(context.getConfiguration());
        this.user = user;

        setTokenizer(loadTokenizer());
        setFinders(loadFinders());
    }

    @Override
    public List<ExtractedEntity> extract(Artifact artifact, String text)
            throws Exception {
        ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new StringReader(text));
        ArrayList<ExtractedEntity> extractedEntities = new ArrayList<ExtractedEntity>();
        String line;
        int charOffset = 0;

        LOGGER.debug("Processing artifact content stream");
        while ((line = untokenizedLineStream.read()) != null) {
            ArrayList<ExtractedEntity> newExtractedEntities = processLine(artifact, line, charOffset);
            extractedEntities.addAll(newExtractedEntities);
            charOffset += line.length() + NEW_LINE_CHARACTER_LENGTH;
        }

        untokenizedLineStream.close();
        LOGGER.debug("Stream processing completed");

        return extractedEntities;
    }

    private ArrayList<ExtractedEntity> processLine(Artifact artifact, String line, int charOffset) {
        ArrayList<ExtractedEntity> extractedEntities = new ArrayList<ExtractedEntity>();
        String tokenList[] = tokenizer.tokenize(line);
        Span[] tokenListPositions = tokenizer.tokenizePos(line);
        for (TokenNameFinder finder : finders) {
            Span[] foundSpans = finder.find(tokenList);
            for (Span span : foundSpans) {
                TermMention termMention = createTermMention(artifact, charOffset, span, tokenList, tokenListPositions);
                extractedEntities.add(new ExtractedEntity(termMention, null));
            }
            finder.clearAdaptiveData();
        }
        return extractedEntities;
    }

    private TermMention createTermMention(Artifact artifact, int charOffset, Span foundName, String[] tokens, Span[] tokenListPositions) {
        String name = Span.spansToStrings(new Span[]{foundName}, tokens)[0];
        int start = charOffset + tokenListPositions[foundName.getStart()].getStart();
        int end = charOffset + tokenListPositions[foundName.getEnd() - 1].getEnd();
        TermMention termMention = new TermMention(new TermMentionRowKey(artifact.getRowKey().toString(), start, end));
        termMention.getMetadata().setSign(name);
        termMention.getMetadata().setConcept(foundName.getType());
        return termMention;
    }

    protected abstract List<TokenNameFinder> loadFinders() throws IOException;

    protected String getPathPrefix() {
        return pathPrefix;
    }

    protected FileSystem getFS() {
        return fs;
    }

    protected User getUser() {
        return user;
    }

    protected Tokenizer loadTokenizer() throws IOException {
        Path tokenizerHdfsPath = new Path(pathPrefix
                + "/conf/opennlp/en-token.bin");

        TokenizerModel tokenizerModel = null;
        InputStream tokenizerModelInputStream = fs.open(tokenizerHdfsPath);
        try {
            tokenizerModel = new TokenizerModel(tokenizerModelInputStream);
        } finally {
            tokenizerModelInputStream.close();
        }

        return new TokenizerME(tokenizerModel);
    }

    protected void setFinders(List<TokenNameFinder> finders) {
        this.finders = finders;

    }

    protected void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    protected void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

}
