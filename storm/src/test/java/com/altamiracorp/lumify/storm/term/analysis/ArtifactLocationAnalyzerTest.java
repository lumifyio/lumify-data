package com.altamiracorp.lumify.storm.term.analysis;

import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
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
    GraphRepository repository;
    @Mock
    User user;
    @Mock
    GraphVertex graphVertex;
    @Mock
    TermMention mention1, mention2, mention3, mention4, mention5;
    TermMentionMetadata metadata1, metadata2, metadata3, metadata4, metadata5;
    List<TermMention> mentions;

    @Before
    public void setup() {
        metadata1 = new TermMentionMetadata();
        metadata1.setGeoLocation(9.0, 77.0);
        metadata1.setGeoLocationPopulation(90L);
        when(mention1.getMetadata()).thenReturn(metadata1);

        metadata2 = new TermMentionMetadata();
        metadata2.setGeoLocation(33.0, 77.0);
        metadata2.setGeoLocationPopulation(100L);
        metadata2.setGeoLocationTitle("Test");
        when(mention2.getMetadata()).thenReturn(metadata2);

        metadata3 = new TermMentionMetadata();
        metadata3.setGeoLocation(22.0, 32.0);
        metadata3.setGeoLocationPopulation(80L);
        when(mention3.getMetadata()).thenReturn(metadata3);

        metadata4 = new TermMentionMetadata();
        when(mention4.getMetadata()).thenReturn(metadata4);

        metadata5 = mock(TermMentionMetadata.class);
        when(metadata5.getGeoLocation()).thenReturn("33,77");
        when(metadata5.getLongitude()).thenReturn(null);
        when(metadata5.getLatitude()).thenReturn(null);
        when(mention5.getMetadata()).thenReturn(metadata5);

        mentions = new ArrayList<TermMention>();
        analyzer = new ArtifactLocationAnalyzer(repository);
    }

    @Test
    public void testAnalyzeLocation() {
        mentions.add(mention1);
        mentions.add(mention2);
        mentions.add(mention3);
        mentions.add(mention4);

        ArgumentCaptor<Geoshape> arg = ArgumentCaptor.forClass(Geoshape.class);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(repository, times(1)).saveVertex(graphVertex, user);
        verify(graphVertex, times(2)).setProperty(any(PropertyName.class), arg.capture());
        assertEquals(33, arg.getValue().getPoint().getLatitude(), 0);
        assertEquals(77, arg.getValue().getPoint().getLongitude(), 0);
    }

    @Test
    public void testAnalyzeWithNoGeoLocations() {
        mentions.add(mention4);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
        verify(repository, times(0)).saveVertex(graphVertex, user);
    }

    @Test
    public void testAnalyzeWithNoTitle() {
        mentions.add(mention1);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(repository, times(1)).saveVertex(graphVertex, user);
        verify(graphVertex, times(1)).setProperty(any(PropertyName.class), anyString());
    }

    @Test
    public void testAnalyzeWithInvalidGeoPoint() {
        mentions.add(mention5);
        analyzer.analyzeLocation(graphVertex, mentions, user);
        verify(repository, times(0)).saveVertex(graphVertex, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
    }

    @Test
    public void testWithNullVertex() {
        mentions.add(mention1);
        analyzer.analyzeLocation(null, mentions, user);
        verify(repository, times(0)).saveVertex(graphVertex, user);
        verify(graphVertex, times(0)).setProperty(any(PropertyName.class), anyString());
    }

    //todo can termMetaData ever be null in updateGraphVertex? - either add test for this or remove case
}
