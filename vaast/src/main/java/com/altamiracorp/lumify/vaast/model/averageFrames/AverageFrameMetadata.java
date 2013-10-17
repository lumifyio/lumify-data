package com.altamiracorp.lumify.vaast.model.averageFrames;

import com.altamiracorp.lumify.model.Value;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameMetadata;

public class AverageFrameMetadata extends VideoFrameMetadata {

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String CHANNELS = "channels";

    public int getWidth () {
        return Value.toInteger(get(WIDTH));
    }

    public AverageFrameMetadata setWidth (int width) {
        set(WIDTH, width);
        return this;
    }

    public int getHeight () {
        return Value.toInteger(get(HEIGHT));
    }

    public AverageFrameMetadata setHeight (int height) {
        set(HEIGHT,height);
        return this;
    }

    public int getChannels () {
        return Value.toInteger(get(CHANNELS));
    }

    public AverageFrameMetadata setChannels(int channels) {
        set(CHANNELS,channels);
        return this;
    }
}
