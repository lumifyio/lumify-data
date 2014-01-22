package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.ImageOcrTextExtractor;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageTextExtractorWorkerTest {
    private ImageTextExtractorWorker worker;
    @Mock
    ImageOcrTextExtractor extractor;
    AdditionalArtifactWorkData data;

    @Before
    public void setup() throws Exception {
        worker = new ImageTextExtractorWorker();
        worker.setImageOcrTextExtractor(extractor);
        data = new AdditionalArtifactWorkData();
        data.setMimeType("image/png");
        data.setHdfsFileSystem(FileSystem.get(new Configuration()));
    }

    @Test
    public void testNullImage() throws Exception {
        BufferedImage image = null;
        ArtifactExtractedInfo result = worker.doWork(image, data);
        assertNull(result);
    }

    @Test
    public void testImageWithNoText() throws Exception {
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("test.png"));
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        when(extractor.extractFromImage(image, "image/png")).thenReturn(info);
        ArtifactExtractedInfo result = worker.doWork(image, data);
        assertNull(result.getText());
    }

    @Test
    public void testDoWork() throws Exception {
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("test.png"));
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        info.setText("Test");
        when(extractor.extractFromImage(image, "image/png")).thenReturn(info);
        ArtifactExtractedInfo result = worker.doWork(image, data);
        assertEquals(info.getText(), result.getText());
        assertNull(result.getTextHdfsPath());
    }

    @Test
    public void testLargeImage() throws Exception {
        // TODO rewrite this test for secure graph!!!
//        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("test.png"));
//        String hugeText = IOUtils.toString(getClass().getResourceAsStream("hugeString.txt"));
//        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
//        info.setText(hugeText);
//        when(extractor.extractFromImage(image, "image/png")).thenReturn(info);
//        ArtifactExtractedInfo result = worker.doWork(image, data);
//        assertNull(info.getText());
//        assertNotNull(result.getRawHdfsPath());
    }

}
