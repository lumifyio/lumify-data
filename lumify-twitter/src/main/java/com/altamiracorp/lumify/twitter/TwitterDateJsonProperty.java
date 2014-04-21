package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.twitter.json.JsonProperty;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A JSON property whose String value is formatted as a Twitter
 * timestamp: <code>EEE MMM dd HH:mm:ss ZZZZZ yyyy</code>.
 */
public class TwitterDateJsonProperty extends JsonProperty<Date, String> {
    /**
     * The class logger.
     */
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TwitterDateJsonProperty.class);

    /**
     * The Twitter Date format string.
     */
    private static final String TWITTER_DATE_FORMAT_STR = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    /**
     * ThreadLocal DateFormat for Twitter dates.  This minimizes creation of
     * non-thread-safe SimpleDateFormat objects and eliminates the need for
     * synchronization of a single DateFormat by creating one format for each
     * Thread.
     */
    private static final ThreadLocal<DateFormat> TWITTER_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat(TWITTER_DATE_FORMAT_STR);
            sdf.setLenient(true);
            return sdf;
        }
    };

    /**
     * Create a new TwitterDateJsonProperty.
     * @param key the property key
     */
    public TwitterDateJsonProperty(final String key) {
        super(key, JsonType.STRING);
    }

    @Override
    protected Date fromJSON(final String jsonValue) {
        Date date;
        try {
            date = TWITTER_DATE_FORMAT.get().parse(jsonValue);
        } catch (ParseException pe) {
            LOGGER.trace("Error parsing Twitter date from value: %s", jsonValue, pe);
            date = null;
        }
        return date;
    }

    @Override
    protected String toJSON(final Date value) {
        return value != null ? TWITTER_DATE_FORMAT.get().format(value) : null;
    }
}
