package com.altamiracorp.lumify.knownEntity;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.*;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class KnownEntityExtractorGraphPropertyWorkerTest {
    private KnownEntityExtractorGraphPropertyWorker extractor;

    @Mock
    private User user;
    private Configuration config;
    String dictionaryPath;
    List<TermMention> termMentions;
    private InMemoryAuthorizations authorizations;
    private InMemoryGraph graph;
    private Visibility visibility;

    @Before
    public void setup() throws Exception {
        termMentions = new ArrayList<TermMention>();
        dictionaryPath = getClass().getResource(".").getPath();
        extractor = new KnownEntityExtractorGraphPropertyWorker() {
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
        graph = new InMemoryGraph();
        visibility = new Visibility("");
        extractor.prepare(workerPrepareData);
        config = new Configuration();
        config.set("termextraction.knownEntities.pathPrefix", "file://" + dictionaryPath);
    }

    @Test
    public void textExtract() throws Exception {
        InputStream in = getClass().getResourceAsStream("bffls.txt");
        VertexBuilder vertexBuilder = graph.prepareVertex("v1", visibility, authorizations);
        StreamingPropertyValue textPropertyValue = new StreamingPropertyValue(in, String.class);
        RawLumifyProperties.TEXT.setProperty(vertexBuilder, textPropertyValue, visibility);
        Vertex vertex = vertexBuilder.save();

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
