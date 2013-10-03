package com.altamiracorp.lumify.entityExtraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.mapreduce.Mapper.Context;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class PhoneNumberExtractor extends EntityExtractor {
    private static final String ENTITY_TYPE = "phoneNumber";
    private static final String DEFAULT_REGION_CODE = "defaultRegionCode";
    private static final String DEFAULT_DEFAULT_REGION_CODE = "US";

    private PhoneNumberUtil phoneNumberUtil;
    private String defaultRegionCode;

    @Override
    public void setup(Context context, User user) throws IOException {
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        defaultRegionCode = context.getConfiguration().get(DEFAULT_REGION_CODE, DEFAULT_DEFAULT_REGION_CODE);
    }

    @Override
    public List<ExtractedEntity> extract(Artifact artifact, String text) throws Exception {
        Iterable<PhoneNumberMatch> phoneNumbers = phoneNumberUtil.findNumbers(text, defaultRegionCode);

        ArrayList<ExtractedEntity> termMentions = new ArrayList<ExtractedEntity>();
        for (PhoneNumberMatch phoneNumber : phoneNumbers) {
            termMentions.add(createTerm(artifact, phoneNumber));
        }

        return termMentions;
    }

    private ExtractedEntity createTerm(Artifact artifact, PhoneNumberMatch phoneNumber) {
        String name = phoneNumberUtil.format(phoneNumber.number(), PhoneNumberUtil.PhoneNumberFormat.E164);
        int start = phoneNumber.start();
        int end = phoneNumber.end();
        TermMention termMention = new TermMention(new TermMentionRowKey(artifact.getRowKey().toString(), start, end));
        termMention.getMetadata().setSign(name);
        termMention.getMetadata().setConcept(ENTITY_TYPE);
        return new ExtractedEntity(termMention, null);
    }
}
