package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.model.termMention.TermMention;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class OpenNlpDictionaryEntityExtractorTest extends BaseExtractorTest {

    private OpenNlpDictionaryEntityExtractor extractor;
    private Context context;

    private String text = "This is a sentence that is going to tell you about a guy named "
            + "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

    @Before
    public void setUp() throws IOException {
        context = mock(Context.class);
        Configuration config = new Configuration();
        config.set("nlpConfPathPrefix", Thread.currentThread().getContextClassLoader().getResource("fs/").toString());
        when(context.getConfiguration()).thenReturn(config);
        extractor = new OpenNlpDictionaryEntityExtractor();
    }

    @Test
    public void testEntityExtraction() throws Exception {
        extractor.setup(context);
        List<TermMention> terms = extractor.extract(createArtifact(text), text);
        assertEquals(3, terms.size());
        ArrayList<String> signs = new ArrayList<String>();
        for (TermMention term : terms) {
            signs.add(term.getMetadata().getSign());
        }

        assertTrue("Bob Robertson not found", signs.contains("Bob Robertson"));
        assertTrue("Altamira Corporation not found", signs.contains("Altamira Corporation"));
        assertTrue("Boston , MA not found", signs.contains("Boston , MA"));
    }

    @Test
    public void testEntityExtractionSetsMentionRelativeToArtifactNotSentence() throws Exception {
        extractor.setup(context);

        Collection<TermMention> terms = extractor.extract(createArtifact(text), text);
        boolean found = false;
        for (TermMention term : terms) {
            if (term.getMetadata().getSign().equals("Bob Robertson")) {
                found = true;
                assertEquals(63, term.getRowKey().getStartOffset());
                assertEquals(76, term.getRowKey().getEndOffset());
                break;
            }
        }
        assertTrue("Expected name not found!", found);
    }
}
