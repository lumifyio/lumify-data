package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class OpenCVObjectDetectorTest {

    private static final String TEST_IMAGE = "cnn.jpg";
    private static final String CLASSIFIER = "haarcascade_frontalface_alt.xml";

    @Mock
    VideoFrameRepository videoFrameRepository;

    @Before
    public void setUp() throws Exception {
        System.out.println(System.getProperty("java.library.path"));
    }

    @Test
    public void testObjectDetection() throws Exception {
        OpenCVObjectDetector objectDetector = new OpenCVObjectDetector();
        objectDetector.loadLibrary();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        BufferedImage bImage = ImageIO.read(cl.getResourceAsStream(TEST_IMAGE));

        CascadeClassifier objectClassifier = new CascadeClassifier(cl.getResource(CLASSIFIER).getPath());
        objectDetector.addObjectClassifier("face", objectClassifier);
        List<ArtifactDetectedObject> detectedObjectList = objectDetector.detectObjects(bImage);
        assertTrue("Incorrect number of objects found", detectedObjectList.size() == 1);

        ArtifactDetectedObject detectedObject = detectedObjectList.get(0);
        assertEquals("face", detectedObject.getConcept());
        assertEquals("X1 incorrect", "434", detectedObject.getX1());
        assertEquals("Y1 incorrect", "117", detectedObject.getY1());
        assertEquals("X2 incorrect", "637", detectedObject.getX2());
        assertEquals("Y2 incorrect", "320", detectedObject.getY2());
    }

}
