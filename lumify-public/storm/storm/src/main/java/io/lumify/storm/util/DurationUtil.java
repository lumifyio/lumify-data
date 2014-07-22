package io.lumify.storm.util;

import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import org.json.JSONObject;

public class DurationUtil {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DurationUtil.class);

    public static Double extractDurationFromJSON(JSONObject json) {
        if (json == null) {
            return null;
        }

        JSONObject formatJson = json.optJSONObject("format");
        if (formatJson != null) {
            Double duration = formatJson.optDouble("duration");
            if (duration != null && !Double.isNaN(duration)) {
                return duration;
            }
        }

        LOGGER.info("Could not retrieve a \"duration\" value from the JSON object.");
        return null;
    }
}
