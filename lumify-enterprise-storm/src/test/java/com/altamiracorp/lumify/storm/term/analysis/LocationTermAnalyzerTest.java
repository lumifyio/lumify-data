package com.altamiracorp.lumify.storm.term.analysis;

import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.geoNames.GeoNamePostalCodeRepository;
import com.altamiracorp.lumify.core.model.geoNames.GeoNameRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionMetadata;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.location.SimpleTermLocationExtractor;
import com.altamiracorp.lumify.storm.term.extraction.TermMentionWithGraphVertex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationTermAnalyzerTest {
    private LocationTermAnalyzer analyzer;
    @Mock
    TermMentionMetadata mockMetadata;
    @Mock
    private AuditRepository auditRepository;
    @Mock
    private GraphRepository graphRepository;
    @Mock
    private TermMentionRepository termMentionRepository;
    @Mock
    private GeoNameRepository geoNameRepository;
    @Mock
    private GeoNamePostalCodeRepository postalCodeRepository;
    @Mock
    private SimpleTermLocationExtractor extractor;
    @Mock
    private User user;
    @Mock
    private TermMentionModel termMention;
    @Mock
    private GraphVertex vertex;
    private TermMentionMetadata metadata;
    private TermMentionWithGraphVertex data;


    @Before
    public void setup() {
        metadata = new TermMentionMetadata();
        when(termMention.getRowKey()).thenReturn(new TermMentionRowKey("testRowKey"));
        data = new TermMentionWithGraphVertex(termMention, vertex);
    }

    @Test
    public void testAnalyze() {
        data = new TermMentionWithGraphVertex(termMention, vertex);
        metadata.setSign("testSign");
        metadata.setGeoLocation(20.0, 10.0);
        when(termMention.getMetadata()).thenReturn(metadata);
        when(extractor.getTermWithLocationLookup(geoNameRepository, termMention, user)).thenReturn(termMention);
        analyzer = new LocationTermAnalyzer(extractor, postalCodeRepository, geoNameRepository, graphRepository, auditRepository, termMentionRepository);
        analyzer.analyzeTermData(data, user);
        verify(termMention, times(3)).getMetadata();
        verify(extractor, times(1)).getTermWithLocationLookup(geoNameRepository, termMention, user);
        verify(graphRepository, times(1)).saveVertex(vertex, user);
    }

    @Test
    public void testAnalyzeWithZipCode() {
        data = new TermMentionWithGraphVertex(termMention, vertex);
        metadata.setSign("20171-1111");
        metadata.setGeoLocation(20.0, 10.0);
        when(termMention.getMetadata()).thenReturn(metadata);
        when(extractor.isPostalCode(termMention)).thenReturn(true);
        when(extractor.getTermWithPostalCodeLookup(postalCodeRepository, termMention, user)).thenReturn(termMention);
        analyzer = new LocationTermAnalyzer(extractor, postalCodeRepository, geoNameRepository, graphRepository, auditRepository, termMentionRepository);
        analyzer.analyzeTermData(data, user);
        verify(termMention, times(3)).getMetadata();
        verify(extractor, times(1)).isPostalCode(termMention);
        verify(extractor, times(1)).getTermWithPostalCodeLookup(postalCodeRepository, termMention, user);
        verify(graphRepository, times(1)).saveVertex(vertex, user);
    }

    @Test
    public void testAnalyzeWithNullLongitude() {
        data = new TermMentionWithGraphVertex(termMention, vertex);
        when(mockMetadata.getLatitude()).thenReturn(10.0);
        when(mockMetadata.getLongitude()).thenReturn(null);
        when(mockMetadata.getSign()).thenReturn("testSign");
        when(termMention.getMetadata()).thenReturn(mockMetadata);
        when(extractor.getTermWithLocationLookup(geoNameRepository, termMention, user)).thenReturn(termMention);
        analyzer = new LocationTermAnalyzer(extractor, postalCodeRepository, geoNameRepository, graphRepository, auditRepository, termMentionRepository);
        analyzer.analyzeTermData(data, user);
        verify(mockMetadata, times(1)).getLatitude();
        verify(mockMetadata, times(1)).getLongitude();
        verify(mockMetadata, times(1)).getSign();
        verify(extractor, times(1)).getTermWithLocationLookup(geoNameRepository, termMention, user);
        verify(termMention, times(3)).getMetadata();
        verify(graphRepository, times(0)).saveVertex(vertex, user);
    }

    @Test
    public void testAnalyzeWithNullLatitude() {
        data = new TermMentionWithGraphVertex(termMention, vertex);
        when(mockMetadata.getLatitude()).thenReturn(null);
        when(mockMetadata.getLongitude()).thenReturn(10.0);
        when(mockMetadata.getSign()).thenReturn("testSign");
        when(termMention.getMetadata()).thenReturn(mockMetadata);
        when(extractor.getTermWithLocationLookup(geoNameRepository, termMention, user)).thenReturn(termMention);
        analyzer = new LocationTermAnalyzer(extractor, postalCodeRepository, geoNameRepository, graphRepository, auditRepository, termMentionRepository);
        analyzer.analyzeTermData(data, user);
        verify(mockMetadata, times(1)).getLatitude();
        verify(mockMetadata, times(1)).getLongitude();
        verify(mockMetadata, times(1)).getSign();
        verify(extractor, times(1)).getTermWithLocationLookup(geoNameRepository, termMention, user);
        verify(termMention, times(3)).getMetadata();
        verify(graphRepository, times(0)).saveVertex(vertex, user);
    }

    @Test
    public void testAnalyzeWithNoUpdate() {
        data = new TermMentionWithGraphVertex(termMention, vertex);
        metadata.setSign("testSign");
        when(termMention.getMetadata()).thenReturn(metadata);
        analyzer = new LocationTermAnalyzer(extractor, postalCodeRepository, geoNameRepository, graphRepository, auditRepository, termMentionRepository);
        assertNull(analyzer.analyzeTermData(data, user));
        verify(graphRepository, times(0)).saveVertex(vertex, user);
        verify(termMention, times(2)).getMetadata();
    }

    @Test
    public void testAnalyzeWithNullVertex() {
        data = new TermMentionWithGraphVertex(termMention, null);
        metadata.setSign("testSign");
        when(termMention.getMetadata()).thenReturn(metadata);
        when(extractor.getTermWithLocationLookup(geoNameRepository, termMention, user)).thenReturn(termMention);
        analyzer = new LocationTermAnalyzer(extractor, postalCodeRepository, geoNameRepository, graphRepository,auditRepository, termMentionRepository);
        analyzer.analyzeTermData(data, user);
        verify(termMention, times(3)).getMetadata();
        verify(extractor, times(1)).getTermWithLocationLookup(geoNameRepository, termMention, user);
        verify(graphRepository, times(0)).saveVertex(vertex, user);
    }

}
