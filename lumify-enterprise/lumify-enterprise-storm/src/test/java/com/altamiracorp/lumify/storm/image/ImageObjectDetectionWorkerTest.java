package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.objectDetection.ObjectDetector;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ImageObjectDetectionWorkerTest {
    private ImageObjectDetectionWorker worker;
    private AdditionalArtifactWorkData data;
    private FileSystem fs;
    private List<ArtifactDetectedObject> detectedObjects;

    @Mock
    com.altamiracorp.lumify.core.user.User user;

    @Mock
    ObjectDetector detector;

    @Mock
    Injector injector;

    @Before
    public void setup() throws Exception {
        worker = new ImageObjectDetectionWorker() {
            @Override
            protected Collection<ObjectDetector> getObjectDetectors() {
                ArrayList<ObjectDetector> objectDetectors = new ArrayList<ObjectDetector>();
                objectDetectors.add(detector);
                return objectDetectors;
            }
        };
        data = new AdditionalArtifactWorkData();
        fs = FileSystem.get(new Configuration());
        data.setHdfsFileSystem(fs);
        detectedObjects = new ArrayList<ArtifactDetectedObject>();
        detectedObjects.add(new ArtifactDetectedObject(0, 0, 0, 0, "face", null));
    }


    @Test
    public void testDoWork() throws Exception {
        Map stormConf = new HashMap<String, String>();
        worker.prepare(new TextExtractionWorkerPrepareData(stormConf, user, fs, injector));

        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("test.png"));
        when(detector.detectObjects(image)).thenReturn(detectedObjects);
        ArtifactExtractedInfo result = worker.doWork(image, data);
        List<ArtifactDetectedObject> artifactDetectedObjects = result.getDetectedObjects();
        assertEquals(1, artifactDetectedObjects.size());
        verify(detector, times(1)).init((Map) anyObject(), (FileSystem) anyObject());
    }
}
