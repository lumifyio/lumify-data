package io.lumify.imageMetadataHelper;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import io.lumify.imageMetadataExtractor.ImageTransform;

import java.io.File;

/**
 * Created by jon.hellmann on 6/27/14.
 */
public class ImageTransformExtractor {

    public static ImageTransform getImageTransform(File localFile){

        //Original image orientation, with no flip needed, and no rotation needed.
        ImageTransform imageTransform = new ImageTransform(false, 0);

        try {
            //Attempt to retrieve the metadata from the image.
            Metadata metadata = ImageMetadataReader.readMetadata(localFile);
            ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
            if (exifDir != null) {
                Integer orientationInteger = exifDir.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
                if (orientationInteger != null) {
                    imageTransform = convertOrientationToTransform(orientationInteger);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageTransform;
    }

    public static ImageTransform convertOrientationToTransform(int orientationInt){
        switch (orientationInt){
            case 1: return new ImageTransform(false, 0);
            case 2: return new ImageTransform(true, 0);
            case 3: return new ImageTransform(false, 180);
            case 4: return new ImageTransform(true, 180);
            case 5: return new ImageTransform(true, 270);
            case 6: return new ImageTransform(false, 90);
            case 7: return new ImageTransform(true, 90);
            case 8: return new ImageTransform(false, 270);
            default:    return new ImageTransform(false, 0);
        }
    }
}
