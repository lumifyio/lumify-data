package com.altamiracorp.lumify.storm.term.extraction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.mapping.DocumentMapping;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ DocumentMappingEntityExtractor.class })
public class DocumentMappingEntityExtractorTest {
    private static final String TEST_VERTEX_ID = "testVertexId";
    private static final String TEST_ROW_KEY = "testRowKey";
    private static final String TEST_JSON_MAPPING = "testJsonMapping";
    private static final String TEST_TEXT = "testText";

    @Mock
    private ObjectMapper jsonMapper;
    @Mock
    private Vertex vertex;
    @Mock
    private User user;
    @Mock
    private DocumentMapping docMapping;
    @Mock
    private StreamingPropertyValue streamingValue;
    @Mock
    private InputStream valueStream;
    @Mock
    private InputStreamReader streamingReader;
    @Mock
    private StringReader textReader;
    @Mock
    private TermExtractionResult expectedResult;

    private DocumentMappingEntityExtractor extractor;

    @Before
    public void setup() throws Exception {
        Whitebox.setInternalState(DocumentMappingEntityExtractor.class, ObjectMapper.class, jsonMapper);
        when(vertex.getId()).thenReturn(TEST_VERTEX_ID);
        when(vertex.getPropertyValue(PropertyName.ROW_KEY.toString())).thenReturn(TEST_ROW_KEY);
        when(vertex.getPropertyValue(PropertyName.MAPPING_JSON.toString())).thenReturn(TEST_JSON_MAPPING);
        when(jsonMapper.readValue(TEST_JSON_MAPPING, DocumentMapping.class)).thenReturn(docMapping);

        extractor = new DocumentMappingEntityExtractor();
    }

    @Test
    public void testExtract_Streaming() throws Exception {
        when(vertex.getPropertyValue(PropertyName.TEXT.toString())).thenReturn(streamingValue);
        when(streamingValue.getInputStream()).thenReturn(valueStream);
        PowerMockito.whenNew(InputStreamReader.class).withArguments(valueStream).thenReturn(streamingReader);
        when(docMapping.mapDocument(eq(streamingReader), anyString())).thenReturn(expectedResult);

        TermExtractionResult result = extractor.extract(vertex, user);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testExtract_StringValue() throws Exception {
        when(vertex.getPropertyValue(PropertyName.TEXT.toString())).thenReturn(TEST_TEXT);
        PowerMockito.whenNew(StringReader.class).withArguments(TEST_TEXT).thenReturn(textReader);
        when(docMapping.mapDocument(eq(textReader), anyString())).thenReturn(expectedResult);

        TermExtractionResult result = extractor.extract(vertex, user);
        assertEquals(expectedResult, result);
    }
}
