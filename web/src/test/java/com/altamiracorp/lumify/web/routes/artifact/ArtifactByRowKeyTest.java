package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.altamiracorp.lumify.web.routes.RouteTestBase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactByRowKeyTest extends RouteTestBase {
    private ArtifactByRowKey artifactByRowKey;

    @Mock
    private User user;

    @Mock
    private HttpSession mockSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        artifactByRowKey = new ArtifactByRowKey(mockArtifactRepository);
    }

    @Test
    public void testHandle() throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        ArtifactRowKey artifactRowKey = ArtifactRowKey.build("testContents".getBytes());
//        when(mockRequest.getAttribute("_rowKey")).thenReturn(artifactRowKey.toString());
//        when(mockRequest.getSession()).thenReturn(mockSession);
//        when(mockSession.getAttribute(AuthenticationProvider.CURRENT_USER_REQ_ATTR_NAME)).thenReturn(user);
//
//        Artifact artifact = new Artifact(artifactRowKey);
//        artifact.getGenericMetadata()
//                .setMimeType("text/html");
//        when(mockArtifactRepository.findByRowKey(artifactRowKey.toString(), user)).thenReturn(artifact);
//
//        artifactByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);
//
//        JSONObject responseJson = new JSONObject(responseStringWriter.getBuffer().toString());
//        assertEquals(ArtifactRawByRowKey.getUrl(artifactRowKey), responseJson.getString("rawUrl"));
//        assertEquals(artifactRowKey.toString(), responseJson.getJSONObject("key").getString("value"));
//        assertEquals("document", responseJson.getString("type"));
    }
}