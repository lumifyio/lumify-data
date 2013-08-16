package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class PhoneNumberExtractor extends EntityExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberExtractor.class);
    private static final String ENTITY_TYPE = "phoneNumber";
    private static final String MODEL_NAME = "libphonenumber";
    private static final String EXTRACTOR_ID = "PhoneNumber";
    private static final String DEFAULT_REGION_CODE = "defaultRegionCode";
    private static final String DEFAULT_DEFAULT_REGION_CODE = "US";

    private PhoneNumberUtil phoneNumberUtil;
    private String defaultRegionCode;

    @Override
    public void setup(Context context) throws IOException {
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        defaultRegionCode = context.getConfiguration().get(DEFAULT_REGION_CODE, DEFAULT_DEFAULT_REGION_CODE);
    }

    @Override
    public Collection<Term> extract(Sentence sentence) throws Exception {
        LOGGER.info("Extracting phone numbers from sentence: " + sentence.getRowKey().toString());
        Iterable<PhoneNumberMatch> phoneNumbers = phoneNumberUtil.findNumbers(sentence.getData().getText(), defaultRegionCode);

        ArrayList<Term> terms = new ArrayList<Term>();
        for (PhoneNumberMatch phoneNumber : phoneNumbers) {
            terms.add(createTerm(sentence, sentence.getData().getStart(), phoneNumber));
        }

        return terms;
    }

    @Override
    protected String getModelName() {
        return MODEL_NAME;
    }

    @Override
    String getExtractorId() {
        return EXTRACTOR_ID;
    }

    private Term createTerm(Sentence sentence, Long charOffset, PhoneNumberMatch phoneNumber) {
        return createTerm(sentence,
                charOffset,
                phoneNumberUtil.format(phoneNumber.number(), PhoneNumberUtil.PhoneNumberFormat.E164),
                getEntityType(),
                phoneNumber.start(),
                phoneNumber.end());
    }

    protected String getEntityType() {
        return ENTITY_TYPE;
    }
}
