package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import io.lumify.imageMetadataHelper.DateExtractorInStringFormat;
import io.lumify.imageMetadataHelper.MakeExtractor;
import io.lumify.imageMetadataHelper.OrientationExtractorOld;

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

        //Testing. Add the Orientation property.
        Integer orientation = OrientationExtractorOld.getOrientation(metadata);
        System.out.println("Orientation is :" + orientation);

        //Testing2.
        Integer orientationInteger = null;
        ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
        if (exifDir != null) {
            orientationInteger = exifDir.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            System.out.println("Orientation raw is:" + orientationInteger);
        } else {
            System.out.println("Orientation raw is: exifDir was null");
        }



        //Get the date.
        String dateString = DateExtractorInStringFormat.getDateDefault(metadata);
        System.out.println("dateString: " + dateString);

        String makeString = MakeExtractor.getMake(metadata);
        System.out.println("makeString: " + makeString);

    }

}
