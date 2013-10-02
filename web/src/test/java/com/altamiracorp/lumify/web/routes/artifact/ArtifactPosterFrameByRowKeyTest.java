package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.altamiracorp.lumify.web.routes.RouteTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactPosterFrameByRowKeyTest extends RouteTestBase {
    private ArtifactPosterFrameByRowKey artifactPosterFrameByRowKey;

    @Mock
    private User user;

    @Mock
    private HttpSession mockSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final ArtifactThumbnailRepository mockThumbnailRepository = Mockito.mock(ArtifactThumbnailRepository.class);
        artifactPosterFrameByRowKey = new ArtifactPosterFrameByRowKey(mockArtifactRepository, mockThumbnailRepository);
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
//        when(mockArtifactRepository.findByRowKey(artifactRowKey.toString(), user)).thenReturn(artifact);
//
//        InputStream testInputStream = new ByteArrayInputStream("test data".getBytes());
//        when(mockArtifactRepository.getRawPosterFrame(artifact, user)).thenReturn(testInputStream);
//
//        doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) throws Throwable {
//                byte[] data = (byte[]) invocation.getArguments()[0];
//                int start = (Integer) invocation.getArguments()[1];
//                int len = (Integer) invocation.getArguments()[2];
//
//                assertEquals(0, start);
//                assertEquals(9, len);
//                assertEquals("test data", new String(data, start, len));
//                return null;
//            }
//        }).when(mockResponseOutputStream).write(any(byte[].class), any(Integer.class), any(Integer.class));
//
//        artifactPosterFrameByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);
//
//        verify(mockResponse).setContentType("image/png");
    }
}
