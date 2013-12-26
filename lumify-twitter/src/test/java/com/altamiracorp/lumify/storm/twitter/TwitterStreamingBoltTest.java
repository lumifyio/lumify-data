package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TwitterStreamingBoltTest {
    @Mock
    private BaseLumifyBolt baseLumifyBolt;
    @Mock
    private Tuple tuple;
    @Mock
    private AuditRepository auditRepository;
    @Mock
    private ModelUserContext modelUserContext;
    @Mock
    private SystemUser systemUser;
    @Mock
    private Concept handleConcept;
    @Mock
    private Concept hashtagConcept;
    @Mock
    private Concept urlConcept;
    @Mock
    private OntologyRepository ontologyRepository;
    @Mock
    private GraphVertex tweet;
    @Mock
    private GraphVertex handleConceptVertex;
    @Mock
    private GraphVertex hashtagConceptVertex;
    @Mock
    private GraphVertex urlConceptVertex;
    @Mock
    private ArtifactRepository artifactRepository;
    @Mock
    private GraphRepository graphRepository;
    @Mock
    private SearchProvider searchProvider;
    @Mock
    private TermMentionRepository termMentionRepository;
    @Mock
    private WorkQueueRepository workQueueRepository;

    private TwitterStreamingBolt twitterStreamingBolt;
    JSONObject userJson = new JSONObject();
    JSONObject tupleJson = new JSONObject();


    @Before
    public void setup() throws Exception {
        twitterStreamingBolt = new TwitterStreamingBolt();
        twitterStreamingBolt.setOntologyRepository(ontologyRepository);
        twitterStreamingBolt.setArtifactRepository(artifactRepository);
        twitterStreamingBolt.setGraphRepository(graphRepository);
        twitterStreamingBolt.setAuditRepository(auditRepository);
        twitterStreamingBolt.setUser(systemUser);
        twitterStreamingBolt.setSearchProvider(searchProvider);
        twitterStreamingBolt.setTermMentionRepository(termMentionRepository);
        twitterStreamingBolt.setWorkQueueRepository(workQueueRepository);
        userJson.put("screen_name", "jota_2705");
        userJson.put("name", "El grinder se vac\\u00edaa");
        userJson.put("statuses_count", new Integer("3977"));
        userJson.put("follwers_count", new Integer("362"));
        userJson.put("friends_count", new Integer("480"));
        userJson.put("created_at", "Fri May 18 14:48:35 +0000 2012");
        userJson.put("description", "Ya desde peque\\u00f1o supe dar ah\\u00ed donde hac\\u00eda da\\u00f1o.Y no me refiero al tabaco,cateto, yo respeto al que consume y consumo con sumo respeto. R.A.P para to'desde el 99");
        userJson.put("profile_image_url", "http://pbs.twimg.com/profile_images/412310266856996864/955IBes8_normal.jpeg");

        tupleJson.put("created_at", "Thu Dec 19 22:07:04 +0000 2013");
        tupleJson.put("text", "I'm at Target (2300 W. Ben White Blvd., S. Lamar Blvd., Austin) w\\/ 3 others http://t.co/eGSHZkXH #shopping");
        tupleJson.put("user", userJson);
    }

    @Test
    public void testSafeExecute() throws Exception {
        when(tuple.getString(0)).thenReturn(tupleJson.toString());
        when(systemUser.getModelUserContext()).thenReturn(modelUserContext);
        when(ontologyRepository.getConceptByName(TwitterStreamingBolt.TWITTER_HANDLE, systemUser)).thenReturn(handleConcept);
        when(ontologyRepository.getConceptByName(TwitterStreamingBolt.URL_CONCEPT, systemUser)).thenReturn(urlConcept);
        when(ontologyRepository.getConceptByName(TwitterStreamingBolt.HASHTAG_CONCEPT, systemUser)).thenReturn(hashtagConcept);

        when(artifactRepository.findByRowKey(anyString(), any(ModelUserContext.class))).thenReturn(null);
        when(artifactRepository.saveToGraph(any(Artifact.class), any(ArtifactExtractedInfo.class), any(User.class))).thenReturn(tweet);
        when(graphRepository.save(tweet, systemUser)).thenReturn("");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                tweet = (GraphVertex) invocationOnMock.getArguments()[0];
                return null;
            }
        }).when(searchProvider).add(any(GraphVertex.class), any(InputStream.class));

        when(graphRepository.findVertex(handleConcept.getId(), systemUser)).thenReturn(handleConceptVertex);
        when(graphRepository.findVertex(urlConcept.getId(), systemUser)).thenReturn(urlConceptVertex);
        when(graphRepository.findVertex(hashtagConcept.getId(), systemUser)).thenReturn(hashtagConceptVertex);
        when(tweet.getId()).thenReturn("");
        when(handleConceptVertex.getId()).thenReturn("");
        when(urlConceptVertex.getId()).thenReturn("");
        when(hashtagConceptVertex.getId()).thenReturn("");
        when(handleConceptVertex.getProperty(PropertyName.DISPLAY_NAME)).thenReturn("");
        when(urlConceptVertex.getProperty(PropertyName.DISPLAY_NAME)).thenReturn("");
        when(hashtagConceptVertex.getProperty(PropertyName.DISPLAY_NAME)).thenReturn("");

        GraphVertex shopping = mock(GraphVertex.class);
        when(graphRepository.findVertexByTitleAndType("shopping", VertexType.ENTITY, systemUser)).thenReturn(shopping);
        when(shopping.getId()).thenReturn("");

        GraphVertex url = mock(GraphVertex.class);
        when(graphRepository.findVertexByTitleAndType("http://t.co/egshzkxh", VertexType.ENTITY, systemUser)).thenReturn(url);
        when(url.getId()).thenReturn("");

        twitterStreamingBolt.safeExecute(tuple);

        verify(artifactRepository, times(2)).findByRowKey(anyString(), any(ModelUserContext.class));
        verify(artifactRepository, times(2)).save(any(Artifact.class), any(ModelUserContext.class));
        verify(graphRepository, times(6)).save(any(GraphVertex.class), any(User.class));
        verify(graphRepository, times(3)).saveRelationship(anyString(), anyString(), anyString(), any(User.class));
    }
}
