package com.altamiracorp.lumify.mapping;

import com.altamiracorp.lumify.core.ingest.FileImportSupportingFileHandler;
import com.altamiracorp.lumify.core.model.properties.StreamingLumifyProperty;
import com.altamiracorp.securegraph.VertexBuilder;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MappingFileImportSupportingFileHandler extends FileImportSupportingFileHandler {
    private static final String MAPPING_JSON_FILE_NAME_SUFFIX = ".mapping.json";
    public static final StreamingLumifyProperty MAPPING_JSON = new StreamingLumifyProperty("http://lumify.io#mappingJson");

    @Override
    public boolean isSupportingFile(File f) {
        return f.getName().endsWith(MAPPING_JSON_FILE_NAME_SUFFIX);
    }

    @Override
    public AddSupportingFilesResult addSupportingFiles(VertexBuilder vertexBuilder, File f, Visibility visibility) throws FileNotFoundException {
        File mappingJsonFile = new File(f.getParentFile(), f.getName() + MAPPING_JSON_FILE_NAME_SUFFIX);
        if (mappingJsonFile.exists()) {
            final FileInputStream mappingJsonInputStream = new FileInputStream(mappingJsonFile);
            StreamingPropertyValue mappingJsonValue = new StreamingPropertyValue(mappingJsonInputStream, byte[].class);
            mappingJsonValue.searchIndex(false);
            MAPPING_JSON.setProperty(vertexBuilder, mappingJsonValue, visibility);
            return new AddSupportingFilesResult() {
                @Override
                public void close() throws IOException {
                    mappingJsonInputStream.close();
                }
            };
        }
        return null;
    }
}
