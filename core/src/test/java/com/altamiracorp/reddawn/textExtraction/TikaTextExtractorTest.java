package com.altamiracorp.reddawn.textExtraction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TikaTextExtractorTest {
  @Test
  public void testExtract() throws Exception {
    String data = "<html>";
    data += "<head><title>Test Title</title></head>";
    data += "<body>";
    data += "test content";
    data += "</body>";
    data += "</html>";

    TikaTextExtractor textExtractor = new TikaTextExtractor();
    ExtractedInfo info = textExtractor.extract(new ByteArrayInputStream(data.getBytes()));

    assertEquals("text/html; charset=ISO-8859-1", info.getMediaType());
    assertEquals("Test Title", info.getSubject());
    assertEquals("test content", info.getText());
  }
}
