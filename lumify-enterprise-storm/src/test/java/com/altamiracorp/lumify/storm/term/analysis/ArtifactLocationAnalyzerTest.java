package com.altamiracorp.lumify.storm.term.analysis;

import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.core.user.User;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactLocationAnalyzerTest {
    private ArtifactLocationAnalyzer analyzer;
    @Mock
    GraphRepository graphRepository;
    @Mock
    AuditRepository auditRepository;
    @Mock
    User user;
    @Mock
    GraphVertex graphVertex;
    @Mock
    TermMentionMetadata mockMetadata;
    @Mock
    TermMentionModel mention;

    List<TermMentionModel> mentions;

    @Before
    public void setup() {
        mentions = new ArrayList<TermMentionModel>();
        analyzer = new ArtifactLocationAnalyzer(graphRepository, auditRepository);
    }

    @Test
    public void testAnalyzeLocation() {
        TermMentionMetadata metadata = new TermMentionMetadata();
        metadata.setGeoLocation(9.0, 77.0);
        metadata.setGeoLocationPopulation(90L);
        when(mention.getMetadata()).thenReturn(metadata);

        TermMentionMetadata metadataWithTitle = new TermMentionMetadata();
        metadataWithTitle.setGeoLocation(33.0, 77.0);
        metadataWithTitle.setGeoLocationPopulation(100L);
        metadataWithTitle.setGeoLocationTitle("Test");
        TermMentionModel mentionWithTitle = mock(TermMentionModel.class);
        when(mentionWithTitle.getMetadata()).thenReturn(metadataWithTitle);

        TermMentionMetadata metadataWithLowPop = new TermMentionMetadata();
        metadataWithLowPop.setGeoLocation(22.0, 32.0);
        metadataWithLowPop.setGeoLocationPopulation(80L);
        TermMentionModel mentionWithLowPop = mock(TermMentionModel.class);
        when(mentionWithLowPop.getMetadata()).thenReturn(metadataWithLowPop);

        TermMentionMetadata emptyMetadata = new TermMentionMetadata();
        TermMentionModel emptyMention = mock(TermMentionModel.class);
        when(emptyMention.getMetadata()).thenReturn(emptyMetadata);

        mentions.add(mention);
        mentions.add(mentionWithTitle);
        mentions.add(mentionWithLowPop);
        mentions.add(emptyMention);

        ArgumentCaptor<Geoshape> arg = ArgumentCaptor.forClass(Geoshape.class);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphRepository, times(1)).saveVertex(graphVertex, user);
        verify(graphVertex, times(2)).setProperty(any(PropertyName.class), arg.capture());
        assertEquals(33, arg.getValue().getPoint().getLatitude(), 0);
        assertEquals(77, arg.getValue().getPoint().getLongitude(), 0);
    }

    @Test
    public void testAnalyzeWithNoGeoLocations() {
        TermMentionMetadata metadata = new TermMentionMetadata();
        when(mention.getMetadata()).thenReturn(metadata);

        mentions.add(mention);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
        verify(graphRepository, times(0)).saveVertex(graphVertex, user);
    }

    @Test
    public void testAnalyzeWithNoTitle() {
        TermMentionMetadata metadata = new TermMentionMetadata();
        metadata.setGeoLocation(9.0, 77.0);
        metadata.setGeoLocationPopulation(90L);
        when(mention.getMetadata()).thenReturn(metadata);

        mentions.add(mention);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphRepository, times(1)).saveVertex(graphVertex, user);
        verify(graphVertex, times(1)).setProperty(any(PropertyName.class), anyString());
    }

    @Test
    public void testAnalyzeWithNullLong() {
        when(mockMetadata.getGeoLocation()).thenReturn("33,77");
        when(mockMetadata.getLongitude()).thenReturn(null);
        when(mockMetadata.getLatitude()).thenReturn(33.0);
        when(mention.getMetadata()).thenReturn(mockMetadata);

        mentions.add(mention);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphRepository, times(0)).saveVertex(graphVertex, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
    }

    @Test
    public void testAnalyzeWithNullLat() {
        when(mockMetadata.getGeoLocation()).thenReturn("33,77");
        when(mockMetadata.getLongitude()).thenReturn(77.0);
        when(mockMetadata.getLatitude()).thenReturn(null);
        when(mention.getMetadata()).thenReturn(mockMetadata);

        mentions.add(mention);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphRepository, times(0)).saveVertex(graphVertex, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
    }

    @Test
    public void testAnalyzeWithNullTermMentionMetadata() {
        when(mention.getMetadata()).thenReturn(null);
        mentions.add(mention);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphRepository, times(0)).saveVertex(graphVertex, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
    }


    //todo can termMetaData ever be null in updateGraphVertex? - either add test for this or remove case
}
