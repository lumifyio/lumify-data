package io.lumify.tesseract;

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
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesseractGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TesseractGraphPropertyWorker.class);
    private static final List<String> ICON_MIME_TYPES = Arrays.asList("image/x-icon", "image/vnd.microsoft.icon");
    private static final String TEXT_PROPERTY_KEY = TesseractGraphPropertyWorker.class.getName();
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
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
            LOGGER.error("Could not load image from property %s on vertex %s", data.getProperty().toString(), data.getElement().getId());
            return;
        }
        String ocrResults = extractTextFromImage(image);
        if (ocrResults == null) {
            return;
        }

        String textPropertyKey = RowKeyHelper.buildMinor(TEXT_PROPERTY_KEY, data.getProperty().getName(), data.getProperty().getKey());

        InputStream textIn = new ByteArrayInputStream(ocrResults.getBytes());
        StreamingPropertyValue textValue = new StreamingPropertyValue(textIn, String.class);

        ExistingElementMutation<Vertex> m = data.getElement().prepareMutation();
        Map<String, Object> textMetadata = new HashMap<String, Object>(data.getPropertyMetadata());
        textMetadata.put(RawLumifyProperties.META_DATA_TEXT_DESCRIPTION, "OCR");
        textMetadata.put(RawLumifyProperties.META_DATA_MIME_TYPE, "text/plain");
        RawLumifyProperties.TEXT.addPropertyValue(m, textPropertyKey, textValue, textMetadata, data.getVisibility());
        Vertex v = m.save(getAuthorizations());
        getAuditRepository().auditVertexElementMutation(AuditAction.UPDATE, m, v, TEXT_PROPERTY_KEY, getUser(), data.getVisibility());
        getAuditRepository().auditAnalyzedBy(AuditAction.ANALYZED_BY, v, getClass().getSimpleName(), getUser(), v.getVisibility());

        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), textPropertyKey, RawLumifyProperties.TEXT.getKey());
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
    public boolean isHandled(Element element, Property property) {
        if (property == null) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getKey());
        if (mimeType == null) {
            return false;
        }
        if (ICON_MIME_TYPES.contains(mimeType)) {
            return false;
        }
        return mimeType.startsWith("image");
    }
}
