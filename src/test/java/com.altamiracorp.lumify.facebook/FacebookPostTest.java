package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Matchers.any;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import org.mockito.stubbing.Answer;
import java.io.InputStream;
import java.util.Iterator;

@RunWith(MockitoJUnitRunner.class)
public class FacebookPostTest {
    @Mock
    Graph graph;
    @Mock
    AuditRepository auditRepository;
    @Mock
    OntologyRepository ontologyRepository;
    @Mock
    SystemUser systemUser;
    @Mock
    private Vertex postVertex;
    @Mock
    private Vertex authorVertex;
    @Mock
    private Vertex taggedVertex;
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
    public void testProcessNormalArtifact () throws Exception {
        facebookPost = new FacebookPost();
        returnedExtractedInfo = facebookPost.processPostArtifact(normalPostObject);
        assertEquals("One day I will live at the White House", returnedExtractedInfo.getTitle());
        assertEquals("12345", returnedExtractedInfo.getAuthor());
        assertEquals("Facebook", returnedExtractedInfo.getSource());
    }

    @Test
    public void testProcessEmptyArtifact () throws Exception {
        facebookPost = new FacebookPost();
        returnedExtractedInfo = facebookPost.processPostArtifact(emptyPostObject);
        assertEquals("Facebook Image Post", returnedExtractedInfo.getTitle());
        assertEquals("12345", returnedExtractedInfo.getAuthor());
        assertEquals("Facebook", returnedExtractedInfo.getSource());
    }

    @Test
    public void testProcessLongArtifact () throws Exception {
        facebookPost = new FacebookPost();
        returnedExtractedInfo = facebookPost.processPostArtifact(longPostObject);
        assertEquals("One day I will live at the White House or maybe just a quite large house that is all white on the outside; as long as it has a nice big y...", returnedExtractedInfo.getTitle());
        assertEquals("12345", returnedExtractedInfo.getAuthor());
        assertEquals("Facebook", returnedExtractedInfo.getSource());
    }

    // TODO rewrite this test for secure graph!!!
//    @Test
//    public void testProcessPostVertex () throws Exception {
//        facebookPost = new FacebookPost();
//        when(graph.findVertexByProperty("profileId", "12345", systemUser)).thenReturn(authorVertex);
//        when(graph.findVertexByProperty("profileId", "67890", systemUser)).thenReturn(taggedVertex);
//
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
//                postVertex   = (Vertex) invocationOnMock.getArguments()[0];
//                authorVertex = (Vertex) invocationOnMock.getArguments()[0];
//                taggedVertex = (Vertex) invocationOnMock.getArguments()[0];
//                return null;
//            }
//        }).when(searchProvider).add(any(Vertex.class), any(InputStream.class));
//
//        when(ontologyRepository.getConceptByName("facebookProfile", systemUser)).thenReturn(facebookConcept);
//        when(ontologyRepository.getConceptByName("emailAddress", systemUser)).thenReturn(emailConcept);
//        when(facebookConcept.getId()).thenReturn("32");
//        when(emailConcept.getId()).thenReturn("32");
//        when(graph.save(postVertex, systemUser)).thenReturn("");
//        when(graph.save(authorVertex, systemUser)).thenReturn("");
//        when(graph.save(taggedVertex, systemUser)).thenReturn("");
//        when(postVertex.getId()).thenReturn("");
//        when(authorVertex.getId()).thenReturn("");
//        when(taggedVertex.getId()).thenReturn("");
//        when(postVertex.getProperty(PropertyName.TITLE)).thenReturn("");
//        when(keys.next()).thenReturn(taggedVertex);
//
//        returnedVertex = facebookPost.processPostVertex(normalPostObject, postVertex, graph, auditRepository, ontologyRepository, systemUser);
//
//        verify(graph, times(1)).save(any(Vertex.class), eq(systemUser));
//        verify(graph, times(2)).saveRelationship(anyString(), anyString(), anyString(), eq(systemUser));
//        verify(graph, times(2)).findVertexByProperty(eq("profileId"), anyString(), eq(systemUser));
//    }
//
//    @Test
//    public void testProcessPostUserVertices () throws Exception {
//        facebookPost = new FacebookPost();
//        when(graph.findVertexByProperty("profileId", "12345", systemUser)).thenReturn(null);
//        when(graph.findVertexByProperty("profileId", "67890", systemUser)).thenReturn(null);
//        when(graph.save(postVertex, systemUser)).thenReturn("");
//        when(graph.save(authorVertex, systemUser)).thenReturn("");
//        when(graph.save(taggedVertex, systemUser)).thenReturn("");
//        when(postVertex.getId()).thenReturn("");
//        when(authorVertex.getId()).thenReturn("");
//        when(taggedVertex.getId()).thenReturn("");
//        when(postVertex.getProperty(PropertyName.TITLE)).thenReturn("");
//        when(keys.next()).thenReturn(taggedVertex);
//        when(ontologyRepository.getConceptByName("facebookProfile", systemUser)).thenReturn(facebookConcept);
//        when(ontologyRepository.getConceptByName("emailAddress", systemUser)).thenReturn(emailConcept);
//        when(facebookConcept.getId()).thenReturn("32");
//        when(emailConcept.getId()).thenReturn("32");
//        returnedVertex = facebookPost.processPostVertex(normalPostObject, postVertex, graph, auditRepository, ontologyRepository, systemUser);
//
//        verify(graph, times(3)).save(any(Vertex.class), eq(systemUser));
//        verify(graph, times(2)).saveRelationship(anyString(), anyString(), anyString(), eq(systemUser));
//        verify(graph, times(2)).findVertexByProperty(eq("profileId"), anyString(), eq(systemUser));
//    }

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
