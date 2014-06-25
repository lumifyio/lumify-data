package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.xmp.XmpDirectory;

/**
 * Created by jon.hellmann on 6/24/14.
 */
public class OrientationExtractorOld {

    public static Integer getOrientation(Metadata metadata){

        Integer orientationInteger = null;

        ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
        if (exifDir != null) {
            orientationInteger = exifDir.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            if (orientationInteger != null) {
                return orientationInteger;
            }
        }

        return null;
    }

    public static String getOrientationDescription(Metadata metadata){

        String orientationDescription = null;

        ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
        if (exifDir != null) {
            orientationDescription = exifDir.getDescription(ExifIFD0Directory.TAG_ORIENTATION);
            if (orientationDescription != null && !orientationDescription.equals("none")) {
                return orientationDescription;
            }
        }

        return null;
    }

}
