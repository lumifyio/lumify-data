package com.altamiracorp.lumify.storm.term.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.StringList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;

@RunWith(MockitoJUnitRunner.class)
public class OpenNlpDictionaryEntityExtractorTest {
    private static final String RESOURCE_CONFIG_DIR = "/fs/conf/opennlp";

    private OpenNlpDictionaryEntityExtractor extractor;

    @Mock
    private User user;

    private Configuration configuration;

    private String text = "This is a sentence that is going to tell you about a guy named "
            + "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

    @Before
    public void setUp() throws IOException, URISyntaxException, InterruptedException {
        final List<TokenNameFinder> finders = loadFinders();
        configuration = new Configuration();
        configuration.set(OpenNlpEntityExtractor.PATH_PREFIX_CONFIG, "file:///" + getClass().getResource(RESOURCE_CONFIG_DIR).getFile());
        configuration.set(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL, "");

        extractor = new OpenNlpDictionaryEntityExtractor() {
            @Override
            protected List<TokenNameFinder> loadFinders(String pathPrefix, FileSystem fs) throws IOException {
                return finders;
            }
        };
        extractor.prepare(configuration, user);
    }

    @Test
    public void testEntityExtraction() throws Exception {
        TermExtractionResult results = extractor.extract(new ByteArrayInputStream(text.getBytes()));
        assertEquals(3, results.getTermMentions().size());
        ArrayList<String> signs = new ArrayList<String>();
        for (TermMention term : results.getTermMentions()) {
            signs.add(term.getSign());
        }

        assertTrue("Bob Robertson not found", signs.contains("Bob Robertson"));
        assertTrue("Altamira Corporation not found", signs.contains("Altamira Corporation"));
        assertTrue("Boston , MA not found", signs.contains("Boston , MA"));
    }

    @Test
    public void testEntityExtractionSetsMentionRelativeToArtifactNotSentence() throws Exception {
        TermExtractionResult results = extractor.extract(new ByteArrayInputStream(text.getBytes()));
        boolean found = false;
        for (TermMention term : results.getTermMentions()) {
            if (term.getSign().equals("Bob Robertson")) {
                found = true;
                assertEquals(63, term.getStart());
                assertEquals(76, term.getEnd());
                break;
            }
        }
        assertTrue("Expected name not found!", found);
    }

    private List<TokenNameFinder> loadFinders() {
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        Dictionary people = new Dictionary();
        people.put(new StringList("Bob Robertson".split(" ")));
        finders.add(new DictionaryNameFinder(people, "person"));

        Dictionary locations = new Dictionary();
        locations.put(new StringList("Boston , MA".split(" ")));
        finders.add(new DictionaryNameFinder(locations, "location"));

        Dictionary organizations = new Dictionary();
        organizations.put(new StringList("Altamira Corporation".split(" ")));
        finders.add(new DictionaryNameFinder(organizations, "organization"));

        return finders;
    }

}
