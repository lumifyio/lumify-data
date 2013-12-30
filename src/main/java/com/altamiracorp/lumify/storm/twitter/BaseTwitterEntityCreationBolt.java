/*
 * Copyright 2013 Altamira Corporation.
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

package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRegexFinder;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.common.collect.Lists;

import java.util.List;

import static com.altamiracorp.lumify.core.model.ontology.PropertyName.*;
import static com.altamiracorp.lumify.storm.twitter.TwitterConstants.*;

/**
 * Base class for bolts that identify and create entities and
 * relationships from a Twitter record.
 */
public abstract class BaseTwitterEntityCreationBolt extends BaseTwitterForkBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(BaseTwitterEntityCreationBolt.class);

    /**
     * Create a new BaseTwitterEntityCreationBolt.
     *
     * @param boltId the bolt ID
     */
    protected BaseTwitterEntityCreationBolt(final String boltId) {
        super(boltId);
    }

    @Override
    protected final void executeFork(final Tuple input) throws Exception {
        LOGGER.info("[%s]: Executing on Tuple [%s]", getClass().getName(), input.getMessageId());
        // creates entities and relationships from the tweet found in
        // the input tuple, establishing those relationships with
        // the Vertex representing the extracted Tweet
        String tweetText = input.getStringByField(TWEET_TEXT_FIELD);
        String tweetVertexId = input.getStringByField(TWEET_VERTEX_ID_FIELD);
        String tweetVertexTitle = input.getStringByField(TWEET_VERTEX_TITLE_FIELD);

        // only process if tweetText is present
        if (tweetText != null && !tweetText.trim().isEmpty()) {
            User user = getUser();
            Concept concept = ontologyRepository.getConceptByName(getConceptName(), user);
            String conceptId = concept.getId();
            String termRegex = getTermRegex();
            String relationshipLabel = getRelationshipLabel();
            String relationshipDisplayName = ontologyRepository.getDisplayNameForLabel(relationshipLabel, user);

            GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
            List<TermMention> termMentions = TermRegexFinder.find(tweetVertexId, conceptVertex, tweetText, termRegex);
            List<String> modifiedProps = Lists.newArrayList(TITLE.toString(), ROW_KEY.toString(), TYPE.toString(), SUBTYPE.toString());

            for (TermMention mention : termMentions) {
                String sign = mention.getMetadata().getSign().toLowerCase();
                String rowKey = mention.getRowKey().toString();

                GraphVertex termVertex = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, user);

                boolean newVertex = false;
                if (termVertex == null) {
                    newVertex = true;
                    termVertex = new InMemoryGraphVertex();
                }
                termVertex.setProperty(TITLE, sign);
                termVertex.setProperty(ROW_KEY, rowKey);
                termVertex.setProperty(TYPE, VertexType.ENTITY.toString());
                termVertex.setProperty(SUBTYPE, conceptId);
                graphRepository.save(termVertex, getUser());
                String termId = termVertex.getId();

                if (newVertex) {
                    auditRepository.audit(tweetVertexId, auditRepository.resolvedEntityAuditMessageForArtifact(sign), user);
                    auditRepository.audit(termId, auditRepository.resolvedEntityAuditMessage(tweetVertexTitle), user);
                }
                auditRepository.audit(termId, auditRepository.vertexPropertyAuditMessages(termVertex, modifiedProps), user);

                mention.getMetadata().setGraphVertexId(termId);
                termMentionRepository.save(mention, user.getModelUserContext());

                graphRepository.saveRelationship(tweetVertexId, termId, relationshipLabel, user);
                auditRepository.audit(tweetVertexId, auditRepository.relationshipAuditMessageOnSource(relationshipDisplayName, sign, ""), user);
                auditRepository.audit(termId, auditRepository.relationshipAuditMessageOnDest(relationshipDisplayName, tweetText, ""), user);
            }
        }
    }

    /**
     * Get the name of the Concept representing the entities created by this bolt.
     *
     * @return the name of the Concept of the entities created by this bolt.
     */
    protected abstract String getConceptName();

    /**
     * Get the regular expression used to match the terms that will be
     * converted to entities.
     *
     * @return the term regex
     */
    protected abstract String getTermRegex();

    /**
     * Get the label for the relationships created by this bolt.
     *
     * @return the relationship label
     */
    protected abstract String getRelationshipLabel();
}
