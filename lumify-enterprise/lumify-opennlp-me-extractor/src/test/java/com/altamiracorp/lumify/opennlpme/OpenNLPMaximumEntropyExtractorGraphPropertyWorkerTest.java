package com.altamiracorp.lumify.opennlpme;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.graphProperty.TermMentionFilter;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.inmemory.InMemoryAuthorizations;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.altamiracorp.securegraph.util.IterableUtils.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class OpenNLPMaximumEntropyExtractorGraphPropertyWorkerTest {
    private static final String RESOURCE_CONFIG_DIR = "/fs/conf/opennlp";

    private OpenNLPMaximumEntropyExtractorGraphPropertyWorker extractor;

    @Mock
    private Mapper.Context context;

    @Mock
    private User user;
    private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
            + "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
            + "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

    List<TermMention> termMentions;

    private InMemoryAuthorizations authorizations;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();

        configuration.set(OpenNLPMaximumEntropyExtractorGraphPropertyWorker.PATH_PREFIX_CONFIG, "file:///" + getClass().getResource(RESOURCE_CONFIG_DIR).getFile());
        configuration.set(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL, "");
        extractor = new OpenNLPMaximumEntropyExtractorGraphPropertyWorker() {
            @Override
            protected List<TermMentionWithGraphVertex> saveTermMentions(Vertex artifactGraphVertex, Iterable<TermMention> termMentions, Visibility visibility) {
                OpenNLPMaximumEntropyExtractorGraphPropertyWorkerTest.this.termMentions = toList(termMentions);
                return null;
            }
        };

        Map stormConf = new HashMap();
        FileSystem hdfsFileSystem = FileSystem.get(new Configuration());
        authorizations = new InMemoryAuthorizations();
        Injector injector = null;
        List<TermMentionFilter> termMentionFilters = new ArrayList<TermMentionFilter>();
        GraphPropertyWorkerPrepareData workerPrepareData = new GraphPropertyWorkerPrepareData(stormConf, termMentionFilters, hdfsFileSystem, user, authorizations, injector);
        extractor.prepare(workerPrepareData);
    }

    @Test
    public void testEntityExtraction() throws Exception {
        GraphPropertyWorkData workData = null;
        extractor.execute(new ByteArrayInputStream(text.getBytes()), workData);
        HashMap<String, TermMention> extractedTerms = new HashMap<String, TermMention>();
        for (TermMention term : termMentions) {
            extractedTerms.put(term.getSign() + "-" + term.getOntologyClassUri(), term);
        }
        assertTrue("A person wasn't found", extractedTerms.containsKey("Bob Robertson-http://lumify.io/dev#person"));
        TermMention bobRobertsonMentions = extractedTerms.get("Bob Robertson-http://lumify.io/dev#person");
        assertEquals(31, bobRobertsonMentions.getStart());
        assertEquals(44, bobRobertsonMentions.getEnd());


        assertTrue("A location wasn't found", extractedTerms.containsKey("Benghazi-http://lumify.io/dev#location"));
        TermMention benghaziMentions = extractedTerms.get("Benghazi-http://lumify.io/dev#location");
        assertEquals(189, benghaziMentions.getStart());
        assertEquals(197, benghaziMentions.getEnd());

        assertTrue("An organization wasn't found", extractedTerms.containsKey("CNN-http://lumify.io/dev#organization"));
        TermMention cnnMentions = extractedTerms.get("CNN-http://lumify.io/dev#organization");
        assertEquals(151, cnnMentions.getStart());
        assertEquals(154, cnnMentions.getEnd());
    }
}
