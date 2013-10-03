package com.altamiracorp.lumify.entityExtraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.StringList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.termMention.TermMention;

@RunWith(JUnit4.class)
public class OpenNlpDictionaryEntityExtractorTest extends BaseExtractorTest {

    private OpenNlpDictionaryEntityExtractor extractor;

    @Mock
    private Context context;

    @Mock
    private User user;


    private String text = "This is a sentence that is going to tell you about a guy named "
            + "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        Configuration config = new Configuration();
        config.set("nlpConfPathPrefix", Thread.currentThread().getContextClassLoader().getResource("fs/").toString());
        doReturn(config).when(context).getConfiguration();
        extractor = spy(new OpenNlpDictionaryEntityExtractor());
        List<TokenNameFinder> finders = loadFinders();
        doReturn(finders).when(extractor).loadFinders();
    }

    @Test
    public void testEntityExtraction() throws Exception {
        extractor.setup(context,user);
        List<ExtractedEntity> terms = extractor.extract(createArtifact(text), text);
        assertEquals(3, terms.size());
        ArrayList<String> signs = new ArrayList<String>();
        for (ExtractedEntity term : terms) {
            signs.add(term.getTermMention().getMetadata().getSign());
        }

        assertTrue("Bob Robertson not found", signs.contains("Bob Robertson"));
        assertTrue("Altamira Corporation not found", signs.contains("Altamira Corporation"));
        assertTrue("Boston , MA not found", signs.contains("Boston , MA"));
    }

    @Test
    public void testEntityExtractionSetsMentionRelativeToArtifactNotSentence() throws Exception {
        extractor.setup(context,user);
        Collection<ExtractedEntity> extractedEntities = extractor.extract(createArtifact(text), text);
        boolean found = false;
        for (ExtractedEntity extractedEntity : extractedEntities) {
            TermMention term = extractedEntity.getTermMention();
            if (term.getMetadata().getSign().equals("Bob Robertson")) {
                found = true;
                assertEquals(63, term.getRowKey().getStartOffset());
                assertEquals(76, term.getRowKey().getEndOffset());
                break;
            }
        }
        assertTrue("Expected name not found!", found);
    }

    private List<TokenNameFinder> loadFinders() {
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        Dictionary people = new Dictionary();
        people.put(new StringList("Bob Robertson".split(" ")));
        finders.add(new DictionaryNameFinder(people,"person"));

        Dictionary locations = new Dictionary();
        locations.put(new StringList("Boston , MA".split(" ")));
        finders.add(new DictionaryNameFinder(locations,"location"));

        Dictionary organizations = new Dictionary();
        organizations.put(new StringList("Altamira Corporation".split(" ")));
        finders.add(new DictionaryNameFinder(organizations,"organization"));

        return finders;
    }

}
