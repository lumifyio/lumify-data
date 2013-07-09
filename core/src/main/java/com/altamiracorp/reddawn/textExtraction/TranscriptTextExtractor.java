package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.VideoTranscript;
import org.apache.hadoop.mapreduce.Mapper;

public class TranscriptTextExtractor implements TextExtractor {
    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public ExtractedInfo extract(Session session, Artifact artifact) throws Exception {
        VideoTranscript videoTranscript = artifact.getContent().getVideoTranscript();
        if (videoTranscript == null) {
            return null;
        }

        ExtractedInfo extractedInfo = new ExtractedInfo();
        extractedInfo.setText(videoTranscript.toString());
        return extractedInfo;
    }


}
