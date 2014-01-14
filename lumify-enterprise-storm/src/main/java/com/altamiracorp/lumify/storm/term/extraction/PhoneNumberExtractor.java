package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.Preconditions.checkNotNull;

public class PhoneNumberExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(PhoneNumberExtractor.class);

    private static final String ENTITY_TYPE = "phoneNumber";
    static final String DEFAULT_REGION_CODE = "defaultRegionCode";
    static final String DEFAULT_DEFAULT_REGION_CODE = "US";

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private String defaultRegionCode;

    public void prepare(final Configuration configuration, final User user) {
        checkNotNull(configuration);
        checkNotNull(user);

        defaultRegionCode = configuration.get(DEFAULT_REGION_CODE, DEFAULT_DEFAULT_REGION_CODE);
    }

    public TermExtractionResult extract(final InputStream textInputStream) throws IOException {
        checkNotNull(textInputStream);

        LOGGER.debug("Extracting phone numbers from provided text");

        final TermExtractionResult termExtractionResult = new TermExtractionResult();
        final String text = CharStreams.toString(new InputStreamReader(textInputStream, Charsets.UTF_8));

        final Iterable<PhoneNumberMatch> phoneNumbers = phoneNumberUtil.findNumbers(text, defaultRegionCode);
        for (final PhoneNumberMatch phoneNumber : phoneNumbers) {
            termExtractionResult.add(createTerm(phoneNumber));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of phone numbers extracted: %d", Iterables.size(phoneNumbers));
        }

        return termExtractionResult;
    }

    private TermMention createTerm(final PhoneNumberMatch phoneNumber) {
        final String formattedNumber = phoneNumberUtil.format(phoneNumber.number(), PhoneNumberUtil.PhoneNumberFormat.E164);
        int start = phoneNumber.start();
        int end = phoneNumber.end();
        
        return new TermMention.Builder()
                .start(start)
                .end(end)
                .sign(formattedNumber)
                .ontologyClassUri(ENTITY_TYPE)
                .resolved(false)
                .useExisting(true)
                .process(getClass().getName())
                .build();
    }
}
