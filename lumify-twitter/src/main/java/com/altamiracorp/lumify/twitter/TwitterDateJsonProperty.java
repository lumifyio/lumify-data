/*
 * Copyright 2014 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.core.json.JsonProperty;
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
public class TwitterDateJsonProperty extends JsonProperty<Long, String> {
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
    protected Long fromJSON(final String jsonValue) {
        try {
            Date date = TWITTER_DATE_FORMAT.get().parse(jsonValue);
            return date != null ? date.getTime() : null;
        } catch (ParseException pe) {
            LOGGER.trace("Error parsing Twitter date from value: %s", jsonValue, pe);
            return null;
        }
    }

    @Override
    protected String toJSON(final Long value) {
        return value != null ? TWITTER_DATE_FORMAT.get().format(new Date(value)) : null;
    }
}
