package io.lumify.translate;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.google.common.io.Files;
import com.google.inject.Inject;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.securegraph.Element;
import org.securegraph.Property;
import org.securegraph.mutation.ExistingElementMutation;
import org.securegraph.property.StreamingPropertyValue;

import java.io.*;

public class TranslateGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TranslateGraphPropertyWorker.class);
    private Translator translator;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        File profileDirectory = createTempProfileDirectory();
        DetectorFactory.loadProfile(profileDirectory);
    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        String text = IOUtils.toString(in);
        if (text.length() < 50) {
            LOGGER.debug("Skipping language detection because the text is too short. (length: %d)", text.length());
            return;
        }

        String language;
        try {
            language = detectLanguage(text);
            if (language == null) {
                return;
            }
        } catch (Throwable ex) {
            LOGGER.warn("Could not detect language", ex);
            return;
        }

        ExistingElementMutation m = data.getElement().prepareMutation()
                .alterPropertyMetadata(data.getProperty(), RawLumifyProperties.META_DATA_LANGUAGE, language);

        boolean translated = false;
        String translatedTextPropertyKey = data.getProperty().getKey() + "#en";
        if (!language.equals("en")) {
            String translatedText = translator.translate(text, language, data);
            if (translatedText != null && translatedText.length() > 0) {
                Object translatedTextValue;
                if (data.getProperty().getValue() instanceof StreamingPropertyValue) {
                    translatedTextValue = new StreamingPropertyValue(new ByteArrayInputStream(translatedText.getBytes()), String.class);
                } else {
                    translatedTextValue = translatedText;
                }
                m.addPropertyValue(translatedTextPropertyKey, data.getProperty().getName(), translatedTextValue, data.getProperty().getVisibility());
                translated = true;
            }
        }

        m.save(getAuthorizations());

        if (translated) {
            getGraph().flush();
            getWorkQueueRepository().pushGraphPropertyQueue(data.getElement(), translatedTextPropertyKey, data.getProperty().getName());
        }
    }

    private String detectLanguage(String text) throws LangDetectException, IOException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        String lang = detector.detect();
        if (lang.length() == 0) {
            return null;
        }
        return lang;
    }

    @Override
    public boolean isHandled(Element element, Property property) {
        return isTextProperty(property);
    }

    public File createTempProfileDirectory() throws IOException {
        File tempDirectory = Files.createTempDir();
        tempDirectory.deleteOnExit();
        String[] filesList = getProfileFilesList();
        for (String profileFileName : filesList) {
            File profileFile = new File(tempDirectory, profileFileName);
            InputStream profileFileIn = TranslateGraphPropertyWorker.class.getResourceAsStream(profileFileName);
            OutputStream profileFileOut = new FileOutputStream(profileFile);
            try {
                LOGGER.info("Loading langdetect profile file: %s", profileFile);
                IOUtils.copy(profileFileIn, profileFileOut);
            } finally {
                profileFileIn.close();
                profileFileOut.close();
            }
            profileFile.deleteOnExit();
        }
        return tempDirectory;
    }

    public String[] getProfileFilesList() throws IOException {
        String filesListContents = IOUtils.toString(TranslateGraphPropertyWorker.class.getResourceAsStream("files.list"));
        return filesListContents.split("\n");
    }

    @Inject
    public void setTranslator(Translator translator) {
        this.translator = translator;
    }
}
