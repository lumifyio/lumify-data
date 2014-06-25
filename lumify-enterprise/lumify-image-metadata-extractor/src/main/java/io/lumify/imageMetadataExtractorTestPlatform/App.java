package io.lumify.imageMetadataExtractorTestPlatform;

import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import org.securegraph.Element;
import org.securegraph.Graph;
import org.securegraph.Vertex;
import org.securegraph.accumulo.AccumuloGraph;
import org.securegraph.accumulo.AccumuloGraphConfiguration;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jon.hellmann on 6/18/14.
 */
public class App
{
    private String myDirectoryPath = "images2";
    private String supportedExtensions[] = {".jpg", ".jpeg", ".bmp", ".tiff", ".gif", ".psd", ".nef", ".cr2", ".orf", ".arw", ".rw2"};
    //Removed support for crw (was not working).
    private int numImagesProcessed = 0;
    private ArrayList<String> rejectedFiles = new ArrayList<String>();

    public static void main( String[] args ) throws Exception
    {
        new App();
    }

    public App() throws Exception
    {

        System.out.println( "Starting Program." );

        //Iterate through all the images in a directory.
        File dir = new File(myDirectoryPath);
        File[] imageFiles = dir.listFiles();
        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                if (isSupportedFileExtension(imageFile, supportedExtensions)) {
                    System.out.println("\n#######################################################");
                    System.out.println("Filename: " + imageFile.getName());

                    //###########################
                    //FakeImageMetadataGraphPropertyWorker worker = new FakeImageMetadataGraphPropertyWorker();
                    //worker.execute(imageFile);

                    ImageRotationTesting testing = new ImageRotationTesting();
                    testing.execute(imageFile);

                    numImagesProcessed++;
                } else {
                    rejectedFiles.add(imageFile.getName());
                }
            }
        }

        //Print out some of the processing data.
        System.out.println("\nNumber of images processed: " + numImagesProcessed);
        System.out.println("Files Rejected: " + rejectedFiles);


        /*
        Use this code to check that the image was found. It will display the image in a JFrame.
        JFrame frame = new JFrame("Image loaded from ImageInputStream");
        JLabel label = new JLabel(new ImageIcon(bufImage));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        */

        System.out.println( "Finished program." );

    }

    public boolean isSupportedFileExtension(File file, String[] extensions)
    {
        for(String extension: extensions) {
            if (file.getName().toLowerCase().endsWith(extension)){
                return true;
            }
        }
        return false;
    }
}