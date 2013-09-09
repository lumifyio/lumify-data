package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.routes.RouteTestBase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ArtifactByRowKeyTest extends RouteTestBase {
    private ArtifactByRowKey artifactByRowKey;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        artifactByRowKey = new ArtifactByRowKey();
        artifactByRowKey.setApp(mockApp);
        artifactByRowKey.setArtifactRepository(mockArtifactRepository);
    }

    @Test
    public void testHandle() throws Exception {
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build("testContents".getBytes());
        when(mockRequest.getAttribute("_rowKey")).thenReturn(artifactRowKey.toString());

        Artifact artifact = new Artifact(artifactRowKey);
        artifact.getGenericMetadata()
                .setMimeType("text/html");
        when(mockArtifactRepository.findByRowKey(mockModelSession, artifactRowKey.toString())).thenReturn(artifact);

        artifactByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);

        JSONObject responseJson = new JSONObject(responseStringWriter.getBuffer().toString());
        assertEquals(ArtifactRawByRowKey.getUrl(artifactRowKey), responseJson.getString("rawUrl"));
        assertEquals(artifactRowKey.toString(), responseJson.getJSONObject("key").getString("value"));
        assertEquals("document", responseJson.getString("type"));
    }
}