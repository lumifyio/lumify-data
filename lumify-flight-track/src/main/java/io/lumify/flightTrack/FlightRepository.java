package io.lumify.flightTrack;

import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import org.json.JSONObject;

public class FlightRepository {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FlightRepository.class);

    public void save(JSONObject json) {
        LOGGER.info(json.toString(2));
    }
}
