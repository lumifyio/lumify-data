package com.altamiracorp.lumify.entityExtraction;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.thirdparty.guava.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class PhoneNumberExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberExtractor.class);

    private static final String ENTITY_TYPE = "phoneNumber";
    private static final String DEFAULT_REGION_CODE = "defaultRegionCode";
    private static final String DEFAULT_DEFAULT_REGION_CODE = "US";

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private String defaultRegionCode;

    public void prepare(final Configuration configuration, final User user) {
        defaultRegionCode = configuration.get(DEFAULT_REGION_CODE, DEFAULT_DEFAULT_REGION_CODE);
    }

    public TermExtractionResult extract(final InputStream textInputStream) throws IOException {
        checkNotNull(textInputStream);

        LOGGER.info("Extracting phone numbers from provided text");

        final TermExtractionResult termExtractionResult = new TermExtractionResult();
        final String text = CharStreams.toString(new InputStreamReader(textInputStream, Charsets.UTF_8));

        final Iterable<PhoneNumberMatch> phoneNumbers = phoneNumberUtil.findNumbers(text, defaultRegionCode);
        for (final PhoneNumberMatch phoneNumber : phoneNumbers) {
            termExtractionResult.add(createTerm(phoneNumber));
        }

        LOGGER.info("Number of phone numbers extracted: " + Iterables.size(phoneNumbers));

        return termExtractionResult;
    }

    private TermExtractionResult.TermMention createTerm(final PhoneNumberMatch phoneNumber) {
        final String formattedNumber = phoneNumberUtil.format(phoneNumber.number(), PhoneNumberUtil.PhoneNumberFormat.E164);
        int start = phoneNumber.start();
        int end = phoneNumber.end();

        return new TermExtractionResult.TermMention(start, end, formattedNumber, ENTITY_TYPE, false);
    }
}
