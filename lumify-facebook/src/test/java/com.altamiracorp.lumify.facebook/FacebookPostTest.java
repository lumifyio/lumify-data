package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.VertexBuilder;
import com.altamiracorp.securegraph.Visibility;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class FacebookPostTest {
    @Mock
    Graph graph;
    @Mock
    AuditRepository auditRepository;
    @Mock
    OntologyRepository ontologyRepository;
    @Mock
    User systemUser;
    @Mock
    private Vertex postVertex;
    @Mock
    private Vertex authorVertex;
    @Mock
    private Vertex taggedVertex;
    @Mock
    private VertexBuilder postBuilder;
    @Mock
    private VertexBuilder authorBuilder;
    @Mock
    private VertexBuilder taggedBuilder;
    @Mock
    private Visibility visibility;
    @Mock
    private Iterator keys;
    @Mock
    private Concept facebookConcept;
    @Mock
    private Concept emailConcept;


    private FacebookPost facebookPost;
    private JSONObject normalPostObject;
    private JSONObject emptyPostObject;
    private JSONObject longPostObject;
    private ArtifactExtractedInfo returnedExtractedInfo;
    private Vertex returnedVertex;

    @Before
    public void setup() {
        setNormalPostObject();
        setEmptyPostObject();
        setLongPostObject();
    }

    @Test
    public void testProcessNormalArtifact() throws Exception {
        facebookPost = new FacebookPost();
        returnedExtractedInfo = facebookPost.processPostArtifact(normalPostObject);
        assertEquals("One day I will live at the White House", returnedExtractedInfo.getTitle());
        assertEquals("12345", returnedExtractedInfo.getAuthor());
        assertEquals("Facebook", returnedExtractedInfo.getSource());
    }

    @Test
    public void testProcessEmptyArtifact() throws Exception {
        facebookPost = new FacebookPost();
        returnedExtractedInfo = facebookPost.processPostArtifact(emptyPostObject);
        assertEquals("Facebook Image Post", returnedExtractedInfo.getTitle());
        assertEquals("12345", returnedExtractedInfo.getAuthor());
        assertEquals("Facebook", returnedExtractedInfo.getSource());
    }

    @Test
    public void testProcessLongArtifact() throws Exception {
        facebookPost = new FacebookPost();
        returnedExtractedInfo = facebookPost.processPostArtifact(longPostObject);
        assertEquals("One day I will live at the White House or maybe just a quite large house that is all white on the outside; as long as it has a nice big y...", returnedExtractedInfo.getTitle());
        assertEquals("12345", returnedExtractedInfo.getAuthor());
        assertEquals("Facebook", returnedExtractedInfo.getSource());
    }

    @Test
    public void testProcessPostVertex() throws Exception {
        //TODO fix test for secure graph
//        facebookPost = new FacebookPost();
//
//        when(graph.prepareVertex(eq("FB-USER-12345"), any(Visibility.class), any(Authorizations.class))).thenReturn(authorBuilder);
//        when(graph.prepareVertex(eq("FB-USER-67890"), any(Visibility.class), any(Authorizations.class))).thenReturn(taggedBuilder);
//        when(ontologyRepository.getConceptByName("facebookProfile")).thenReturn(facebookConcept);
//        when(ontologyRepository.getConceptByName("emailAddress")).thenReturn(emailConcept);
//        when(facebookConcept.getId()).thenReturn("32");
//        when(emailConcept.getId()).thenReturn("32");
//        when(authorBuilder.save()).thenReturn(authorVertex);
//        when(taggedBuilder.save()).thenReturn(taggedVertex);
//        when(postVertex.getId()).thenReturn("");
//        when(authorVertex.getId()).thenReturn("");
//        when(taggedVertex.getId()).thenReturn("");
//        when(keys.next()).thenReturn(taggedVertex);
//        returnedVertex = facebookPost.processPostVertex(normalPostObject, postVertex, graph, auditRepository, ontologyRepository, systemUser);
//        verify(graph, times(2)).flush();
//        verify(graph, times(2)).addEdge(any(Vertex.class), any(Vertex.class), anyString(), any(Visibility.class), any(Authorizations.class));
    }

    private void setNormalPostObject() {
        normalPostObject = new JSONObject();
        normalPostObject.put("message", "One day I will live at the White House");
        normalPostObject.put("author_uid", 12345);
        normalPostObject.put("timestamp", 1388172642);
        JSONObject tagged = new JSONObject();
        tagged.put("67890", 67890);
        normalPostObject.put("tagged_uids", tagged);
        JSONObject coords = new JSONObject();
        coords.put("latitude", 38.9876);
        coords.put("longitude", -77.1234);
        normalPostObject.put("coords", coords);
    }

    private void setEmptyPostObject() {
        emptyPostObject = new JSONObject();
        emptyPostObject.put("message", "");
        emptyPostObject.put("author_uid", 12345);
        emptyPostObject.put("timestamp", 1388172642);
    }

    private void setLongPostObject() {
        longPostObject = new JSONObject();
        longPostObject.put("message", "One day I will live at the White House or maybe just a quite large house that is all white on the outside; as long as it has a nice big yard and pool.");
        longPostObject.put("author_uid", 12345);
        longPostObject.put("timestamp", 1388172642);
    }
}
