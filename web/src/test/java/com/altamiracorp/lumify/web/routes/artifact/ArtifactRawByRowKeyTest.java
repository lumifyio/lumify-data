package com.altamiracorp.lumify.web.routes.artifact;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.routes.RouteTestBase;

@RunWith(JUnit4.class)
public class ArtifactRawByRowKeyTest extends RouteTestBase {
    private ArtifactRawByRowKey artifactRawByRowKey;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        artifactRawByRowKey = new ArtifactRawByRowKey(mockArtifactRepository);
        artifactRawByRowKey.setApp(mockApp);
    }

    @Test
    public void testHandleTextFile() throws Exception {
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build("testContents".getBytes());
        when(mockRequest.getParameter("download")).thenReturn(null);
        when(mockRequest.getParameter("playback")).thenReturn(null);
        when(mockRequest.getAttribute("_rowKey")).thenReturn(artifactRowKey.toString());

        Artifact artifact = new Artifact(artifactRowKey);
        artifact.getGenericMetadata()
                .setFileName("testFile")
                .setFileExtension("testExt")
                .setMimeType("text/plain");
        when(mockArtifactRepository.findByRowKey(mockModelSession, artifactRowKey.toString())).thenReturn(artifact);

        InputStream testInputStream = new ByteArrayInputStream("test data".getBytes());
        when(mockArtifactRepository.getRaw(mockModelSession, artifact)).thenReturn(testInputStream);

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

        artifactRawByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);

        verify(mockResponse).setContentType("text/plain");
        verify(mockResponse).addHeader("Content-Disposition", "inline; filename=testFile.testExt");
    }

    @Test
    public void testHandleVideoPlayback() throws Exception {
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build("testContents".getBytes());
        when(mockRequest.getParameter("download")).thenReturn(null);
        when(mockRequest.getParameter("playback")).thenReturn("true");
        when(mockRequest.getParameter("type")).thenReturn("video/mp4");
        when(mockRequest.getHeader("Range")).thenReturn("bytes=1-4");
        when(mockRequest.getAttribute("_rowKey")).thenReturn(artifactRowKey.toString());

        Artifact artifact = new Artifact(artifactRowKey);
        artifact.getGenericMetadata()
                .setFileName("testFile")
                .setFileExtension("testExt")
                .setMimeType("text/plain");
        when(mockArtifactRepository.findByRowKey(mockModelSession, artifactRowKey.toString())).thenReturn(artifact);

        InputStream testInputStream = new ByteArrayInputStream("test data".getBytes());
        when(mockArtifactRepository.getRawMp4(mockModelSession, artifact)).thenReturn(testInputStream);
        when(mockArtifactRepository.getRawMp4Length(mockModelSession, artifact)).thenReturn((long) "test data".length());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                byte[] data = (byte[]) invocation.getArguments()[0];
                int start = (Integer) invocation.getArguments()[1];
                int len = (Integer) invocation.getArguments()[2];

                assertEquals(0, start);
                assertEquals(4, len);
                assertEquals("est ", new String(data, start, len));
                return null;
            }
        }).when(mockResponseOutputStream).write(any(byte[].class), any(Integer.class), any(Integer.class));

        artifactRawByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);

        verify(mockResponse).setContentType("video/mp4");
        verify(mockResponse).addHeader("Content-Disposition", "attachment; filename=testFile.testExt.mp4");
        verify(mockResponse).addHeader("Content-Length", "4");
        verify(mockResponse).addHeader("Content-Range", "bytes 1-4/9");
    }
}
