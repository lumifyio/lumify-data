package com.altamiracorp.reddawn.contentTypeExtraction;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TikaContentTypeExtractorTest{

    @Test
    public void testTikaExtractContentType () throws Exception{
        TikaContentTypeExtractor tikaContentTypeExtractor = new TikaContentTypeExtractor();

        // Testing .m4v files
        InputStream h264 = TikaContentTypeExtractor.class.getResourceAsStream("/H_264.m4v");
        assertEquals("video/x-m4v", tikaContentTypeExtractor.extract(h264));

        // Testing .m2v files
        InputStream mpeg2 = TikaContentTypeExtractor.class.getResourceAsStream("/MPEG-2.m2v");
        assertEquals("video/mpeg", tikaContentTypeExtractor.extract(mpeg2));

        // Testing .mp4 files
        InputStream mpeg4 = TikaContentTypeExtractor.class.getResourceAsStream("/MPEG-4.mp4");
        assertEquals("video/mp4", tikaContentTypeExtractor.extract(mpeg4));

        // Testing .txt files
        InputStream txt = TikaContentTypeExtractor.class.getResourceAsStream("/hello.txt");
        assertEquals("text/plain; charset=ISO-8859-1", tikaContentTypeExtractor.extract(txt));

        // Testing .pdf files
        InputStream pdf = TikaContentTypeExtractor.class.getResourceAsStream("/hello.pdf");
        assertEquals("application/pdf", tikaContentTypeExtractor.extract(pdf));

        // Testing .jpg files
        InputStream jpg = TikaContentTypeExtractor.class.getResourceAsStream("/cat.jpg");
        assertEquals("image/jpeg", tikaContentTypeExtractor.extract(jpg));
    }

}
