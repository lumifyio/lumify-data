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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jon.hellmann on 6/18/14.
 */
public class App
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Starting Program." );

        //Load an image.
        String imageFileName = "images/Apple iPhone 4S.jpg";

        InputStream input = new FileInputStream(imageFileName);
        //ImageInputStream imageInput = ImageIO.createImageInputStream(input);
        //BufferedImage bufImage = ImageIO.read(imageInput);

        /*
        Use this code to check that the image was found. It will display the image in a JFrame.
        JFrame frame = new JFrame("Image loaded from ImageInputStream");
        JLabel label = new JLabel(new ImageIcon(bufImage));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        */

        //For Testing Purposes.
        FakeImageMetadataGraphPropertyWorker worker = new FakeImageMetadataGraphPropertyWorker();
        worker.execute(input);


        /*
        //See https://github.com/altamiracorp/securegraph for how to setup a graph and add vertices.

        // specify Accumulo config, more options than shown are available
        Map mapConfig = new HashMap();
        mapConfig.put(AccumuloGraphConfiguration.USE_SERVER_SIDE_ELEMENT_VISIBILITY_ROW_FILTER, false);
        mapConfig.put(AccumuloGraphConfiguration.ACCUMULO_INSTANCE_NAME, "instance_name");
        mapConfig.put(AccumuloGraphConfiguration.ACCUMULO_USERNAME, "username");
        mapConfig.put(AccumuloGraphConfiguration.ACCUMULO_PASSWORD, "password");
        mapConfig.put(AccumuloGraphConfiguration.ZOOKEEPER_SERVERS, "localhost");

        AccumuloGraphConfiguration graphConfig = new AccumuloGraphConfiguration(mapConfig);
        Graph graph = AccumuloGraph.create(graphConfig);

        //TestImageMetadataGraphPropertyWorker worker = new TestImageMetadataGraphPropertyWorker();
        //Check if isHandled...
        //Run prepare method.

        //Element element = new Element();
        //Vertex vertex = new Vertex();
        //GraphPropertyWorkData workData = new GraphPropertyWorkData();
        //worker.execute(imageInput, );
        */



        System.out.println("Got here.");



        System.out.println( "Finished program." );
    }
}