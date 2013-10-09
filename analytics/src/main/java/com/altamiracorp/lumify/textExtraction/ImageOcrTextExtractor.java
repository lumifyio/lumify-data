package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.google.inject.Inject;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class ImageOcrTextExtractor {
    private static final String NAME = "imageOCRExtractor";
    private static final List<String> ICON_MIME_TYPES = Arrays.asList(new String[]{"image/x-icon", "image/vnd.microsoft.icon"});
    private ArtifactRepository artifactRepository;
    private VideoFrameRepository videoFrameRepository;
    private Tesseract tesseract;

    @Inject
    public ImageOcrTextExtractor(ArtifactRepository artifactRepository, VideoFrameRepository videoFrameRepository) {
        this.artifactRepository = artifactRepository;
        this.videoFrameRepository = videoFrameRepository;
        tesseract = Tesseract.getInstance();
    }

    public ArtifactExtractedInfo extractFromImage(BufferedImage image) throws Exception {
//        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        if (isIcon(artifact)) {
//            return null;
//       }
        String ocrResults = extractTextFromImage(image);
        if (ocrResults == null) {
            return null;
        }
        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setText(ocrResults);
        return extractedInfo;
    }

    public VideoFrameExtractedInfo extractFromVideoFrame(BufferedImage videoFrame) throws Exception {
        ArtifactExtractedInfo info = extractFromImage(videoFrame);
        if (info == null) {
            return null;
        }
        VideoFrameExtractedInfo extractedInfo = new VideoFrameExtractedInfo();
        extractedInfo.setText(info.getText());
        return extractedInfo;
    }

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

    private boolean isIcon(Artifact artifact) {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        return ICON_MIME_TYPES.contains(artifact.getGenericMetadata().getMimeType());
    }
}
