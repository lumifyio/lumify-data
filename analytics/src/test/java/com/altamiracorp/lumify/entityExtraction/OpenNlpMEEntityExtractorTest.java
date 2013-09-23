package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.termMention.TermMention;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class OpenNlpMEEntityExtractorTest extends BaseExtractorTest {
    private OpenNlpMaximumEntropyEntityExtractor extractor;

    @Mock
    private Context context;

    @Mock
    private User user;
    private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
            + "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
            + "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        Configuration config = new Configuration();
        config.set("nlpConfPathPrefix", Thread.currentThread().getContextClassLoader().getResource("fs/").toString());
        doReturn(config).when(context).getConfiguration();
        extractor = new OpenNlpMaximumEntropyEntityExtractor();
    }

    @Test
    public void testEntityExtraction() throws Exception {
        extractor.setup(context,user);
        Collection<ExtractedEntity> terms = extractor.extract(createArtifact(text), text);
        HashMap<String, TermMention> extractedTerms = new HashMap<String, TermMention>();
        for (ExtractedEntity term : terms) {
            extractedTerms.put(term.getTermMention().getMetadata().getSign() + "-" + term.getTermMention().getMetadata().getConcept(), term.getTermMention());
        }
        assertTrue("A person wasn't found", extractedTerms.containsKey("Bob Robertson-person"));
        TermMention bobRobertsonMentions = extractedTerms.get("Bob Robertson-person");
        assertEquals(31, bobRobertsonMentions.getRowKey().getStartOffset());
        assertEquals(44, bobRobertsonMentions.getRowKey().getEndOffset());


        assertTrue("A location wasn't found", extractedTerms.containsKey("Benghazi-location"));
        TermMention benghaziMentions = extractedTerms.get("Benghazi-location");
        assertEquals(189, benghaziMentions.getRowKey().getStartOffset());
        assertEquals(197, benghaziMentions.getRowKey().getEndOffset());

        assertTrue("An organization wasn't found", extractedTerms.containsKey("CNN-organization"));
        TermMention cnnMentions = extractedTerms.get("CNN-organization");
        assertEquals(151, cnnMentions.getRowKey().getStartOffset());
        assertEquals(154, cnnMentions.getRowKey().getEndOffset());
    }
}
