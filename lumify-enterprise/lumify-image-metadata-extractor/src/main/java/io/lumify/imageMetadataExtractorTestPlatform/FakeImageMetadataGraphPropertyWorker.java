package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.lumify.imageMetadataHelper.DateExtractor;

import java.io.File;

/**
 * Created by jon.hellmann on 6/19/14.
 */
public class FakeImageMetadataGraphPropertyWorker {

    public void execute(File imageFile) throws Exception{

        //Retrieve the metadata from the image.
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

        //Print the metadata to System.out. May give an error, based on adobe xmp.
        for(Directory directory : metadata.getDirectories()){
            for(Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }


        //Get the date.
        String dateString = DateExtractor.getDateDefault(metadata);
        System.out.println("dateString: " + dateString);


    }

}
