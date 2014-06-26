package io.lumify.imageMetadataExtractorTestPlatform;


import io.lumify.core.model.artifactThumbnails.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by jon.hellmann on 6/25/14.
 */
public class ImageRotationTesting {

    public void execute(File imageFile) throws Exception{
        BufferedImage old_img = ImageIO.read(imageFile);
        BufferedImage new_img = ImageUtils.tilt(old_img, Math.PI / 2, thumnbailType(old_img));
        showImageInFrame(old_img);
        showImageInFrame(new_img);
    }

    public void showImageInFrame(BufferedImage bufImage){
        showImageInFrame(bufImage, bufImage.getWidth(), bufImage.getHeight() );
    }

    public void showImageInFrame(BufferedImage bufImage, int frameWidth, int frameHeight){
        JFrame frame = new JFrame("Image loaded from ImageInputStream");
        JLabel label = new JLabel(new ImageIcon(bufImage));
        frame.getContentPane().add(label);
        //frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
    }


    //TODO. Duplicate code from ArtifactThumbnailRespository. Can delete later.
    private int thumnbailType(BufferedImage image) {
        if (image.getColorModel().getNumComponents() > 3) {
            return BufferedImage.TYPE_4BYTE_ABGR;
        }
        return BufferedImage.TYPE_INT_RGB;
    }

}
