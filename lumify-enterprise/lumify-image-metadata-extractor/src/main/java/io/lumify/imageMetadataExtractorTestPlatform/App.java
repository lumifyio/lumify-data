package io.lumify.imageMetadataExtractorTestPlatform;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jon.hellmann on 6/18/14.
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Starting Program." );

        //Load an image.
        String imageFileName = "images/Apple iPhone 4S.jpg";
        try {
            InputStream input = new FileInputStream(imageFileName);
            ImageInputStream imageInput = ImageIO.createImageInputStream(input);
            BufferedImage bufImage = ImageIO.read(imageInput);

            System.out.println("Got here.");

        } catch (IOException e) {
            System.err.println("Caught FileNotFoundException or IOException. Filename: " + imageFileName);
            e.printStackTrace();
        }










        //Work on later..
        //TestImageMetadataGraphPropertyWorker worker = new TestImageMetadataGraphPropertyWorker();
        //Check if isHandled...
        //Run prepare method.
        //worker.execute();
        System.out.println( "Finished successfully." );
    }
}