package com.altamiracorp.lumify.web.routes.map;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.resources.Resource;
import com.altamiracorp.lumify.model.resources.ResourceRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MapMarkerImage extends BaseRequestHandler {
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private ResourceRepository resourceRepository = new ResourceRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        String typeStr = getAttributeString(request, "type");
        long scale = getOptionalParameterLong(request, "scale", 1L);

        String glyphIconRowKey = getGlyphIcon(session, typeStr);
        if (glyphIconRowKey == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Resource resource = resourceRepository.findByRowKey(session.getModelSession(), glyphIconRowKey);
        if (resource == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        byte[] imageData = getMarkerImage(resource, scale);
        ServletOutputStream out = response.getOutputStream();
        out.write(imageData);
        out.close();
    }

    private byte[] getMarkerImage(Resource resource, long scale) throws IOException {
        BufferedImage resourceImage = resource.getContent().getDataImage();
        if (resourceImage == null) {
            return null;
        }

        BufferedImage backgroundImage = getBackgroundImage(scale);
        if (backgroundImage == null) {
            return null;
        }

        BufferedImage image = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), backgroundImage.getType());

        Graphics2D g = image.createGraphics();
        g.drawImage(backgroundImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), null);
        int[] resourceImageDim = new int[]{resourceImage.getWidth(), resourceImage.getHeight()};
        int size = image.getWidth() * 2 / 3;
        int[] boundary = new int[]{size, size};
        int[] scaledDims = ArtifactThumbnailRepository.getScaledDimension(resourceImageDim, boundary);
        int x = (backgroundImage.getWidth() - scaledDims[0]) / 2;
        int y = (backgroundImage.getWidth() - scaledDims[1]) / 2;
        g.drawImage(resourceImage, x, y, scaledDims[0], scaledDims[1], null);
        g.dispose();

        return imageToBytes(image);
    }

    private BufferedImage getBackgroundImage(long scale) throws IOException {
        InputStream res;
        if (scale == 1) {
            res = this.getClass().getResourceAsStream("marker-background.png");
        } else if (scale == 2) {
            res = this.getClass().getResourceAsStream("marker-background-2x.png");
        } else {
            return null;
        }
        return ImageIO.read(res);
    }

    private byte[] imageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream imageData = new ByteArrayOutputStream();
        ImageIO.write(image, "png", imageData);
        imageData.close();
        return imageData.toByteArray();
    }

    private String getGlyphIcon(AppSession session, String typeStr) {
        Concept concept = ontologyRepository.getConceptById(session.getGraphSession(), typeStr);
        if (concept == null) {
            concept = ontologyRepository.getConceptByName(session.getGraphSession(), typeStr);
        }

        while (concept != null) {
            String glyphIcon = (String) concept.getProperty(PropertyName.GLYPH_ICON);
            if (glyphIcon != null) {
                return glyphIcon;
            }

            concept = ontologyRepository.getParentConcept(session.getGraphSession(), concept.getId());
        }

        return null;
    }
}
