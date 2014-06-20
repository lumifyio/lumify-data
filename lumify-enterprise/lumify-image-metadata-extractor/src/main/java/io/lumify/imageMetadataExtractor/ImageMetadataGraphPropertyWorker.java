package io.lumify.imageMetadataExtractor;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.lumify.core.exception.LumifyException;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import io.lumify.core.model.audit.AuditAction;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.core.util.RowKeyHelper;
import io.lumify.imageMetadataHelper.DateExtractor;
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
    public static final String CONFIG_PHONE_NUMBER_IRI = "ontology.iri.phoneNumber";
    public static final String DEFAULT_REGION_CODE = "phoneNumber.defaultRegionCode";
    public static final String DEFAULT_DEFAULT_REGION_CODE = "US";
    private static final String MULTI_VALUE_KEY = ImageMetadataGraphPropertyWorker.class.getName();

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private String defaultRegionCode;
    private String entityType;

    @Override
    public boolean isLocalFileRequired() {
        return true;
    }

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        LOGGER.info("Test -- Got to here - prepare method 1.");

        //TODO. Not sure what this code is doing/ Delete it?.
        defaultRegionCode = (String) workerPrepareData.getStormConf().get(DEFAULT_REGION_CODE);
        if (defaultRegionCode == null) {
            defaultRegionCode = DEFAULT_DEFAULT_REGION_CODE;
        }

        entityType = (String) workerPrepareData.getStormConf().get(CONFIG_PHONE_NUMBER_IRI);
        if (entityType == null || entityType.length() == 0) {
            throw new LumifyException("Could not find config: " + CONFIG_PHONE_NUMBER_IRI);
        }

        LOGGER.info("Test -- Got to here - prepare method 2.");

    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        LOGGER.info("Test -- Got to here - execute method");


        /*
        Iterator itr = data.getElement().getProperties().iterator();
        while(itr.hasNext())
        {
            Property property = (Property) itr.next();
            LOGGER.info("Test -- next Property: ");
            LOGGER.info("Test -- Got to here - execute method: property Name = " + property.getName() );
            LOGGER.info("Test -- Got to here - execute method: property Value = " + property.getValue() );
            LOGGER.info("Test -- Got to here - execute method: property Key = " + property.getKey() );
        }
        */

        //Create a reference to the local file.
        File imageFile = data.getLocalFile();

        //Retrieve the metadata from the image.
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

        //Test printing out the metadata.
        /*
        for(Directory directory : metadata.getDirectories()){
            for(Tag tag : directory.getTags()) {
                LOGGER.info("Tag: " + tag );
            }
        }
        */

        //Get the date.
        String dateString = DateExtractor.getDateDefault(metadata);
        LOGGER.info("dateString: " + dateString );

        //Add the Property.
        Ontology.ORIENTATION.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, dateString, data.getVisibility(), getAuthorizations());
        //Ontology.ORIENTATION.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, "hor", data.getVisibility(), getAuthorizations());

        String makeString = "Apple";
        Ontology.DEVICE_MAKE.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, makeString, data.getVisibility(), getAuthorizations());

        String modelString = "iPhone 4S";
        Ontology.DEVICE_MODEL.addPropertyValue(data.getElement(), MULTI_VALUE_KEY, modelString, data.getVisibility(), getAuthorizations());


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
}
