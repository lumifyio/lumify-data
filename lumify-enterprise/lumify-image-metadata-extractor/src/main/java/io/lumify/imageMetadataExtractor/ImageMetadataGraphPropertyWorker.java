package io.lumify.imageMetadataExtractor;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import io.lumify.core.exception.LumifyException;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import io.lumify.core.model.audit.AuditAction;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.core.util.RowKeyHelper;
import io.lumify.imageMetadataHelper.*;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageHelper;
import org.securegraph.Element;
import org.securegraph.Property;
import org.securegraph.Vertex;
import org.securegraph.mutation.ExistingElementMutation;
import org.securegraph.property.StreamingPropertyValue;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import com.google.i18n.phonenumbers.PhoneNumberUtil;


public class ImageMetadataGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ImageMetadataGraphPropertyWorker.class);
    private static final String MULTI_VALUE_KEY = ImageMetadataGraphPropertyWorker.class.getName();

    @Override
    public boolean isLocalFileRequired() {
        return true;
    }

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        LOGGER.info("Test -- Got to here - prepare method.");

    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        LOGGER.info("Test -- Got to here - execute method");

        //Create a reference to the local file.
        File imageFile = data.getLocalFile();

        //Retrieve the metadata from the image.
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

        //Get the date as a Date object using the metadata-extractor library.
        Date date = DateExtractor.getDateDefault(metadata);
        printDateInfo("imageMetadata date", date);

        //Add the Date Taken property.
        if (date != null) {
            Ontology.DATE_TAKEN.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, date, data.getVisibility(), getAuthorizations());
        }

        //Add the Orientation property.
        String orientation = OrientationExtractor.getOrientation(metadata);
        if (orientation != null) {
            Ontology.ORIENTATION.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, orientation, data.getVisibility(), getAuthorizations());
        }

        //Add the Orientation Description property.
        String orientationDescription = OrientationExtractor.getOrientationDescription(metadata);
        if (orientationDescription != null) {
            Ontology.ORIENTATION_DESCRIPTION.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, orientationDescription, data.getVisibility(), getAuthorizations());
        }

        //Add the Device Make property.
        String deviceMake = MakeExtractor.getMake(metadata);
        if (deviceMake != null) {
            Ontology.DEVICE_MAKE.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, deviceMake, data.getVisibility(), getAuthorizations());
        }

        //Add the Device Model property.
        String deviceModel = ModelExtractor.getModel(metadata);
        if (deviceModel != null) {
            Ontology.DEVICE_MODEL.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, deviceModel, data.getVisibility(), getAuthorizations());
        }

    }


    @Override
    public boolean isHandled(Element element, Property property) {

        LOGGER.info("Test -- Got to here - isHandled method 1.");

        if (property == null) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getPropertyName());
        if (mimeType == null) {
            return false;
        }
        //TODO. Checking for jpg only so far. Need to support other file types.
        if (mimeType.startsWith("image/jpeg")){
            LOGGER.info("Test -- Got to here - Starts with image/jpeg.");
            return true;
        } else {
            return false;
        }

    }

    public void printDateInfo(String name, Date date){
        if (date != null) {
            LOGGER.info(name + ", years since 1900: " + date.getYear());
            LOGGER.info(name + ", mon: " + date.getMonth());
            LOGGER.info(name + ", day: " + date.getDate());
            LOGGER.info(name + ", hours: " + date.getHours());
            LOGGER.info(name + ", min: " + date.getMinutes());
            LOGGER.info(name + ", sec: " + date.getSeconds());
        }
    }

    public void printPropertyInfo(GraphPropertyWorkData data){
        Iterator itr = data.getElement().getProperties().iterator();
        while(itr.hasNext())
        {
            Property property = (Property) itr.next();
            LOGGER.info("Test -- next Property: ");
            LOGGER.info("Test -- Got to here - execute method: property Name = " + property.getName() );
            LOGGER.info("Test -- Got to here - execute method: property Value = " + property.getValue() );
            LOGGER.info("Test -- Got to here - execute method: property Key = " + property.getKey() );
        }
    }

    public void printAllMetadata(Metadata metadata) {
        for(Directory directory : metadata.getDirectories()){
            for(Tag tag : directory.getTags()) {
                LOGGER.info("Tag: " + tag );
            }
        }
    }
}
