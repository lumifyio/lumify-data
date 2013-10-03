package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.storm.TextExtractedInfo;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class OpenNlpEntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNlpEntityExtractor.class);
    public static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private static final int NEW_LINE_CHARACTER_LENGTH = 1;

    private FileSystem fs;
    private String pathPrefix;
    private Tokenizer tokenizer;
    private List<TokenNameFinder> finders;
    private User user;

    public OpenNlpEntityExtractor(Configuration configuration, User user) throws IOException {
        setPathPrefix(configuration.get(PATH_PREFIX_CONFIG, DEFAULT_PATH_PREFIX));
        this.fs = FileSystem.get(configuration);
        this.user = user;

        setTokenizer(loadTokenizer());
        setFinders(loadFinders());
    }

    public TextExtractedInfo extract(InputStream textInputStream)
            throws Exception {
        TextExtractedInfo textExtractedInfo = new TextExtractedInfo();

        ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new InputStreamReader(textInputStream));
        String line;
        int charOffset = 0;

        LOGGER.debug("Processing artifact content stream");
        while ((line = untokenizedLineStream.read()) != null) {
            ArrayList<TextExtractedInfo.TermMention> newTermMenitons = processLine(line, charOffset);
            textExtractedInfo.addAll(newTermMenitons);
            charOffset += line.length() + NEW_LINE_CHARACTER_LENGTH;
        }

        untokenizedLineStream.close();
        LOGGER.debug("Stream processing completed");

        return textExtractedInfo;
    }

    private ArrayList<TextExtractedInfo.TermMention> processLine(String line, int charOffset) {
        ArrayList<TextExtractedInfo.TermMention> termMentions = new ArrayList<TextExtractedInfo.TermMention>();
        String tokenList[] = tokenizer.tokenize(line);
        Span[] tokenListPositions = tokenizer.tokenizePos(line);
        for (TokenNameFinder finder : finders) {
            Span[] foundSpans = finder.find(tokenList);
            for (Span span : foundSpans) {
                TextExtractedInfo.TermMention termMention = createTermMention(charOffset, span, tokenList, tokenListPositions);
                termMentions.add(termMention);
            }
            finder.clearAdaptiveData();
        }
        return termMentions;
    }

    private TextExtractedInfo.TermMention createTermMention(int charOffset, Span foundName, String[] tokens, Span[] tokenListPositions) {
        String name = Span.spansToStrings(new Span[]{foundName}, tokens)[0];
        int start = charOffset + tokenListPositions[foundName.getStart()].getStart();
        int end = charOffset + tokenListPositions[foundName.getEnd() - 1].getEnd();
        return new TextExtractedInfo.TermMention(start, end, name, foundName.getType());
    }

    protected abstract List<TokenNameFinder> loadFinders() throws IOException;

    protected String getPathPrefix() {
        return pathPrefix;
    }

    protected FileSystem getFS() {
        return fs;
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

    public User getUser() {
        return user;
    }
}
