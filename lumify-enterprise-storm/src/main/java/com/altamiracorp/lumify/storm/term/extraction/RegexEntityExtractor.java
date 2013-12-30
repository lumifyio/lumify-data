package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegexEntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexEntityExtractor.class);

    public static final String REGULAR_EXPRESSION = "regularExpression";
    public static final String ENTITY_TYPE = "entityType";

    private Pattern pattern;
    private String entityType;

    public void prepare(final Configuration configuration, final User user) throws IOException {
        checkNotNull(configuration);
        checkNotNull(user);

        final String regularExpression = configuration.get(REGULAR_EXPRESSION);
        if (regularExpression == null) {
            throw new IOException("No regular expression was provided!");
        }

        entityType = configuration.get(ENTITY_TYPE);
        if (entityType == null) {
            throw new IOException("No entity type for this regular expression was provided!");
        }

        pattern = Pattern.compile(regularExpression, Pattern.MULTILINE);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Extractor prepared for entity type [%s] with regular expression: %s", entityType, regularExpression));
        }
    }

    public TermExtractionResult extract(final InputStream textInputStream) throws IOException {
        checkNotNull(textInputStream);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Extracting pattern [%s] from provided text", pattern));
        }

        final TermExtractionResult termExtractionResult = new TermExtractionResult();
        final String text = CharStreams.toString(new InputStreamReader(textInputStream, Charsets.UTF_8));

        final Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            termExtractionResult.add(createTerm(matcher));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of patterns extracted: " + termExtractionResult.getTermMentions().size());
        }

        return termExtractionResult;
    }

    private TermExtractionResult.TermMention createTerm(final Matcher matched) {
        final String patternGroup = matched.group();
        int start = matched.start();
        int end = matched.end();

        return new TermExtractionResult.TermMention(start, end, patternGroup, entityType, false, null, null, true);
    }
}
