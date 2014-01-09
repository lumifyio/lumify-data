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
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
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
    private static final String PROCESS = BaseTwitterEntityCreationBolt.class.getName();

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
            String conceptId = concept.getId().toString();
            String termRegex = getTermRegex();
            String relationshipLabel = getRelationshipLabel();
            String relationshipDisplayName = ontologyRepository.getDisplayNameForLabel(relationshipLabel, user);

            GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
            GraphVertex tweetVertex = graphRepository.findVertex(tweetVertexId, user);
            List<TermMention> termMentions = TermRegexFinder.find(tweetVertexId, conceptVertex, tweetText, termRegex);
            List<String> modifiedProps = Lists.newArrayList(TITLE.toString(), ROW_KEY.toString(), CONCEPT_TYPE.toString());

            for (TermMention mention : termMentions) {
                String sign = mention.getMetadata().getSign().toLowerCase();
                String rowKey = mention.getRowKey().toString();

                GraphVertex termVertex = graphRepository.findVertexByExactTitle(sign, user);

                boolean newVertex = false;
                if (termVertex == null) {
                    newVertex = true;
                    termVertex = new InMemoryGraphVertex();
                }
                termVertex.setProperty(TITLE, sign);
                termVertex.setProperty(ROW_KEY, rowKey);
                termVertex.setProperty(CONCEPT_TYPE, conceptId);
                graphRepository.save(termVertex, getUser());
                String termId = termVertex.getId();

                if (newVertex) {
                    auditRepository.auditEntity(AuditAction.CREATE.toString(), termVertex.getId(), tweetVertexId, sign, conceptId, PROCESS, "", getUser());
                }
                for (String modifiedProperty : modifiedProps) {
                    auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), termVertex, modifiedProperty, PROCESS, "", getUser());
                }

                mention.getMetadata().setGraphVertexId(termId);
                termMentionRepository.save(mention, user.getModelUserContext());

                graphRepository.saveRelationship(tweetVertexId, termId, relationshipLabel, user);
                auditRepository.auditRelationships(AuditAction.CREATE.toString(), tweetVertex, termVertex, relationshipDisplayName, PROCESS, "", getUser());
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
