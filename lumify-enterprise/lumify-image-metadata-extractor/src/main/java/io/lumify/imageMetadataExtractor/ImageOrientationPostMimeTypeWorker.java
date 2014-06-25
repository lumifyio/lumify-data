package io.lumify.imageMetadataExtractor;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.PostMimeTypeWorker;
import org.securegraph.Authorizations;

import java.io.File;

public class ImageOrientationPostMimeTypeWorker extends PostMimeTypeWorker {
    private static final String MULTI_VALUE_PROPERTY_KEY = ImageOrientationPostMimeTypeWorker.class.getName();

    @Override
    public void execute(String mimeType, GraphPropertyWorkData data, Authorizations authorizations) throws Exception {
        if (!mimeType.startsWith("image")) {
            return;
        }

        File localFile = getLocalFileForRaw(data.getElement());
        ImageTransform imageTransform = getImageOrientation(localFile);
        if (imageTransform != null) {
            data.getElement().addPropertyValue(
                    MULTI_VALUE_PROPERTY_KEY,
                    Ontology.CW_ROTATION_NEEDED.getPropertyName(),
                    imageTransform.getCWRotationNeeded(),
                    data.getVisibility(),
                    authorizations);
            getGraph().flush();

            getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), MULTI_VALUE_PROPERTY_KEY, Ontology.CW_ROTATION_NEEDED.getPropertyName());
        }
    }

    private ImageTransform getImageOrientation(File localFile){

        //Original image orientation, with no flip needed, and no rotation needed.
        ImageTransform imageTransform = new ImageTransform(false, 0);

        try {
            //Attempt to retrieve the metadata from the image.
            Metadata metadata = ImageMetadataReader.readMetadata(localFile);
            ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
            if (exifDir != null) {
                Integer orientationInteger = exifDir.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
                if (orientationInteger != null) {
                    imageTransform = convertOrientation(orientationInteger);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageTransform;
    }

    private ImageTransform convertOrientation(int orientationInt){
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