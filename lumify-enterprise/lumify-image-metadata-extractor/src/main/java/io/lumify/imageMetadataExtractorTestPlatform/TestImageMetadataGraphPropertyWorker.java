package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.sun.javafx.iio.ImageMetadata;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by jon.hellmann on 6/18/14.
 */
public class TestImageMetadataGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TestImageMetadataGraphPropertyWorker.class);
    private static final List<String> ICON_MIME_TYPES = Arrays.asList("image/x-icon", "image/vnd.microsoft.icon");
    private static final String TEXT_PROPERTY_KEY = TestImageMetadataGraphPropertyWorker.class.getName();
    private static final String CONFIG_DATA_PATH = "tesseract.dataPath";
    private Tesseract tesseract;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);
        tesseract = Tesseract.getInstance();

        String dataPath = getConfiguration().get(CONFIG_DATA_PATH);
        if (dataPath != null) {
            tesseract.setDatapath(dataPath);
        }
    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        //Keep.
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
            LOGGER.error("Could not load image from property %s on vertex %s", data.getProperty().toString(), data.getElement().getId());
            return;
        }

        //Old - Delete.
        //String ocrResults = extractTextFromImage(image);

        //New.
        //Extract all of the metadata.
        try {
            //ImageMetadataReader.readMetadata() needs a File, so we will write to a temp file.
            File tempImageFile = new File("tempFile.jpg");
            ImageIO.write(image, "jpg", tempImageFile);
            //TODO. Will need to support more than just .jpg.

            Metadata metadata = ImageMetadataReader.readMetadata(tempImageFile);
            String dateString = DateExtractor.getDateDefault(metadata);

            System.out.println("dateString: " + dateString);

        } catch (ImageProcessingException e) {
            System.err.println("Caught ImageProcessingException: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }


        //Old - Delete later. Keep for reference.
        /*
        if (ocrResults == null) {
            return;
        }

        String textPropertyKey = RowKeyHelper.buildMinor(TEXT_PROPERTY_KEY, data.getProperty().getName(), data.getProperty().getKey());

        InputStream textIn = new ByteArrayInputStream(ocrResults.getBytes());
        StreamingPropertyValue textValue = new StreamingPropertyValue(textIn, String.class);

        ExistingElementMutation<Vertex> m = data.getElement().prepareMutation();
        Map<String, Object> textMetadata = data.createPropertyMetadata();
        textMetadata.put(RawLumifyProperties.META_DATA_TEXT_DESCRIPTION, "OCR Text");
        textMetadata.put(RawLumifyProperties.META_DATA_MIME_TYPE, "text/plain");
        RawLumifyProperties.TEXT.addPropertyValue(m, textPropertyKey, textValue, textMetadata, data.getVisibility());
        Vertex v = m.save(getAuthorizations());
        getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, m, v, TEXT_PROPERTY_KEY, getUser(), data.getVisibility());
        getAuditRepository().auditAnalyzedBy(AuditAction.ANALYZED_BY, v, getClass().getSimpleName(), getUser(), v.getVisibility());

        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), textPropertyKey, RawLumifyProperties.TEXT.getPropertyName());
        */
    }

    //Delete later. Keep for reference.
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

    //Keep. This method works fine the way it is.
    @Override
    public boolean isHandled(Element element, Property property) {
        if (property == null) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getPropertyName());
        if (mimeType == null) {
            return false;
        }
        if (ICON_MIME_TYPES.contains(mimeType)) {
            return false;
        }
        return mimeType.startsWith("image");

        //We may want to check the file extensions somehow. Because only some file extensions are supported.

    }
}
