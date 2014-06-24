package io.lumify.imageMetadataHelper;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.xmp.XmpDirectory;

/**
 * Created by jon.hellmann on 6/24/14.
 */
public class OrientationExtractor {



    public static String getOrientation(Metadata metadata){

        String orientationString = null;

        ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
        if (exifDir != null) {
            orientationString = exifDir.getString(ExifIFD0Directory.TAG_ORIENTATION);
            if (orientationString != null && !orientationString.equals("none")) {
                return orientationString;
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
