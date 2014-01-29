package com.altamiracorp.lumify.storm.structuredData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.ontology.DisplayType;
import com.altamiracorp.lumify.mapping.DocumentMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DocumentMappingTextExtractorWorker.class })
public class DocumentMappingTextExtractorWorkerTest {
    private static final String HIDDEN_FILE_NAME = ".inputFile" + StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX;
    private static final String INPUT_FILE_NAME = "inputFile.json";
    private static final String MAPPING_FILE_NAME = INPUT_FILE_NAME + StructuredDataContentTypeSorter.MAPPING_JSON_FILE_NAME_SUFFIX;
    private static final String TEST_FILE_CONTENTS = "testContents";
    private static final String TEST_SUBJECT = "testSubject";
    private static final String TEST_JSON_MAPPING = "{ \"mapping\": \"testJson\" }";

    @Mock
    private ObjectMapper jsonMapper;
    @Mock
    private AdditionalArtifactWorkData data;
    @Mock
    private InputStream inputStream;
    @Mock
    private File tempDir;
    @Mock
    private File hiddenFile;
    @Mock
    private File inputFile;
    @Mock
    private File mappingFile;
    @Mock
    private DocumentMapping docMapping;
    @Mock
    private StringWriter stringWriter;

    @Test
    public void testDoWork() throws Exception {
        DocumentMappingTextExtractorWorker worker = new DocumentMappingTextExtractorWorker();
        Whitebox.setInternalState(worker, ObjectMapper.class, jsonMapper);
        when(data.getArchiveTempDir()).thenReturn(tempDir);
        when(data.getFileName()).thenReturn(INPUT_FILE_NAME);
        when(tempDir.isDirectory()).thenReturn(true);
        when(tempDir.listFiles()).thenReturn(new File[] { hiddenFile, inputFile, mappingFile });
        when(hiddenFile.getName()).thenReturn(HIDDEN_FILE_NAME);
        when(inputFile.getName()).thenReturn(INPUT_FILE_NAME);
        when(mappingFile.getName()).thenReturn(MAPPING_FILE_NAME);
        when(jsonMapper.readValue(mappingFile, DocumentMapping.class)).thenReturn(docMapping);
        when(jsonMapper.writeValueAsString(docMapping)).thenReturn(TEST_JSON_MAPPING);
        when(docMapping.getSubject()).thenReturn(TEST_SUBJECT);
        PowerMockito.whenNew(StringWriter.class).withNoArguments().thenReturn(stringWriter);
        when(stringWriter.toString()).thenReturn(TEST_FILE_CONTENTS);

        ArtifactExtractedInfo expected = new ArtifactExtractedInfo()
                .text(TEST_FILE_CONTENTS)
                .title(TEST_SUBJECT)
                .mappingJson(TEST_JSON_MAPPING)
                .conceptType(DisplayType.DOCUMENT.toString());
        ArtifactExtractedInfo results = worker.doWork(inputStream, data);

        assertEquals(expected, results);
    }
}
