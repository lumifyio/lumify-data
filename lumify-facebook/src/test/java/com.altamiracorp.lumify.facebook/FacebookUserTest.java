package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.id.UUIDIdGenerator;
import com.altamiracorp.securegraph.inmemory.InMemoryAuthorizations;
import com.altamiracorp.securegraph.inmemory.InMemoryGraph;
import com.altamiracorp.securegraph.inmemory.InMemoryGraphConfiguration;
import com.altamiracorp.securegraph.search.DefaultSearchIndex;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FacebookUserTest {
    @Mock
    AuditRepository auditRepository;
    @Mock
    OntologyRepository ontologyRepository;
    @Mock
    User systemUser;
    @Mock
    private Vertex postVertex;
    @Mock
    private Vertex profileUser;
    @Mock
    private Vertex emailVertex;
    @Mock
    private VertexBuilder emailBuilder;
    @Mock
    private Vertex picVertex;
    @Mock
    private Concept facebookConcept;
    @Mock
    private Concept emailConcept;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private FacebookBolt facebookBolt;
    @Mock
    private FSDataOutputStream rawFile;
    @Mock
    private Iterator<Vertex> vertexIterator;
    @Mock
    private Iterator<Edge> edgeIterator;
    @Mock
    private Iterable<Edge> edgeIterable;
    @Mock
    private Graph mockGraph;

    private Graph graph;
    private InMemoryAuthorizations authorizations;
    private FacebookUser facebookUser;
    private JSONObject normalUserObject;
    private JSONObject fullUserObject;
    private ArtifactExtractedInfo returnedExtractedInfo;
    private Vertex returnedVertex;

    @Before
    public void setup() {
        InMemoryGraphConfiguration config = new InMemoryGraphConfiguration(new HashMap());
        graph = new InMemoryGraph(config, new UUIDIdGenerator(config.getConfig()), new DefaultSearchIndex(config.getConfig()));
        authorizations = new InMemoryAuthorizations("");

        graph.addVertex("FB-USER-12345", new Visibility(""), authorizations);
        graph.prepareVertex(new Visibility(""), authorizations)
                .setProperty("email", "facebookTestFull@lumify.io", new Visibility(""))
                .setProperty("_conceptType", "48", new Visibility(""))
                .save();
        graph.addVertex("FB-USER-67890", new Visibility(""), authorizations);
        graph.flush();

        setNormalUserObject();
        setFullUserObject();
    }

    @Test
    public void testNormalUserProcess() throws Exception {
        facebookUser = new FacebookUser();
        when(systemUser.getAuthorizations()).thenReturn(authorizations);
        when(ontologyRepository.getConceptByName("facebookProfile")).thenReturn(facebookConcept);
        when(ontologyRepository.getConceptByName("emailAddress")).thenReturn(emailConcept);
        when(facebookConcept.getId()).thenReturn("32");
        when(emailConcept.getId()).thenReturn("48");

        returnedVertex = facebookUser.process(normalUserObject, graph, auditRepository, ontologyRepository, systemUser);

        assertEquals("facebookTest", returnedVertex.getPropertyValue("displayName"));
    }

    @Test(expected = RuntimeException.class)
    public void testNullUserProcess() throws Exception {
        facebookUser = new FacebookUser();
        returnedVertex = facebookUser.process(new JSONObject(), graph, auditRepository, ontologyRepository, systemUser);
    }

    @Test
    public void testFullUserProcess() throws Exception {
        facebookUser = new FacebookUser();
        when(systemUser.getAuthorizations()).thenReturn(authorizations);
        when(ontologyRepository.getConceptByName("facebookProfile")).thenReturn(facebookConcept);
        when(ontologyRepository.getConceptByName("emailAddress")).thenReturn(emailConcept);
        when(facebookConcept.getId()).thenReturn("32");
        when(emailConcept.getId()).thenReturn("48");

        returnedVertex = facebookUser.process(fullUserObject, graph, auditRepository, ontologyRepository, systemUser);

        assertEquals("facebookTest", returnedVertex.getPropertyValue("displayName"));
    }

    @Test
    public void testSmallPictureArtifact() throws IOException {
        facebookUser = new FacebookUser();
        returnedExtractedInfo = facebookUser.createProfilePhotoArtifact(normalUserObject, profileUser);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        URL imageUrl = getClass().getResource("small-picture-test.jpg");
        InputStream is = imageUrl.openStream();
        IOUtils.copy(is, os);
        byte[] raw = os.toByteArray();
        String build = RowKeyHelper.buildSHA256KeyString(raw);

        assertEquals("Facebook profile picture", returnedExtractedInfo.getSource());
        assertEquals(build, returnedExtractedInfo.getRowKey());
        assertNull(returnedExtractedInfo.getRawHdfsPath());
    }

    @Test
    public void testHdfsPictureArtifact() throws IOException {
        facebookUser = new FacebookUser();
        facebookBolt.setHdfsFileSystem(fileSystem);
        when(facebookBolt.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.create(any(Path.class))).thenReturn(rawFile);
        returnedExtractedInfo = facebookUser.createProfilePhotoArtifact(fullUserObject, profileUser);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        URL imageUrl = getClass().getResource("big-picture-test.png");
        InputStream is = imageUrl.openStream();
        IOUtils.copy(is, os);
        byte[] raw = os.toByteArray();
        String build = RowKeyHelper.buildSHA256KeyString(raw);

        assertEquals("Facebook profile picture", returnedExtractedInfo.getSource());
        assertEquals(build, returnedExtractedInfo.getRowKey());
//        verify(fileSystem).create(any(Path.class));
//        verify(rawFile).write(any(byte[].class));
    }

    @Test(expected = JSONException.class)
    public void testBadJsonPictureArtifact() {
        facebookUser = new FacebookUser();
        URL imageUrl = getClass().getResource("big-picture-test.jpg");
        normalUserObject.put("pic", imageUrl);
        returnedExtractedInfo = facebookUser.createProfilePhotoArtifact(normalUserObject, profileUser);
    }

    @Test
    public void testCreateProfilePictureVertex() throws Exception {
        //TODO fix test for secure graph
//        facebookUser = new FacebookUser();
//        when(systemUser.getAuthorizations()).thenReturn(authorizations);
//        when(profileUser.getEdges(picVertex, Direction.IN, "entityHasImageFacebookProfileImage", authorizations)).thenReturn(edgeIterable);
//        when(edgeIterable.iterator()).thenReturn(edgeIterator);
//        List<Vertex> list = new ArrayList<Vertex>(0);
//        list.add(postVertex);
//        when(picVertex.getId()).thenReturn("1234567890");
//        facebookUser.createProfilePhotoVertex(picVertex, profileUser, mockGraph, auditRepository, ontologyRepository, systemUser);
    }

    @Test
    public void testCreateNewProfilePictureVertex() throws Exception {
        //TODO fix test for secure graph
//        facebookUser = new FacebookUser();
//        when(systemUser.getAuthorizations()).thenReturn(authorizations);
//        when(profileUser.getEdges(picVertex, Direction.IN, "entityHasImageFacebookProfileImage", authorizations)).thenReturn(edgeIterable);
//        when(edgeIterable.iterator()).thenReturn(edgeIterator);
//        when(edgeIterator.hasNext()).thenReturn(false);
//        List<Vertex> list = new ArrayList<Vertex>(0);
//        list.add(postVertex);
//        when(picVertex.getId()).thenReturn("1234567890");
//        facebookUser.createProfilePhotoVertex(picVertex, profileUser, mockGraph, auditRepository, ontologyRepository, systemUser);
//        verify(mockGraph).addEdge(eq(profileUser), eq(picVertex), anyString(), any(Visibility.class), eq(authorizations));
    }

    private void setNormalUserObject() {
        normalUserObject = new JSONObject();
        normalUserObject.put("username", "facebookTest");
        normalUserObject.put("name", "Facebook Test");
        normalUserObject.put("birthday_date", "12/30");
        normalUserObject.put("uid", "12345");
        URL imageUrl = getClass().getResource("small-picture-test.jpg");
        normalUserObject.put("pic", imageUrl);
    }

    private void setFullUserObject() {
        fullUserObject = new JSONObject();
        fullUserObject.put("uid", "12345");
        fullUserObject.put("name", "Facebook Test");
        fullUserObject.put("username", "facebookTest");
        fullUserObject.put("email", "facebookTestFull@lumify.io");
        fullUserObject.put("sex", "male");
        fullUserObject.put("birthday_date", "12/30/1987");
        URL imageUrl = getClass().getResource("big-picture-test.png");
        fullUserObject.put("pic", imageUrl);
        JSONObject coords = new JSONObject();
        coords.put("latitude", 38.9876);
        coords.put("longitude", -77.1234);
        fullUserObject.put("coords", coords);
    }
}
