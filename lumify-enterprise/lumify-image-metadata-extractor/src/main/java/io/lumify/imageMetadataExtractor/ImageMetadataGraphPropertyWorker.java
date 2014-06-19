package io.lumify.imageMetadataExtractor;

import io.lumify.core.exception.LumifyException;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import io.lumify.core.model.audit.AuditAction;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.core.util.RowKeyHelper;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.i18n.phonenumbers.PhoneNumberUtil;


public class ImageMetadataGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ImageMetadataGraphPropertyWorker.class);
    public static final String CONFIG_PHONE_NUMBER_IRI = "ontology.iri.phoneNumber";
    public static final String DEFAULT_REGION_CODE = "phoneNumber.defaultRegionCode";
    public static final String DEFAULT_DEFAULT_REGION_CODE = "US";

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
        //LOGGER.error("Test -- Could not load image from property %s on vertex %s", data.getProperty().toString(), data.getElement().getId());
        LOGGER.info("Test -- Got to here - execute method");

        File myFile = data.getLocalFile()

        //BufferedImage image = ImageIO.read(in);
        //if (image == null) {
        //    LOGGER.error("Could not load image from property %s on vertex %s", data.getProperty().toString(), data.getElement().getId());
        //    return;
        //}
        //String ocrResults = extractTextFromImage(image);
        //if (ocrResults == null) {
        //    return;
        //}

        //String textPropertyKey = RowKeyHelper.buildMinor(TEXT_PROPERTY_KEY, data.getProperty().getName(), data.getProperty().getKey());

        //InputStream textIn = new ByteArrayInputStream(ocrResults.getBytes());
        //StreamingPropertyValue textValue = new StreamingPropertyValue(textIn, String.class);

        //ExistingElementMutation<Vertex> m = data.getElement().prepareMutation();
        //Map<String, Object> textMetadata = data.createPropertyMetadata();
        //textMetadata.put(RawLumifyProperties.META_DATA_TEXT_DESCRIPTION, "OCR Text");
        //textMetadata.put(RawLumifyProperties.META_DATA_MIME_TYPE, "text/plain");
        //RawLumifyProperties.TEXT.addPropertyValue(m, textPropertyKey, textValue, textMetadata, data.getVisibility());
        //Vertex v = m.save(getAuthorizations());
        //getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, m, v, TEXT_PROPERTY_KEY, getUser(), data.getVisibility());
        //getAuditRepository().auditAnalyzedBy(AuditAction.ANALYZED_BY, v, getClass().getSimpleName(), getUser(), v.getVisibility());

        //getGraph().flush();
        //getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), textPropertyKey, RawLumifyProperties.TEXT.getPropertyName());
    }

    /*
    private String extractTextFromImage(BufferedImage image) throws TesseractException {
        BufferedImage grayImage = ImageHelper.convertImageToGrayscale(image);
        String ocrResults = tesseract.doOCR(grayImage).replaceAll("\\n{2,}", "\n");
        if (ocrResults == null || ocrResults.trim().length() == 0) {
            return null;
        }
        ocrResults = ocrResults.trim();
        // TODO remove the trash that doesn't seem to be words
        return ocrResults;
    }
    */

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
        //if (ICON_MIME_TYPES.contains(mimeType)) {
        //    return false;
        // }
        return mimeType.startsWith("image");

    }
}
