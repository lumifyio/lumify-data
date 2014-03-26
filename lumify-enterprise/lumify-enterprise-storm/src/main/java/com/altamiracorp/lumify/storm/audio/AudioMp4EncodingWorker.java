package com.altamiracorp.lumify.storm.audio;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.audio.AudioTextExtractionWorker;
import com.altamiracorp.lumify.core.model.ontology.ConceptType;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ProcessRunner;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.InputStream;
import java.util.Random;

public class AudioMp4EncodingWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements AudioTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(AudioMp4EncodingWorker.class);
    private static final Random random = new Random();
    private ProcessRunner processRunner;

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.info("Encoding (mp4) [AudioMp4EncodingWorker] %s", data.getFileName());
        File mp4File = File.createTempFile("encode_mp4_", ".mp4");
        try {
            processRunner.execute(
                    "ffmpeg",
                    new String[]{
                            "-y", // overwrite output files
                            "-i", data.getLocalFileName(),
                            "-acodec", "libfdk_aac",
                            mp4File.getAbsolutePath()
                    },
                    null,
                    data.getFileName() + ": "
            );

            Path copySourcePath = new Path(mp4File.getAbsolutePath());
            Path copyDestPath = new Path("/tmp/audioMp4-" + random.nextInt() + ".mp4");
            data.getHdfsFileSystem().copyFromLocalFile(false, true, copySourcePath, copyDestPath);

            ArtifactExtractedInfo info = new ArtifactExtractedInfo();
            info.setAudioMp4HdfsFilePath(copyDestPath.toString());
            info.setConceptType(ConceptType.AUDIO.toString());
            LOGGER.debug("Finished [AudioMp4EncodingWorker]: %s", data.getFileName());
            return info;
        } finally {
            mp4File.delete();
        }
    }

    @Inject
    public void setProcessRunner(ProcessRunner ffmpeg) {
        this.processRunner = ffmpeg;
    }
}
