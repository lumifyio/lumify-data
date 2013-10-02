package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.MockSession;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TikaTextExtractorTest {
    private MockSession session;

    @Mock
    private User user;

    @Mock
    private ArtifactRepository artifactRepository;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        session = new MockSession();
        session.initializeTables(user);
    }

    @Test
    public void testExtract() throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        String data = "<html>";
//        data += "<head>";
//        data += "<title>Test Title</title>";
//        data += "<meta content=\"2013-01-01T18:09:20Z\" itemprop=\"datePublished\" name=\"pubdate\"/>";
//        data += "</head>";
//        data += "<body>";
//        data += "<div><table><tr><td>Menu1</td><td>Menu2</td><td>Menu3</td></tr></table></div>\n";
//        data += "\n";
//        data += "<h1>Five reasons why Windows 8 has failed</h1>\n";
//        data += "<p>The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.</p>\n";
//        data += "</body>";
//        data += "</html>";
//
//        TikaTextExtractor textExtractor = new TikaTextExtractor(artifactRepository);
//        Artifact artifact = new Artifact();
//        artifact.getContent().setDocArtifactBytes(data.getBytes());
//        artifact.getGenericMetadata().setMimeType("text/html");
//
//
//        when(artifactRepository.getRaw(eq(artifact), eq(user))).thenReturn(new ByteArrayInputStream(data.getBytes()));
//        ArtifactExtractedInfo info = textExtractor.extract(artifact, user);
//
//        assertEquals("Test Title", info.getSubject());
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        assertEquals("2013-01-01", df.format(info.getDate()));
//        assertEquals(
//                "Five reasons why Windows 8 has failed\n" +
//                        "The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.\n",
//                info.getText());
    }
}
