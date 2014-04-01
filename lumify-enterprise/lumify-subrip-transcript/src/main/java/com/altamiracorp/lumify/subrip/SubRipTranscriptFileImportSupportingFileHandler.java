package com.altamiracorp.lumify.subrip;

import com.altamiracorp.lumify.core.ingest.FileImportSupportingFileHandler;
import com.altamiracorp.lumify.core.model.properties.StreamingLumifyProperty;
import com.altamiracorp.securegraph.VertexBuilder;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SubRipTranscriptFileImportSupportingFileHandler extends FileImportSupportingFileHandler {
    public static final String SUBRIP_CC_FILE_NAME_SUFFIX = ".srt";
    public static final StreamingLumifyProperty SUBRIP_CC = new StreamingLumifyProperty("http://lumify.io#subrip");

    @Override
    public boolean isSupportingFile(File f) {
        return f.getName().endsWith(SUBRIP_CC_FILE_NAME_SUFFIX);
    }

    @Override
    public AddSupportingFilesResult addSupportingFiles(VertexBuilder vertexBuilder, File f, Visibility visibility) throws Exception {
        File mappingJsonFile = new File(f.getParentFile(), f.getName() + SUBRIP_CC_FILE_NAME_SUFFIX);
        if (mappingJsonFile.exists()) {
            final FileInputStream subripIn = new FileInputStream(mappingJsonFile);
            StreamingPropertyValue subripValue = new StreamingPropertyValue(subripIn, byte[].class);
            subripValue.searchIndex(false);
            SUBRIP_CC.setProperty(vertexBuilder, subripValue, visibility);
            return new AddSupportingFilesResult() {
                @Override
                public void close() throws IOException {
                    subripIn.close();
                }
            };
        }
        return null;
    }
}
