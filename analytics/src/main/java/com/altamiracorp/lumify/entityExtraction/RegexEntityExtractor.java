package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexEntityExtractor extends EntityExtractor {
    private static final String REGULAR_EXPRESSION = "regularExpression";
    private static final String ENTITY_TYPE = "entityType";

    private Pattern pattern;
    private String entityType;

    @Override
    public void setup(Context context, User user) throws IOException {
        String regularExpression = context.getConfiguration().get(REGULAR_EXPRESSION);
        if (regularExpression == null) {
            throw new IOException("No regular expression was provided!");
        }

        this.pattern = Pattern.compile(regularExpression, Pattern.MULTILINE);

        entityType = context.getConfiguration().get(ENTITY_TYPE);
        if (entityType == null) {
            throw new IOException("No entity type for this regular expression was provided!");
        }
    }

    @Override
    List<ExtractedEntity> extract(Artifact artifact, String text) throws Exception {
        ArrayList<ExtractedEntity> extractedEntities = new ArrayList<ExtractedEntity>();
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String name = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            TermMention termMention = new TermMention(new TermMentionRowKey(artifact.getRowKey().toString(), start, end));
            termMention.getMetadata().setConcept(entityType);
            termMention.getMetadata().setSign(name);
            extractedEntities.add(new ExtractedEntity(termMention, null));
        }
        return extractedEntities;
    }
}
