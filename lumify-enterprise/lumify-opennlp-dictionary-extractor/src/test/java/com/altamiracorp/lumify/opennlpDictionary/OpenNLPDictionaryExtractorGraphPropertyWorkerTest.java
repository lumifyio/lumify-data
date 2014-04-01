package com.altamiracorp.lumify.opennlpDictionary;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Authorizations;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.inmemory.InMemoryAuthorizations;
import com.google.inject.Injector;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class OpenNLPDictionaryExtractorGraphPropertyWorkerTest {
    private static final String RESOURCE_CONFIG_DIR = "/fs/conf/opennlp";

    private OpenNLPDictionaryExtractorGraphPropertyWorker extractor;

    @Mock
    private User user;

    private Configuration configuration;

    private String text = "This is a sentence that is going to tell you about a guy named "
            + "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

    private InMemoryAuthorizations authorizations;

    List<TermMention> termMentions;

    @Before
    public void setUp() throws Exception {
        final List<TokenNameFinder> finders = loadFinders();
        configuration = new Configuration();
        configuration.set(OpenNLPDictionaryExtractorGraphPropertyWorker.PATH_PREFIX_CONFIG, "file:///" + getClass().getResource(RESOURCE_CONFIG_DIR).getFile());
        configuration.set(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL, "");

        termMentions = new ArrayList<TermMention>();
        extractor = new OpenNLPDictionaryExtractorGraphPropertyWorker() {
            @Override
            protected List<TokenNameFinder> loadFinders() throws IOException {
                return finders;
            }

            @Override
            protected TermMentionModel saveTermMention(Vertex vertex, TermMention termMention, User user, Visibility visibility, Authorizations authorizations) {
                termMentions.add(termMention);
                return new TermMentionModel(new TermMentionRowKey(vertex.getId().toString()));
            }
        };
        Map stormConf = new HashMap();
        FileSystem hdfsFileSystem = FileSystem.get(new Configuration());
        authorizations = new InMemoryAuthorizations();
        Injector injector = null;
        GraphPropertyWorkerPrepareData workerPrepareData = new GraphPropertyWorkerPrepareData(stormConf, hdfsFileSystem, user, authorizations, injector);
        extractor.prepare(workerPrepareData);
    }

    @Test
    public void testEntityExtraction() throws Exception {
        GraphPropertyWorkData workData = null;
        extractor.execute(new ByteArrayInputStream(text.getBytes()), workData);
        assertEquals(3, termMentions.size());
        ArrayList<String> signs = new ArrayList<String>();
        for (TermMention term : termMentions) {
            signs.add(term.getSign());
        }

        assertTrue("Bob Robertson not found", signs.contains("Bob Robertson"));
        assertTrue("Altamira Corporation not found", signs.contains("Altamira Corporation"));
        assertTrue("Boston , MA not found", signs.contains("Boston , MA"));
    }

    @Test
    public void testEntityExtractionSetsMentionRelativeToArtifactNotSentence() throws Exception {
        GraphPropertyWorkData workData = null;
        extractor.execute(new ByteArrayInputStream(text.getBytes()), workData);
        boolean found = false;
        for (TermMention term : termMentions) {
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
