package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OpenNlpEntityExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(OpenNlpEntityExtractor.class);
    public static final String PATH_PREFIX_CONFIG = "termextraction.opennlp.pathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private static final int NEW_LINE_CHARACTER_LENGTH = 1;

    private Tokenizer tokenizer;
    private List<TokenNameFinder> finders;
    private User user;

    public void prepare(Configuration configuration, User user) throws URISyntaxException, IOException, InterruptedException {
        String pathPrefix = configuration.get(PATH_PREFIX_CONFIG, DEFAULT_PATH_PREFIX);
        String hdfsRootDir = configuration.get(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL);
        checkNotNull(hdfsRootDir, com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL + " is a required configuration parameter");
        FileSystem fs = FileSystem.get(new URI(hdfsRootDir), configuration, "hadoop");
        this.user = user;
        this.tokenizer = loadTokenizer(pathPrefix, fs);
        this.finders = loadFinders(pathPrefix, fs);
    }

    public TermExtractionResult extract(InputStream textInputStream)
            throws Exception {
        TermExtractionResult termExtractionResult = new TermExtractionResult();

        ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new InputStreamReader(textInputStream));
        String line;
        int charOffset = 0;

        LOGGER.debug("Processing artifact content stream");
        while ((line = untokenizedLineStream.read()) != null) {
            ArrayList<TermMention> newTermMenitons = processLine(line, charOffset);
            termExtractionResult.addAllTermMentions(newTermMenitons);
            charOffset += line.length() + NEW_LINE_CHARACTER_LENGTH;
        }

        untokenizedLineStream.close();
        LOGGER.debug("Stream processing completed");

        return termExtractionResult;
    }

    private ArrayList<TermMention> processLine(String line, int charOffset) {
        ArrayList<TermMention> termMentions = new ArrayList<TermMention>();
        String tokenList[] = tokenizer.tokenize(line);
        Span[] tokenListPositions = tokenizer.tokenizePos(line);
        for (TokenNameFinder finder : finders) {
            Span[] foundSpans = finder.find(tokenList);
            for (Span span : foundSpans) {
                TermMention termMention = createTermMention(charOffset, span, tokenList, tokenListPositions);
                termMentions.add(termMention);
            }
            finder.clearAdaptiveData();
        }
        return termMentions;
    }

    private TermMention createTermMention(int charOffset, Span foundName, String[] tokens, Span[] tokenListPositions) {
        String name = Span.spansToStrings(new Span[]{foundName}, tokens)[0];
        int start = charOffset + tokenListPositions[foundName.getStart()].getStart();
        int end = charOffset + tokenListPositions[foundName.getEnd() - 1].getEnd();
        String type = foundName.getType();
        String ontologyClassUri;
        if ("location".equals(type)) {
            ontologyClassUri = "http://lumify.io/dev#location";
        } else if ("organization".equals(type)) {
            ontologyClassUri = "http://lumify.io/dev#organization";
        } else if ("person".equals(type)) {
            ontologyClassUri = "http://lumify.io/dev#person";
        } else {
            ontologyClassUri = "http://www.w3.org/2002/07/owl#Thing";
        }
        return new TermMention.Builder()
                .start(start)
                .end(end)
                .sign(name)
                .ontologyClassUri(ontologyClassUri)
                .resolved(false)
                .useExisting(true)
                .process(getClass().getName())
                .build();
    }

    protected abstract List<TokenNameFinder> loadFinders(String pathPrefix, FileSystem fs) throws IOException;

    protected Tokenizer loadTokenizer(String pathPrefix, FileSystem fs) throws IOException {
        Path tokenizerHdfsPath = new Path(pathPrefix + "/en-token.bin");

        TokenizerModel tokenizerModel = null;
        InputStream tokenizerModelInputStream = fs.open(tokenizerHdfsPath);
        try {
            tokenizerModel = new TokenizerModel(tokenizerModelInputStream);
        } finally {
            tokenizerModelInputStream.close();
        }

        return new TokenizerME(tokenizerModel);
    }

    public User getUser() {
        return user;
    }
}
