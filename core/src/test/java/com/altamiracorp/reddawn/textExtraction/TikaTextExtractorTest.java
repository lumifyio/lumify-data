package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TikaTextExtractorTest {
    private MockSession session;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
    }

    @Test
    public void testExtract() throws Exception {
        String data = "<html>";
        data += "<head>";
        data += "<title>Test Title</title>";
        data += "<meta content=\"2013-01-01T18:09:20Z\" itemprop=\"datePublished\" name=\"pubdate\"/>";
        data += "</head>";
        data += "<body>";
        data += "<div><table><tr><td>Menu1</td><td>Menu2</td><td>Menu3</td></tr></table></div>\n";
        data += "\n";
        data += "<h1>Five reasons why Windows 8 has failed</h1>\n";
        data += "<p>The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.</p>\n";
        data += "</body>";
        data += "</html>";

        TikaTextExtractor textExtractor = new TikaTextExtractor();
        Artifact artifact = new Artifact();
        artifact.getContent().setDocArtifactBytes(data.getBytes());
        artifact.getGenericMetadata().setMimeType("text/plain");
        ArtifactExtractedInfo info = textExtractor.extract(session, artifact);

        assertEquals("Test Title", info.getSubject());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2013-01-01", df.format(info.getDate()));
        assertEquals(
                "Menu1 Menu2 Menu3     Five reasons why Windows 8 has failed  The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.\n",
                info.getText());
    }
}
