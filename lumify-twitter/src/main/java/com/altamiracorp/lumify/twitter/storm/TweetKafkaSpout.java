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

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import com.altamiracorp.lumify.core.bootstrap.InjectHelper;
import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.storm.LumifyKafkaSpout;
import com.altamiracorp.lumify.twitter.LumifyTwitterProcessor;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * This Spout reads raw Twitter JSON from a Kafka queue and parses a
 * Tweet, generating the Tweet's GraphVertex and providing that as the
 * 
 */
public class TweetKafkaSpout extends LumifyKafkaSpout {
    /**
     * The twitter processor.
     */
    private LumifyTwitterProcessor twitterProcessor;
    
    /**
     * Createa a new TweetKafkaSpout.
     * @param configuration the configuration
     * @param queueName the queueName
     * @param startOffsetTime the startOffsetTime
     */
    public TweetKafkaSpout(Configuration configuration, String queueName, Long startOffsetTime) {
        super(configuration, queueName, new TweetKafkaEncoder(), startOffsetTime);
    }

    
    @Override
    public void fail(final Object messageId) {
        if (messageId instanceof TweetMessageId) {
            super.fail(((TweetMessageId)messageId).getOriginalId());
        } else {
            super.fail(messageId);
        }
    }

    @Override
    public void ack(final Object messageId) {
        if (messageId instanceof TweetMessageId) {
            TweetMessageId tweetMessageId = (TweetMessageId) messageId;
            twitterProcessor.finalizeTweetVertex(getClass().getName(), tweetMessageId.getTweetVertexId());
        } else {
            super.ack(messageId);
        }
    }

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        super.open(conf, topologyContext, new TweetSpoutOutputCollector(collector));
        InjectHelper.inject(this);
        InjectHelper.inject(getScheme());
    }
    
    @Inject
    public void setTwitterProcessor(final LumifyTwitterProcessor proc) {
        twitterProcessor = proc;
    }
    
    /**
     * This object wraps the messageId provided by calls to emit() from
     * the base class nextTuple() method so we can perform any final
     * processing on the Tweet once it has been fully processed by
     * the Storm topology.
     */
    private static class TweetMessageId {
        /**
         * The Tweet vertex ID.
         */
        private final String tweetVertexId;
        
        /**
         * The original message ID.
         */
        private final Object originalId;

        /**
         * Create a new TweetMessageId.
         * @param vertexId the tweet vertex ID
         * @param origId the original message ID
         */
        public TweetMessageId(final String vertexId, final Object origId) {
            this.tweetVertexId = vertexId;
            this.originalId = origId;
        }

        public String getTweetVertexId() {
            return tweetVertexId;
        }

        public Object getOriginalId() {
            return originalId;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.tweetVertexId != null ? this.tweetVertexId.hashCode() : 0);
            hash = 29 * hash + (this.originalId != null ? this.originalId.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TweetMessageId other = (TweetMessageId) obj;
            if ((this.tweetVertexId == null) ? (other.tweetVertexId != null) : !this.tweetVertexId.equals(other.tweetVertexId)) {
                return false;
            }
            if (this.originalId != other.originalId && (this.originalId == null || !this.originalId.equals(other.originalId))) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * This SpoutOutputCollector intercepts the emit() call from the KafkaSpout and changes
     * the messageId to include the Lumify GraphVertex ID for the created Tweet vertex.
     */
    private static class TweetSpoutOutputCollector extends SpoutOutputCollector {
        /**
         * The delegate output collector.
         */
        private final SpoutOutputCollector delegate;
        
        /**
         * Create a new TweetSpoutOutputCollector.
         * @param delegate the collector to delegate to
         */
        public TweetSpoutOutputCollector(SpoutOutputCollector delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        public List<Integer> emit(String streamId, List<Object> tuple, Object messageId) {
            return delegate.emit(streamId, tuple, wrapMessageId(tuple, messageId));
        }

        @Override
        public List<Integer> emit(List<Object> tuple, Object messageId) {
            return delegate.emit(tuple, wrapMessageId(tuple, messageId));
        }

        @Override
        public List<Integer> emit(List<Object> tuple) {
            return delegate.emit(tuple);
        }

        @Override
        public List<Integer> emit(String streamId, List<Object> tuple) {
            return delegate.emit(streamId, tuple);
        }

        @Override
        public void emitDirect(int taskId, String streamId, List<Object> tuple, Object messageId) {
            delegate.emitDirect(taskId, streamId, tuple, wrapMessageId(tuple, messageId));
        }

        @Override
        public void emitDirect(int taskId, List<Object> tuple, Object messageId) {
            delegate.emitDirect(taskId, tuple, wrapMessageId(tuple, messageId));
        }

        @Override
        public void emitDirect(int taskId, String streamId, List<Object> tuple) {
            delegate.emitDirect(taskId, streamId, tuple);
        }

        @Override
        public void emitDirect(int taskId, List<Object> tuple) {
            delegate.emitDirect(taskId, tuple);
        }

        @Override
        public void reportError(Throwable error) {
            delegate.reportError(error);
        }
        
        private Object wrapMessageId(final List<Object> tuple, final Object messageId) {
            try {
                GraphVertex vertex = (GraphVertex) tuple.get(TweetKafkaEncoder.TWEET_VERTEX_IDX);
                return new TweetMessageId((vertex != null ? vertex.getId() : null), messageId);
            } catch (IndexOutOfBoundsException unused) {
                return messageId;
            } catch (ClassCastException unused) {
                return messageId;
            }
        }
    }
}
