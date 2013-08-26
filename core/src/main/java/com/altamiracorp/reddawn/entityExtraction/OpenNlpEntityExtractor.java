package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.model.termMention.TermMentionRowKey;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OpenNlpEntityExtractor extends EntityExtractor {
    private FileSystem fs;
    private String pathPrefix;

    private Tokenizer tokenizer;
    private List<TokenNameFinder> finders;

    private static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private static final int NEW_LINE_CHARACTER_LENGTH = 1;

    @Override
    public void setup(Context context) throws IOException {
        setPathPrefix(context.getConfiguration().get(PATH_PREFIX_CONFIG,
                DEFAULT_PATH_PREFIX));
        this.fs = FileSystem.get(context.getConfiguration());

        setTokenizer(loadTokenizer());
        setFinders(loadFinders());
    }

    @Override
    public Collection<TermMention> extract(Artifact artifact, String text)
            throws Exception {
        ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new StringReader(text));
        ArrayList<TermMention> termMentions = new ArrayList<TermMention>();
        String line;
        int charOffset = 0;
        while ((line = untokenizedLineStream.read()) != null) {
            ArrayList<TermMention> newTermMentions = processLine(artifact, line, charOffset);
            termMentions.addAll(newTermMentions);
            charOffset += line.length() + NEW_LINE_CHARACTER_LENGTH;
        }
        return termMentions;
    }

    private ArrayList<TermMention> processLine(Artifact artifact, String line, int charOffset) {
        ArrayList<TermMention> termMentions = new ArrayList<TermMention>();
        String tokenList[] = tokenizer.tokenize(line);
        Span[] tokenListPositions = tokenizer.tokenizePos(line);
        for (TokenNameFinder finder : finders) {
            Span[] foundSpans = finder.find(tokenList);
            for (Span span : foundSpans) {
                TermMention termMention = createTermMention(artifact, charOffset, span, tokenList, tokenListPositions);
                termMentions.add(termMention);
            }
            finder.clearAdaptiveData();
        }
        return termMentions;
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
        return this.pathPrefix;
    }

    protected FileSystem getFS() {
        return this.fs;
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
