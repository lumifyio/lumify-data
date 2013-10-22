package com.altamiracorp.lumify.storm.term.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.AccumuloSession;

@RunWith(MockitoJUnitRunner.class)
public class OpenNlpMEEntityExtractorTest {
    private static final String RESOURCE_CONFIG_DIR = "/fs/conf/opennlp";

    private OpenNlpMaximumEntropyEntityExtractor extractor;

    @Mock
    private Context context;

    @Mock
    private User user;
    private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
            + "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
            + "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

    @Before
    public void setUp() throws InterruptedException, IOException, URISyntaxException {
        Configuration configuration = new Configuration();
<<<<<<< HEAD
        configuration.set(OpenNlpEntityExtractor.PATH_PREFIX_CONFIG, "file:///" + getClass().getResource(RESOURCE_CONFIG_DIR).getFile());
        configuration.set(AccumuloSession.HADOOP_URL, "");

=======
        configuration.set(OpenNlpEntityExtractor.PATH_PREFIX_CONFIG, "file:///" + System.getProperty("user.dir") + "/storm/src/test/resources/fs/conf/opennlp/");
        configuration.set(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL, "");
>>>>>>> Initial configuration refactor
        extractor = new OpenNlpMaximumEntropyEntityExtractor();
        extractor.prepare(configuration, user);
    }

    @Test
    public void testEntityExtraction() throws Exception {
        TermExtractionResult results = extractor.extract(new ByteArrayInputStream(text.getBytes()));
        HashMap<String, TermExtractionResult.TermMention> extractedTerms = new HashMap<String, TermExtractionResult.TermMention>();
        for (TermExtractionResult.TermMention term : results.getTermMentions()) {
            extractedTerms.put(term.getSign() + "-" + term.getOntologyClassUri(), term);
        }
        assertTrue("A person wasn't found", extractedTerms.containsKey("Bob Robertson-person"));
        TermExtractionResult.TermMention bobRobertsonMentions = extractedTerms.get("Bob Robertson-person");
        assertEquals(31, bobRobertsonMentions.getStart());
        assertEquals(44, bobRobertsonMentions.getEnd());


        assertTrue("A location wasn't found", extractedTerms.containsKey("Benghazi-location"));
        TermExtractionResult.TermMention benghaziMentions = extractedTerms.get("Benghazi-location");
        assertEquals(189, benghaziMentions.getStart());
        assertEquals(197, benghaziMentions.getEnd());

        assertTrue("An organization wasn't found", extractedTerms.containsKey("CNN-organization"));
        TermExtractionResult.TermMention cnnMentions = extractedTerms.get("CNN-organization");
        assertEquals(151, cnnMentions.getStart());
        assertEquals(154, cnnMentions.getEnd());
    }
}
