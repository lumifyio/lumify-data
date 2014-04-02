package com.altamiracorp.lumify.opennlpme;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpenNLPMaximumEntropyExtractorGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(OpenNLPMaximumEntropyExtractorGraphPropertyWorker.class);
    public static final String PATH_PREFIX_CONFIG = "termextraction.opennlp.pathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private static final int NEW_LINE_CHARACTER_LENGTH = 1;

    private List<TokenNameFinder> finders;
    private Tokenizer tokenizer;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        String pathPrefix = (String) workerPrepareData.getStormConf().get(PATH_PREFIX_CONFIG);
        if (pathPrefix == null) {
            pathPrefix = DEFAULT_PATH_PREFIX;
        }
        this.tokenizer = loadTokenizer(pathPrefix, workerPrepareData.getHdfsFileSystem());
        this.finders = loadFinders(pathPrefix, workerPrepareData.getHdfsFileSystem());
    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new InputStreamReader(in));
        String line;
        int charOffset = 0;

        LOGGER.debug("Processing artifact content stream");
        List<TermMention> termMenitons = new ArrayList<TermMention>();
        while ((line = untokenizedLineStream.read()) != null) {
            ArrayList<TermMention> newTermMentions = processLine(line, charOffset, data.getVertex().getVisibility());
            termMenitons.addAll(newTermMentions);
            getGraph().flush();
            charOffset += line.length() + NEW_LINE_CHARACTER_LENGTH;
        }
        saveTermMentions(data.getVertex(), termMenitons);

        untokenizedLineStream.close();
        LOGGER.debug("Stream processing completed");
    }

    private ArrayList<TermMention> processLine(String line, int charOffset, Visibility visibility) {
        ArrayList<TermMention> termMentions = new ArrayList<TermMention>();
        String tokenList[] = tokenizer.tokenize(line);
        Span[] tokenListPositions = tokenizer.tokenizePos(line);
        for (TokenNameFinder finder : finders) {
            Span[] foundSpans = finder.find(tokenList);
            for (Span span : foundSpans) {
                TermMention termMention = createTermMention(charOffset, span, tokenList, tokenListPositions, visibility);
                termMentions.add(termMention);
            }
            finder.clearAdaptiveData();
        }
        return termMentions;
    }

    private TermMention createTermMention(int charOffset, Span foundName, String[] tokens, Span[] tokenListPositions, Visibility visibility) {
        String name = Span.spansToStrings(new Span[]{foundName}, tokens)[0];
        int start = charOffset + tokenListPositions[foundName.getStart()].getStart();
        int end = charOffset + tokenListPositions[foundName.getEnd() - 1].getEnd();
        String type = foundName.getType();
        String ontologyClassUri;

        // TODO abstract this out so that it doesn't depend on the dev ontology.
        if ("location".equals(type)) {
            ontologyClassUri = "http://lumify.io/dev#location";
        } else if ("organization".equals(type)) {
            ontologyClassUri = "http://lumify.io/dev#organization";
        } else if ("person".equals(type)) {
            ontologyClassUri = "http://lumify.io/dev#person";
        } else {
            ontologyClassUri = "http://www.w3.org/2002/07/owl#Thing";
        }
        return new TermMention.Builder(start, end, name, ontologyClassUri, visibility)
                .resolved(false)
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

    protected List<TokenNameFinder> loadFinders(String pathPrefix, FileSystem fs)
            throws IOException {
        Path finderHdfsPaths[] = {
                new Path(pathPrefix + "/en-ner-location.bin"),
                new Path(pathPrefix + "/en-ner-organization.bin"),
                new Path(pathPrefix + "/en-ner-person.bin")};
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        for (Path finderHdfsPath : finderHdfsPaths) {
            InputStream finderModelInputStream = fs.open(finderHdfsPath);
            TokenNameFinderModel model = null;
            try {
                model = new TokenNameFinderModel(finderModelInputStream);
            } finally {
                finderModelInputStream.close();
            }
            NameFinderME finder = new NameFinderME(model);
            finders.add(finder);
        }

        return finders;
    }

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
}
