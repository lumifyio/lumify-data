package io.lumify.imageMetadataExtractorTestPlatform;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by jon.hellmann on 6/25/14.
 */
public class ImageRotationTesting {

    public void execute(File imageFile) throws Exception{
        BufferedImage old_img = ImageIO.read(imageFile);
        int w = old_img.getWidth();
        int h = old_img.getHeight();

        BufferedImage new_img = new BufferedImage(h,w,BufferedImage.TYPE_INT_BGR);
        Graphics2D g2d = new_img.createGraphics();

        AffineTransform origXform = g2d.getTransform();
        AffineTransform newXform = (AffineTransform)(origXform.clone());
        // center of rotation is center of the panel
        double xRot = w/2.0;
        newXform.rotate(Math.toRadians(270.0), xRot, xRot); //270

        g2d.setTransform(newXform);
        // draw image centered in panel
        g2d.drawImage(old_img, 0, 0, null);
        // Reset to Original
        g2d.setTransform(origXform);
        //
        FileOutputStream out = new FileOutputStream("D:\\test2.jpg");
        try{
            ImageIO.write(new_img, "JPG", out);
        }finally{
            out.close();
        }
    }

}
