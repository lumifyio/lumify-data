package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OpenNlpEntityExtractor implements EntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNlpEntityExtractor.class.getName());

    private FileSystem fs;
    private String pathPrefix;

    private Tokenizer tokenizer;
    private List<TokenNameFinder> finders;

    private static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";

    @Override
    public void setup(Context context) throws IOException {
        this.pathPrefix = context.getConfiguration().get(PATH_PREFIX_CONFIG,
                DEFAULT_PATH_PREFIX);
        this.fs = FileSystem.get(context.getConfiguration());

        setTokenizer(loadTokenizer());
        setFinders(loadFinders());
    }

    @Override
    public Collection<Term> extract(SentenceRowKey sentenceRowKey, String text)
            throws Exception {
        LOGGER.info("Extracting entities from sentence: " + sentenceRowKey.toString());
        ArrayList<Term> terms = new ArrayList<Term>();
        ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new StringReader(text));
        String line;
        int charOffset = 0;
        while ((line = untokenizedLineStream.read()) != null) {
            ArrayList<Term> newTerms = processLine(sentenceRowKey, line, charOffset);
            terms.addAll(newTerms);
            charOffset += line.length() + 1; // + 1 for new line character
        }
        return terms;
    }

    private ArrayList<Term> processLine(SentenceRowKey sentenceRowKey, String line, int charOffset) {
        ArrayList<Term> terms = new ArrayList<Term>();
        String tokenList[] = tokenizer.tokenize(line);
        Span[] tokenListPositions = tokenizer.tokenizePos(line);
        for (TokenNameFinder finder : finders) {
            Span[] foundSpans = finder.find(tokenList);
            for (Span span : foundSpans) {
                Term term = createTerm(sentenceRowKey, charOffset, span, tokenList, tokenListPositions);
                terms.add(term);
            }
            finder.clearAdaptiveData();
        }
        return terms;
    }

    private Term createTerm(SentenceRowKey sentenceRowKey, int charOffset, Span foundName, String[] tokens, Span[] tokenListPositions) {
        String sign = Span.spansToStrings(new Span[]{foundName}, tokens)[0];
        int termMentionStart = charOffset + tokenListPositions[foundName.getStart()].getStart();
        int termMentionEnd = charOffset + tokenListPositions[foundName.getEnd() - 1].getEnd();

        String concept = openNlpTypeToConcept(foundName.getType());
        TermRowKey termKey = new TermRowKey(sign, getModelName(), concept);
        TermMention termMention = new TermMention()
                .setArtifactKey(sentenceRowKey.getArtifactRowKey())
                        // .setArtifactKeySign("testArtifactKeySign") TODO what should go here?
                        // .setAuthor("testAuthor") TODO what should go here?
                .setMentionStart((long) termMentionStart)
                .setMentionEnd((long) termMentionEnd);
        Term term = new Term(termKey)
                .addTermMention(termMention);
        return term;
    }

    protected abstract List<TokenNameFinder> loadFinders() throws IOException;

    protected abstract String getModelName();

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

    private String openNlpTypeToConcept(String type) {
        return type; // TODO create a mapping for OpenNLP to UCD concepts
    }

}
