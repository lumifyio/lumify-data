package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.model.termMention.TermMentionRowKey;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.hadoop.mapreduce.Mapper.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class PhoneNumberExtractor extends EntityExtractor {
    private static final String ENTITY_TYPE = "phoneNumber";
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
    public Collection<TermMention> extract(Artifact artifact, String text) throws Exception {
        Iterable<PhoneNumberMatch> phoneNumbers = phoneNumberUtil.findNumbers(text, defaultRegionCode);

        ArrayList<TermMention> termMentions = new ArrayList<TermMention>();
        for (PhoneNumberMatch phoneNumber : phoneNumbers) {
            termMentions.add(createTerm(artifact, phoneNumber));
        }

        return termMentions;
    }

    private TermMention createTerm(Artifact artifact, PhoneNumberMatch phoneNumber) {
        String name = phoneNumberUtil.format(phoneNumber.number(), PhoneNumberUtil.PhoneNumberFormat.E164);
        int start = phoneNumber.start();
        int end = phoneNumber.end();
        TermMention termMention = new TermMention(new TermMentionRowKey(artifact.getRowKey().toString(), start, end));
        termMention.getMetadata().setSign(name);
        termMention.getMetadata().setConcept(ENTITY_TYPE);
        return termMention;
    }
}
