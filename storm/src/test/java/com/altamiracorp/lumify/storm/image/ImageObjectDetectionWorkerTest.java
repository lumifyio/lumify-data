package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.objectDetection.OpenCVObjectDetector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ImageObjectDetectionWorkerTest {
    private ImageObjectDetectionWorker worker;
    @Mock
    com.altamiracorp.lumify.core.user.User user;
    AdditionalArtifactWorkData data;
    FileSystem fs;
    @Mock
    OpenCVObjectDetector detector;

    @Before
    public void setup() throws Exception {
        worker = new ImageObjectDetectionWorker();
        data = new AdditionalArtifactWorkData();
        fs = FileSystem.get(new Configuration());
        data.setHdfsFileSystem(fs);
        worker.setObjectDetector(detector);
    }


    @Test
    public void testDoWork() throws Exception {
        Map stormConf = new HashMap<String, String>();
        stormConf.put("objectdetection.classifierConcepts", "face");
        stormConf.put("objectdetection.classifier.face.path", getClass().getResource("face.xml").getPath());
        worker.prepare(stormConf, user);

        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("test.png"));
        ArtifactExtractedInfo result = worker.doWork(image, data);
        assertNotNull(result);

        //todo check for faces
    }

}
