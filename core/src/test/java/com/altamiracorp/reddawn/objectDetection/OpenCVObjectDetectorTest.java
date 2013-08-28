package com.altamiracorp.reddawn.objectDetection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class OpenCVObjectDetectorTest {

    private static final String TEST_IMAGE = "cnn.jpg";
    private static final String CLASSIFIER = "haarcascade_frontalface_alt.xml";

    @Before
    public void setUp() throws Exception {
        System.out.println(System.getProperty("java.library.path"));
    }

    @Test
    public void testObjectDetection() throws IOException {
        /*OpenCVObjectDetector objectDetector = new OpenCVObjectDetector();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        BufferedImage bImage = ImageIO.read(cl.getResourceAsStream(TEST_IMAGE));

        List<DetectedObject> detectedObjectList = objectDetector.detectObjects(bImage, cl.getResource(CLASSIFIER).getPath());
        assertTrue("Incorrect number of objects found", detectedObjectList.size() == 1);

        DetectedObject detectedObject = detectedObjectList.get(0);
        assertEquals("X1 incorrect", 434, detectedObject.getX1());
        assertEquals("Y1 incorrect", 117, detectedObject.getY1());
        assertEquals("X2 incorrect", 637, detectedObject.getX2());
        assertEquals("Y2 incorrect", 320, detectedObject.getY2());*/
    }

}
