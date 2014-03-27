package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.mapping.DocumentMapping;
import com.altamiracorp.lumify.mapping.MappingFileImportSupportingFileHandler;
import com.altamiracorp.securegraph.Text;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.ROW_KEY;
import static com.altamiracorp.lumify.core.model.properties.RawLumifyProperties.TEXT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DocumentMappingEntityExtractor.class})
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
        when(vertex.getId()).thenReturn(TEST_VERTEX_ID);
        when(vertex.getPropertyValue(ROW_KEY.getKey())).thenReturn(TEST_ROW_KEY);
        when(vertex.getPropertyValue(MappingFileImportSupportingFileHandler.MAPPING_JSON.getKey())).thenReturn(new Text(TEST_JSON_MAPPING));
        when(jsonMapper.readValue(TEST_JSON_MAPPING, DocumentMapping.class)).thenReturn(docMapping);

        extractor = new DocumentMappingEntityExtractor();
        extractor.setJsonMapper(jsonMapper);
    }

    @Test
    public void testExtract() throws Exception {
        when(vertex.getPropertyValue(TEXT.getKey())).thenReturn(streamingValue);
        when(streamingValue.getInputStream()).thenReturn(valueStream);
        PowerMockito.whenNew(InputStreamReader.class).withArguments(valueStream).thenReturn(streamingReader);
        when(docMapping.mapDocument(eq(streamingReader), anyString())).thenReturn(expectedResult);

        TermExtractionResult result = extractor.extract(vertex, user);
        assertEquals(expectedResult, result);
    }
}
