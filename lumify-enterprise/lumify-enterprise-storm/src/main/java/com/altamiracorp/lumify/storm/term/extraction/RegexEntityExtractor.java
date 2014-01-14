package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegexEntityExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(RegexEntityExtractor.class);
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

        LOGGER.debug("Extractor prepared for entity type [%s] with regular expression: %s", entityType, regularExpression);
    }

    public TermExtractionResult extract(final InputStream textInputStream) throws IOException {
        checkNotNull(textInputStream);

        LOGGER.debug("Extracting pattern [%s] from provided text", pattern);

        final TermExtractionResult termExtractionResult = new TermExtractionResult();
        final String text = CharStreams.toString(new InputStreamReader(textInputStream, Charsets.UTF_8));

        final Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            termExtractionResult.add(createTerm(matcher));
        }

        LOGGER.debug("Number of patterns extracted: %d", termExtractionResult.getTermMentions().size());

        return termExtractionResult;
    }

    private TermMention createTerm(final Matcher matched) {
        final String patternGroup = matched.group();
        int start = matched.start();
        int end = matched.end();
        
        return new TermMention.Builder()
                .start(start)
                .end(end)
                .sign(patternGroup)
                .ontologyClassUri(entityType)
                .resolved(false)
                .useExisting(true)
                .process(getClass().getName())
                .build();
    }
}
