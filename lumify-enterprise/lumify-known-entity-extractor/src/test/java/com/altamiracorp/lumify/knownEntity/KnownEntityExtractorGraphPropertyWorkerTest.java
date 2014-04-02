package com.altamiracorp.lumify.knownEntity;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.graphProperty.TermMentionFilter;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.VertexBuilder;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.inmemory.InMemoryAuthorizations;
import com.altamiracorp.securegraph.inmemory.InMemoryGraph;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.altamiracorp.securegraph.util.IterableUtils.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class KnownEntityExtractorGraphPropertyWorkerTest {
    private KnownEntityExtractorGraphPropertyWorker extractor;

    @Mock
    private User user;
    String dictionaryPath;
    List<TermMention> termMentions;
    private InMemoryAuthorizations authorizations;
    private InMemoryGraph graph;
    private Visibility visibility;

    @Before
    public void setup() throws Exception {
        dictionaryPath = getClass().getResource(".").getPath();
        extractor = new KnownEntityExtractorGraphPropertyWorker() {
            @Override
            protected List<TermMentionWithGraphVertex> saveTermMentions(Vertex artifactGraphVertex, Iterable<TermMention> termMentions) {
                KnownEntityExtractorGraphPropertyWorkerTest.this.termMentions = toList(termMentions);
                return null;
            }
        };
        Map<String, String> stormConf = new HashMap<String, String>();
        stormConf.put(KnownEntityExtractorGraphPropertyWorker.PATH_PREFIX_CONFIG, "file://" + dictionaryPath);
        FileSystem hdfsFileSystem = FileSystem.get(new Configuration());
        authorizations = new InMemoryAuthorizations();
        Injector injector = null;
        List<TermMentionFilter> termMentionFilters = new ArrayList<TermMentionFilter>();
        GraphPropertyWorkerPrepareData workerPrepareData = new GraphPropertyWorkerPrepareData(stormConf, termMentionFilters, hdfsFileSystem, user, authorizations, injector);
        graph = new InMemoryGraph();
        visibility = new Visibility("");
        extractor.prepare(workerPrepareData);
        extractor.setGraph(graph);
    }

    @Test
    public void textExtract() throws Exception {
        InputStream in = getClass().getResourceAsStream("bffls.txt");
        VertexBuilder vertexBuilder = graph.prepareVertex("v1", visibility, authorizations);
        StreamingPropertyValue textPropertyValue = new StreamingPropertyValue(in, String.class);
        RawLumifyProperties.TEXT.setProperty(vertexBuilder, textPropertyValue, visibility);
        Vertex vertex = vertexBuilder.save();

        in = getClass().getResourceAsStream("bffls.txt");
        Property property = vertex.getProperty(RawLumifyProperties.TEXT.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        extractor.execute(in, workData);
        assertEquals(3, termMentions.size());
        for (TermMention termMention : termMentions) {
            assertTrue(termMention.isResolved());
            assertEquals("person", termMention.getOntologyClassUri());
            assertEquals("Joe Ferner", termMention.getSign());
        }
    }
}
