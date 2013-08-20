package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
import opennlp.tools.namefind.RegexNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexEntityExtractor extends EntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexEntityExtractor.class);
    private static final String MODEL_NAME = "RegularExpression";
    private static final String EXTRACTOR_ID = "RegularExpression";

    private static final String REGULAR_EXPRESSION = "regularExpression";
    private static final String ENTITY_TYPE = "entityType";

    private Pattern pattern;
    private String entityType;

    @Override
    public void setup(Context context) throws IOException{
        String regularExpression = context.getConfiguration().get(REGULAR_EXPRESSION);
        if (regularExpression == null) {
            throw new IOException("No regular expression was provided!");
        }

        this.pattern = Pattern.compile(regularExpression,Pattern.MULTILINE);

        entityType = context.getConfiguration().get(ENTITY_TYPE);
        if (entityType == null) {
            throw new IOException ("No entity type for this regular expression was provided!");
        }
    }

    @Override
    Collection<Term> extract(Sentence sentence) throws Exception {
        LOGGER.info("Extracting entities of type " + entityType + " from sentence: " + sentence.getRowKey().toString());
        ArrayList<Term> terms = new ArrayList<Term>();
        Matcher matcher = pattern.matcher(sentence.getData().getText());

        while (matcher.find()) {
            terms.add(createTerm(sentence,
                    sentence.getData().getStart(),
                    matcher.group(),
                    entityType,
                    matcher.start(),
                    matcher.end()));
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
}
