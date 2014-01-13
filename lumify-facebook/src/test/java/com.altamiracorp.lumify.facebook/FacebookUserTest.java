package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRelationship;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.SystemUser;
import org.json.JSONException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FacebookUserTest {
    @Mock
    private SearchProvider searchProvider;
    @Mock
    GraphRepository graphRepository;
    @Mock
    AuditRepository auditRepository;
    @Mock
    OntologyRepository ontologyRepository;
    @Mock
    SystemUser systemUser;
    @Mock
    private GraphVertex postVertex;
    @Mock
    private GraphVertex profileUser;
    @Mock
    private GraphVertex emailVertex;
    @Mock
    private GraphVertex picVertex;
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
    private GraphRelationship profileAndPicRelationship;

    private FacebookUser facebookUser;
    private JSONObject normalUserObject;
    private JSONObject fullUserObject;
    private ArtifactExtractedInfo returnedExtractedInfo;
    private GraphVertex returnedVertex;

    @Before
    public void setup() {
        setNormalUserObject();
        setFullUserObject();
    }

    @Test
    public void testNormalUserProcess () throws Exception {
        facebookUser = new FacebookUser();
        when(graphRepository.findVertexByExactTitle("12345", systemUser)).thenReturn(profileUser);
        when(graphRepository.findVertexByProperty("email", "facebookTest@lumify.io", systemUser)).thenReturn(emailVertex);
        when(graphRepository.save(profileUser, systemUser)).thenReturn("");
        when(graphRepository.save(emailVertex, systemUser)).thenReturn("");
        when(profileUser.getId()).thenReturn("");
        when(ontologyRepository.getConceptByName("facebookProfile", systemUser)).thenReturn(facebookConcept);
        when(facebookConcept.getId()).thenReturn("32");

        returnedVertex = facebookUser.process(normalUserObject, graphRepository, auditRepository, ontologyRepository, systemUser);

        verify(graphRepository, times(2)).save(any(GraphVertex.class), eq(systemUser));
        verify(profileUser, times(2)).setProperty(any(PropertyName.class), anyString());
        verify(profileUser, times(1)).setProperty(anyString(), anyString());
    }
    @Test
    public void testFullUserProcess () throws Exception {
        facebookUser = new FacebookUser();
        when(graphRepository.findVertexByExactTitle("12345", systemUser)).thenReturn(profileUser);
        when(graphRepository.findVertexByProperty("email", "facebookTest@lumify.io", systemUser)).thenReturn(null);
        when(graphRepository.save(profileUser, systemUser)).thenReturn("");
        when(graphRepository.save(emailVertex, systemUser)).thenReturn("");
        when(profileUser.getId()).thenReturn("");
        when(ontologyRepository.getConceptByName("facebookProfile", systemUser)).thenReturn(facebookConcept);
        when(ontologyRepository.getConceptByName("emailAddress", systemUser)).thenReturn(emailConcept);
        when(facebookConcept.getId()).thenReturn("32");
        when(emailConcept.getId()).thenReturn("48");

        returnedVertex = facebookUser.process(fullUserObject, graphRepository, auditRepository, ontologyRepository, systemUser);

        verify(graphRepository, times(3)).save(any(GraphVertex.class), eq(systemUser));
        verify(graphRepository, times(1)).saveRelationship(anyString(), anyString(), eq("personHasEmailAddress"), eq(systemUser));
        verify(graphRepository, times(1)).findVertexByProperty(eq("emailAddress"), anyString(), eq(systemUser));
        verify(profileUser, times(3)).setProperty(any(PropertyName.class), anyString());
        verify(profileUser, times(2)).setProperty(anyString(), anyString());
    }

    @Test
    public void testAlreadyProcessedUserProcess () throws Exception {
        facebookUser = new FacebookUser();
        when(graphRepository.findVertexByExactTitle("12345", systemUser)).thenReturn(null);
        when(graphRepository.findVertexByExactTitle("Facebook Test", systemUser)).thenReturn(profileUser);
        when(graphRepository.findVertexByProperty("email", "facebookTest@lumify.io", systemUser)).thenReturn(null);
        when(graphRepository.save(profileUser, systemUser)).thenReturn("");
        when(graphRepository.save(emailVertex, systemUser)).thenReturn("");
        when(profileUser.getId()).thenReturn("");
        when(ontologyRepository.getConceptByName("facebookProfile", systemUser)).thenReturn(facebookConcept);
        when(ontologyRepository.getConceptByName("emailAddress", systemUser)).thenReturn(emailConcept);
        when(facebookConcept.getId()).thenReturn("32");
        when(emailConcept.getId()).thenReturn("48");

        returnedVertex = facebookUser.process(fullUserObject, graphRepository, auditRepository, ontologyRepository, systemUser);

        verify(graphRepository, times(0)).save(any(GraphVertex.class), eq(systemUser));
    }

    @Test (expected = RuntimeException.class)
    public void testNoPostUserProcess () throws Exception {
        facebookUser = new FacebookUser();
        when(graphRepository.findVertexByExactTitle("12345", systemUser)).thenReturn(null);
        when(graphRepository.findVertexByExactTitle("Facebook Test", systemUser)).thenReturn(null);
        returnedVertex = facebookUser.process(fullUserObject, graphRepository, auditRepository, ontologyRepository, systemUser);
    }

    @Test
    public void testSmallPictureArtifact () throws IOException {
        facebookUser = new FacebookUser();
        returnedExtractedInfo = facebookUser.createProfilePhotoArtifact(normalUserObject, profileUser);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        URL imageUrl = getClass().getResource("small-picture-test.jpg");
        InputStream is = imageUrl.openStream();
        IOUtils.copy(is, os);
        byte[] raw = os.toByteArray();
        ArtifactRowKey build = ArtifactRowKey.build(raw);

        assertEquals("Facebook profile picture", returnedExtractedInfo.getSource());
        assertEquals(build.toString(), returnedExtractedInfo.getRowKey());
        assertNull(returnedExtractedInfo.getRawHdfsPath());
    }

    @Test
    public void testHdfsPictureArtifact () throws IOException {
        facebookUser = new FacebookUser();
        facebookUser.setFacebookBolt(facebookBolt);
        facebookBolt.setHdfsFileSystem(fileSystem);
        when(facebookBolt.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.create(any(Path.class))).thenReturn(rawFile);
        returnedExtractedInfo = facebookUser.createProfilePhotoArtifact(fullUserObject, profileUser);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        URL imageUrl = getClass().getResource("big-picture-test.png");
        InputStream is = imageUrl.openStream();
        IOUtils.copy(is, os);
        byte[] raw = os.toByteArray();
        ArtifactRowKey build = ArtifactRowKey.build(raw);

        assertEquals("Facebook profile picture", returnedExtractedInfo.getSource());
        assertEquals(build.toString(), returnedExtractedInfo.getRowKey());
        assertNull(returnedExtractedInfo.getRaw());
        verify(fileSystem).create(any(Path.class));
        verify(rawFile).write(any(byte[].class));
    }

    @Test (expected = JSONException.class)
    public void testBadJsonPictureArtifact () {
        facebookUser = new FacebookUser();
        URL imageUrl = getClass().getResource("big-picture-test.jpg");
        normalUserObject.put("pic", imageUrl);
        returnedExtractedInfo = facebookUser.createProfilePhotoArtifact(normalUserObject, profileUser);
    }

    @Test
    public void testCreateProfilePictureVertex () throws Exception {
        facebookUser = new FacebookUser();
        when(graphRepository.findVertexByProperty("profileId", "12345", systemUser)).thenReturn(profileUser);
        when(graphRepository.findVertexByProperty("email", "facebookTest@lumify.io", systemUser)).thenReturn(null);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                profileUser = (GraphVertex) invocationOnMock.getArguments()[0];
                picVertex   = (GraphVertex) invocationOnMock.getArguments()[0];
                postVertex  = (GraphVertex) invocationOnMock.getArguments()[0];
                return null;
            }
        }).when(searchProvider).add(any(GraphVertex.class), any(InputStream.class));

        List<GraphVertex> list = new ArrayList<GraphVertex>(0);
        list.add(postVertex);
        when(graphRepository.getRelatedVertices(profileUser.getId(), systemUser)).thenReturn(list);
        when(graphRepository.save(profileUser, systemUser)).thenReturn("");
        when(graphRepository.findOrAddRelationship(profileUser.getId(), picVertex.getId(), "hasImage", systemUser)).thenReturn(profileAndPicRelationship);
        when(picVertex.getId()).thenReturn("1234567890");
        when(postVertex.getProperty(PropertyName.TITLE)).thenReturn("Facebook Test Post");

        facebookUser.createProfilePhotoVertex(picVertex, profileUser, graphRepository, auditRepository, systemUser);

        verify(graphRepository).save(profileUser, systemUser);
        verify(profileUser).setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/1234567890/raw");
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
        fullUserObject.put("email", "facebookTest@lumify.io");
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
