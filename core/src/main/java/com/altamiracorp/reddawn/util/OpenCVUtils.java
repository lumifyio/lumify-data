package com.altamiracorp.reddawn.util;

import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.opencv.core.CvType.CV_8UC3;

public class OpenCVUtils {

    public static Mat bufferedImageToMat (BufferedImage image) {
        if (image != null) {
            Mat mat = new Mat(image.getHeight(),image.getWidth(),CV_8UC3);
            byte[] pixelData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
            mat.put(0,0,pixelData);
            return mat;
        }
        return null;
    }

    public static BufferedImage matToBufferedImage (Mat mat) throws IOException {
        byte[] pixelData = new byte[(int)(mat.total() * mat.channels())];
        mat.get(0,0,pixelData);

        return ImageIO.read(new ByteArrayInputStream(pixelData));
    }
}
