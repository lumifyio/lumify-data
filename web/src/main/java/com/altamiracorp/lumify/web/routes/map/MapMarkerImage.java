package com.altamiracorp.lumify.web.routes.map;

import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.resources.Resource;
import com.altamiracorp.lumify.model.resources.ResourceRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MapMarkerImage extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapMarkerImage.class);

    private final OntologyRepository ontologyRepository;
    private final ResourceRepository resourceRepository;
    private Cache<String, byte[]> imageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Inject
    public MapMarkerImage(final OntologyRepository ontologyRepository, ResourceRepository resourceRepository) {
        this.ontologyRepository = ontologyRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        String typeStr = getAttributeString(request, "type");
        long scale = getOptionalParameterLong(request, "scale", 1L);
        int heading = roundHeadingAngle(getOptionalParameterDouble(request, "heading", 0.0));

        String cacheKey = typeStr + scale + heading;
        byte[] imageData = imageCache.getIfPresent(cacheKey);
        if (imageData == null) {
            LOGGER.info("map marker cache miss " + typeStr + " (scale: " + scale + ", heading: " + heading + ")");

            Concept concept = ontologyRepository.getConceptById(typeStr, user);
            if (concept == null) {
                concept = ontologyRepository.getConceptByName(typeStr, user);
            }

            boolean isMapGlyphIcon = false;
            String glyphIconRowKey = getMapGlyphIcon(concept, user);
            if (glyphIconRowKey != null) {
                isMapGlyphIcon = true;
            } else {
                glyphIconRowKey = getGlyphIcon(concept, user);
                if (glyphIconRowKey == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            Resource resource = resourceRepository.findByRowKey(glyphIconRowKey, user);
            if (resource == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            imageData = getMarkerImage(resource, scale, heading, isMapGlyphIcon);
            imageCache.put(cacheKey, imageData);
        }

        ServletOutputStream out = response.getOutputStream();
        out.write(imageData);
        out.close();
    }

    private int roundHeadingAngle(double heading) {
        while (heading < 0.0) {
            heading += 360.0;
        }
        while (heading > 360.0) {
            heading -= 360.0;
        }
        return (int) (Math.round(heading / 10.0) * 10.0);
    }

    private byte[] getMarkerImage(Resource resource, long scale, int heading, boolean isMapGlyphIcon) throws IOException {
        BufferedImage resourceImage = resource.getContent().getDataImage();
        if (resourceImage == null) {
            return null;
        }

        if (heading != 0) {
            resourceImage = rotateImage(resourceImage, heading);
        }

        BufferedImage backgroundImage = getBackgroundImage(scale);
        if (backgroundImage == null) {
            return null;
        }
        int[] resourceImageDim = new int[]{resourceImage.getWidth(), resourceImage.getHeight()};

        BufferedImage image = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), backgroundImage.getType());
        Graphics2D g = image.createGraphics();
        if (isMapGlyphIcon) {
            int[] boundary = new int[]{backgroundImage.getWidth(), backgroundImage.getHeight()};
            int[] scaledDims = ArtifactThumbnailRepository.getScaledDimension(resourceImageDim, boundary);
            g.drawImage(resourceImage, 0, 0, scaledDims[0], scaledDims[1], null);
        } else {
            g.drawImage(backgroundImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), null);
            int size = image.getWidth() * 2 / 3;
            int[] boundary = new int[]{size, size};
            int[] scaledDims = ArtifactThumbnailRepository.getScaledDimension(resourceImageDim, boundary);
            int x = (backgroundImage.getWidth() - scaledDims[0]) / 2;
            int y = (backgroundImage.getWidth() - scaledDims[1]) / 2;
            g.drawImage(resourceImage, x, y, scaledDims[0], scaledDims[1], null);
        }
        g.dispose();
        return imageToBytes(image);
    }

    private BufferedImage rotateImage(BufferedImage image, int angleDeg) {
        BufferedImage rotatedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = rotatedImage.createGraphics();
        g.rotate(Math.toRadians(angleDeg), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return rotatedImage;
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

    private String getMapGlyphIcon(Concept concept, User user) {
        while (concept != null) {
            String mapGlyphIcon = (String) concept.getProperty(PropertyName.MAP_GLYPH_ICON);
            if (mapGlyphIcon != null) {
                return mapGlyphIcon;
            }

            concept = ontologyRepository.getParentConcept(concept.getId(), user);
        }

        return null;
    }

    private String getGlyphIcon(Concept concept, User user) {
        while (concept != null) {
            String glyphIcon = (String) concept.getProperty(PropertyName.GLYPH_ICON);
            if (glyphIcon != null) {
                return glyphIcon;
            }

            concept = ontologyRepository.getParentConcept(concept.getId(), user);
        }

        return null;
    }
}
