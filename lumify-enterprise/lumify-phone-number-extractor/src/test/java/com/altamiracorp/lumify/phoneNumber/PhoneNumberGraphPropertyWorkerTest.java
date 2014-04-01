package com.altamiracorp.lumify.phoneNumber;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.VertexBuilder;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.inmemory.InMemoryAuthorizations;
import com.altamiracorp.securegraph.inmemory.InMemoryGraph;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.common.base.Charsets;
import com.google.inject.Injector;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PhoneNumberGraphPropertyWorkerTest {
    private static final String PHONE_TEXT = "This terrorist's phone number is 410-678-2230, and his best buddy's phone number is +44 (0)207 437 0478";
    private static final String PHONE_NEW_LINES = "This terrorist's phone\n number is 410-678-2230, and his best buddy's phone number\n is +44 (0)207 437 0478";
    private static final String PHONE_MISSING = "This is a sentence without any phone numbers in it.";

    @Mock
    private User user;

    private PhoneNumberGraphPropertyWorker extractor;
    private InMemoryAuthorizations authorizations;
    private InMemoryGraph graph;
    private Visibility visibility;
    private ArrayList<TermMention> termMentions;


    @Before
    public void setUp() throws Exception {
        termMentions = new ArrayList<TermMention>();
        extractor = new PhoneNumberGraphPropertyWorker() {
            @Override
            protected TermMentionModel saveTermMention(Vertex vertex, TermMention termMention, Visibility visibility) {
                termMentions.add(termMention);
                return new TermMentionModel(new TermMentionRowKey(vertex.getId().toString()));
            }
        };

        Map stormConf = new HashMap();
        FileSystem hdfsFileSystem = null;
        authorizations = new InMemoryAuthorizations();
        Injector injector = null;
        GraphPropertyWorkerPrepareData workerPrepareData = new GraphPropertyWorkerPrepareData(stormConf, hdfsFileSystem, user, authorizations, injector);
        graph = new InMemoryGraph();
        visibility = new Visibility("");
        extractor.prepare(workerPrepareData);
    }

    @Test
    public void testPhoneNumberExtraction() throws Exception {
        InputStream in = asStream(PHONE_TEXT);
        VertexBuilder vertexBuilder = graph.prepareVertex("v1", visibility, authorizations);
        StreamingPropertyValue textPropertyValue = new StreamingPropertyValue(in, String.class);
        RawLumifyProperties.TEXT.setProperty(vertexBuilder, textPropertyValue, visibility);
        Vertex vertex = vertexBuilder.save();

        Property property = vertex.getProperty(RawLumifyProperties.TEXT.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        extractor.execute(in, workData);

        assertEquals("Incorrect number of phone numbers extracted", 2, termMentions.size());
        TermMention firstTerm = termMentions.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230", firstTerm.getSign());
        assertEquals(33, firstTerm.getStart());
        assertEquals(45, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478", secondTerm.getSign());
        assertEquals(84, secondTerm.getStart());
        assertEquals(103, secondTerm.getEnd());
    }

    @Test
    public void testPhoneNumberExtractionWithNewlines() throws Exception {
        InputStream in = asStream(PHONE_NEW_LINES);
        VertexBuilder vertexBuilder = graph.prepareVertex("v1", visibility, authorizations);
        StreamingPropertyValue textPropertyValue = new StreamingPropertyValue(in, String.class);
        RawLumifyProperties.TEXT.setProperty(vertexBuilder, textPropertyValue, visibility);
        Vertex vertex = vertexBuilder.save();

        Property property = vertex.getProperty(RawLumifyProperties.TEXT.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        extractor.execute(in, workData);

        assertEquals("Incorrect number of phone numbers extracted", 2, termMentions.size());
        TermMention firstTerm = termMentions.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230", firstTerm.getSign());
        assertEquals(34, firstTerm.getStart());
        assertEquals(46, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478", secondTerm.getSign());
        assertEquals(86, secondTerm.getStart());
        assertEquals(105, secondTerm.getEnd());
    }

    @Test
    public void testNegativePhoneNumberExtraction() throws Exception {
        InputStream in = asStream(PHONE_MISSING);
        VertexBuilder vertexBuilder = graph.prepareVertex("v1", visibility, authorizations);
        StreamingPropertyValue textPropertyValue = new StreamingPropertyValue(in, String.class);
        RawLumifyProperties.TEXT.setProperty(vertexBuilder, textPropertyValue, visibility);
        Vertex vertex = vertexBuilder.save();

        Property property = vertex.getProperty(RawLumifyProperties.TEXT.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        extractor.execute(in, workData);

        assertTrue("Phone number extracted when there were no phone numbers", termMentions.isEmpty());
    }

    private InputStream asStream(final String text) {
        return new ByteArrayInputStream(text.getBytes(Charsets.UTF_8));
    }
}
