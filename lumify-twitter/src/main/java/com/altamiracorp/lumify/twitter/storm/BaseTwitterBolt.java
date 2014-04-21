package com.altamiracorp.lumify.twitter.storm;

import com.altamiracorp.lumify.twitter.BaseLumifyJsonBolt;
import com.altamiracorp.lumify.twitter.LumifyTwitterProcessor;
import com.google.inject.Inject;

/**
 * Base class for Twitter processing bolts.  All Twitter bolts
 * expect the serialized JSON Tweet object in the <code>json</code>
 * field of the input Tuple.
 */
public abstract class BaseTwitterBolt extends BaseLumifyJsonBolt {
    /**
     * The TwitterProcessor.
     */
    private LumifyTwitterProcessor twitterProcessor;

    /**
     * Create a new BaseTwitterBolt with the default missing and invalid JSON
     * handling policies.
     */
    protected BaseTwitterBolt() {
    }

    /**
     * Create a new BaseTwitterBolt with the provided missing and invalid JSON
     * handling policies.
     * @param missingPolicy the missing JSON policy
     * @param invalidPolicy the invalid JSON policy
     */
    protected BaseTwitterBolt(final JsonHandlingPolicy missingPolicy, final JsonHandlingPolicy invalidPolicy) {
        super(missingPolicy, invalidPolicy);
    }

    protected final LumifyTwitterProcessor getTwitterProcessor() {
        return twitterProcessor;
    }

    @Inject
    public final void setTwitterProcessor(final LumifyTwitterProcessor processor) {
        this.twitterProcessor = processor;
    }
}
