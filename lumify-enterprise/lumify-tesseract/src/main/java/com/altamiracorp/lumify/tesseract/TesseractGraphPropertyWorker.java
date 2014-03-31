package com.altamiracorp.lumify.tesseract;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkResult;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class TesseractGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TesseractGraphPropertyWorker.class);
    private static final List<String> ICON_MIME_TYPES = Arrays.asList("image/x-icon", "image/vnd.microsoft.icon");
    private static final String TEXT_PROPERTY_KEY = TesseractGraphPropertyWorker.class.getName();
    private Tesseract tesseract;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);
        tesseract = Tesseract.getInstance();
    }

    @Override
    public GraphPropertyWorkResult execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
            LOGGER.error("Could not load image from property %s on vertex %s", data.getProperty().toString(), data.getVertex().getId());
            return null;
        }
        String ocrResults = extractTextFromImage(image);
        if (ocrResults == null) {
            return null;
        }

        InputStream textIn = new ByteArrayInputStream(ocrResults.getBytes());
        StreamingPropertyValue textValue = new StreamingPropertyValue(textIn, String.class);

        ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();
        // TODO set the test property in metadata so we can handle video frames
        RawLumifyProperties.TEXT.addPropertyValue(m, TEXT_PROPERTY_KEY, textValue, data.getVertex().getVisibility());
        m.save();
        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), TEXT_PROPERTY_KEY, RawLumifyProperties.TEXT.getKey());

        return new GraphPropertyWorkResult();
    }

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

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        if (mimeType == null) {
            return false;
        }
        if (ICON_MIME_TYPES.contains(mimeType)) {
            return false;
        }
        return mimeType.startsWith("image");
    }
}
