package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.web.routes.RouteTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class ArtifactPosterFrameByRowKeyTest extends RouteTestBase {
    private ArtifactPosterFrameByRowKey artifactPosterFrameByRowKey;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        artifactPosterFrameByRowKey = new ArtifactPosterFrameByRowKey();
        artifactPosterFrameByRowKey.setApp(mockApp);
        artifactPosterFrameByRowKey.setArtifactRepository(mockArtifactRepository);
    }

    @Test
    public void testHandle() throws Exception {
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build("testContents".getBytes());
        when(mockRequest.getAttribute("_rowKey")).thenReturn(artifactRowKey.toString());

        Artifact artifact = new Artifact(artifactRowKey);
        when(mockArtifactRepository.findByRowKey(mockModelSession, artifactRowKey.toString())).thenReturn(artifact);

        InputStream testInputStream = new ByteArrayInputStream("test data".getBytes());
        when(mockArtifactRepository.getRawPosterFrame(mockModelSession, artifact)).thenReturn(testInputStream);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                byte[] data = (byte[]) invocation.getArguments()[0];
                int start = (Integer) invocation.getArguments()[1];
                int len = (Integer) invocation.getArguments()[2];

                assertEquals(0, start);
                assertEquals(9, len);
                assertEquals("test data", new String(data, start, len));
                return null;
            }
        }).when(mockResponseOutputStream).write(any(byte[].class), any(Integer.class), any(Integer.class));

        artifactPosterFrameByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);

        verify(mockResponse).setContentType("image/png");
    }
}
