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

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.altamiracorp.lumify.twitter.TwitterConstants.*;
import static org.mockito.Mockito.*;

/**
 * Abstract base class for testing Twitter bolts.
 * @param <B> the type of bolt being tested
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTwitterBoltTest<B extends BaseLumifyBolt> {
    @Mock
    protected Tuple tuple;
    @Mock
    protected AuditRepository auditRepository;
    @Mock
    protected SystemUser systemUser;
    @Mock
    protected OntologyRepository ontologyRepository;
    @Mock
    protected ArtifactRepository artifactRepository;
    @Mock
    protected GraphRepository graphRepository;
    @Mock
    protected TermMentionRepository termMentionRepository;
    @Mock
    protected WorkQueueRepository workQueueRepository;
    @Mock
    protected OutputCollector outputCollector;
    @Mock
    protected ModelUserContext modelUserContext;
    @Mock
    protected Concept handleConcept;
    @Mock
    protected Concept hashtagConcept;
    @Mock
    protected Concept urlConcept;
    
    protected B testBolt;
    
    protected abstract B createTestBolt();
    
    @Before
    public void setup() throws Exception {
        testBolt = spy(createTestBolt());
        testBolt.setOntologyRepository(ontologyRepository);
        testBolt.setArtifactRepository(artifactRepository);
        testBolt.setGraphRepository(graphRepository);
        testBolt.setAuditRepository(auditRepository);
        testBolt.setUser(systemUser);
        testBolt.setTermMentionRepository(termMentionRepository);
        testBolt.setWorkQueueRepository(workQueueRepository);
        when(testBolt.getCollector()).thenReturn(outputCollector);
        
        when(systemUser.getModelUserContext()).thenReturn(modelUserContext);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_HANDLE, systemUser)).thenReturn(handleConcept);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_URL, systemUser)).thenReturn(urlConcept);
        when(ontologyRepository.getConceptByName(CONCEPT_TWITTER_HASHTAG, systemUser)).thenReturn(hashtagConcept);
        when(artifactRepository.findByRowKey(anyString(), any(ModelUserContext.class))).thenReturn(null);
    }
}
