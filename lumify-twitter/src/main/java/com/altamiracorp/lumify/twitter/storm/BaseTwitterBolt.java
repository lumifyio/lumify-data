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

package com.altamiracorp.lumify.twitter.storm;

import com.altamiracorp.lumify.storm.BaseLumifyJsonBolt;
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

    protected final LumifyTwitterProcessor getTwitterProcessor() {
        return twitterProcessor;
    }

    @Inject
    public final void setTwitterProcessor(final LumifyTwitterProcessor processor) {
        this.twitterProcessor = processor;
    }
}
