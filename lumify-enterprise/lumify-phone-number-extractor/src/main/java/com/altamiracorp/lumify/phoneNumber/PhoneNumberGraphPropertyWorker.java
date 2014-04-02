package com.altamiracorp.lumify.phoneNumber;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(PhoneNumberGraphPropertyWorker.class);
    public static final String ENTITY_TYPE = "phoneNumber.entityType";
    public static final String DEFAULT_ENTITY_TYPE = "http://lumify.io/dev#phoneNumber";
    public static final String DEFAULT_REGION_CODE = "phoneNumber.defaultRegionCode";
    public static final String DEFAULT_DEFAULT_REGION_CODE = "US";

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private String defaultRegionCode;
    private String entityType;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        defaultRegionCode = (String) workerPrepareData.getStormConf().get(DEFAULT_REGION_CODE);
        if (defaultRegionCode == null) {
            defaultRegionCode = DEFAULT_DEFAULT_REGION_CODE;
        }

        entityType = (String) workerPrepareData.getStormConf().get(ENTITY_TYPE);
        if (entityType == null) {
            entityType = DEFAULT_ENTITY_TYPE;
        }
    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        LOGGER.debug("Extracting phone numbers from provided text");

        final String text = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));

        final Iterable<PhoneNumberMatch> phoneNumbers = phoneNumberUtil.findNumbers(text, defaultRegionCode);
        List<TermMention> termMentions = new ArrayList<TermMention>();
        for (final PhoneNumberMatch phoneNumber : phoneNumbers) {
            TermMention termMention = createTerm(phoneNumber, data.getProperty().getKey(), data.getVertex().getVisibility());
            termMentions.add(termMention);
        }

        saveTermMentions(data.getVertex(), termMentions);
        getGraph().flush();

        LOGGER.debug("Number of phone numbers extracted: %d", Iterables.size(phoneNumbers));
    }

    private TermMention createTerm(final PhoneNumberMatch phoneNumber, String propertyKey, Visibility visibility) {
        final String formattedNumber = phoneNumberUtil.format(phoneNumber.number(), PhoneNumberUtil.PhoneNumberFormat.E164);
        int start = phoneNumber.start();
        int end = phoneNumber.end();

        return new TermMention.Builder(start, end, formattedNumber, entityType, propertyKey, visibility)
                .resolved(false)
                .useExisting(true)
                .process(getClass().getName())
                .build();
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        return !(mimeType == null || !mimeType.startsWith("text"));
    }
}
