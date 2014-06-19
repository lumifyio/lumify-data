package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Created by jon.hellmann on 6/19/14.
 */
public class FakeImageMetadataGraphPropertyWorker {

    public void execute(InputStream in) throws Exception{
        //Retrieve the image from the input stream.
        ImageInputStream imageInput = ImageIO.createImageInputStream(in);
        BufferedImage bufImage = ImageIO.read(imageInput);

        //BufferedImage bufImage = ImageIO.read(in);
        if (bufImage == null) {
            System.out.println("Could not load image.");
            return;
        } else {
            System.out.println("Loaded the image");
        }

        /*
        Use this code to check that the image was found. It will display the image in a JFrame.
        JFrame frame = new JFrame("Image loaded from ImageInputStream");
        JLabel label = new JLabel(new ImageIcon(bufImage));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        */

        //ImageMetadataReader.readMetadata() needs a File, so we will write to a temp file.
        File tempImageFile = new File("tempFile.jpg");
        ImageIO.write(bufImage, "jpg", tempImageFile);
        //ImageWriter.
        //TODO. Will need to support more than just .jpg.

        //Retrieve the metadata from the image.
        Metadata metadata = ImageMetadataReader.readMetadata(tempImageFile);

        //Get the date.
        String dateString = DateExtractor.getDateDefault(metadata);

        System.out.println("dateString: " + dateString);


    }

}
