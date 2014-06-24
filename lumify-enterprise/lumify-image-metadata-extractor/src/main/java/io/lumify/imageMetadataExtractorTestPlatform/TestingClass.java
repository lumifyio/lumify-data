package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.CanonMakernoteDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.KodakMakernoteDirectory;
import com.drew.metadata.exif.OlympusMakernoteDirectory;

/**
 * Created by jon.hellmann on 6/23/14.
 */
public class TestingClass {

    public static String getData(Metadata metadata){

        //Test getting the TAG_MODEL_ID from CanonMakerNote soon.
        String modelString = null;


        OlympusMakernoteDirectory olympDir = metadata.getDirectory(OlympusMakernoteDirectory.class);
        if(olympDir != null){
            modelString = olympDir.getDescription(OlympusMakernoteDirectory.TAG_OLYMPUS_ORIGINAL_MANUFACTURER_MODEL);
            if (modelString != null)
                return modelString;
        }


        return null;

    }
}
