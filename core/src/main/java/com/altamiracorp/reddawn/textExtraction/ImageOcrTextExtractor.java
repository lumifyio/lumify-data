package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageHelper;
import org.apache.hadoop.mapreduce.Mapper;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class ImageOcrTextExtractor implements TextExtractor {
    private static final String NAME = "imageOCRExtractor";
    private static final List<String> ICON_MIME_TYPES = Arrays.asList(new String[]{"image/x-icon", "image/vnd.microsoft.icon"});
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();
    private Tesseract tesseract;

    @Override
    public void setup(Mapper.Context context) {
        tesseract = Tesseract.getInstance();
    }

    @Override
    public ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception {
        if (artifact.getType() != ArtifactType.IMAGE) {
            return null;
        }

        if (isIcon(artifact)) {
            return null;
        }

        BufferedImage image = artifactRepository.getRawAsImage(session, artifact);
        if (image == null){
            return null;
        }
        String ocrResults = extractTextFromImage(image);
        if (ocrResults == null) {
            return null;
        }
        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setText(ocrResults);
        return extractedInfo;
    }

    @Override
    public VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception {
        BufferedImage image = videoFrameRepository.loadImage(session, videoFrame);
        String ocrResults = extractTextFromImage(image);
        if (ocrResults == null) {
            return null;
        }
        VideoFrameExtractedInfo extractedInfo = new VideoFrameExtractedInfo();
        extractedInfo.setText(ocrResults);
        return extractedInfo;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private String extractTextFromImage(BufferedImage image) throws TesseractException {
        BufferedImage grayImage = ImageHelper.convertImageToGrayscale(image);
        String ocrResults = tesseract.doOCR(grayImage);
        if (ocrResults == null || ocrResults.trim().length() == 0) {
            return null;
        }
        ocrResults = ocrResults.trim();
        // TODO remove the trash that doesn't seem to be words
        return ocrResults;
    }

    private boolean isIcon (Artifact artifact) {
        return ICON_MIME_TYPES.contains(artifact.getGenericMetadata().getMimeType());
    }
}
