package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import com.google.inject.Inject;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtifactRawByRowKey extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactRawByRowKey.class);
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=([0-9]*)-([0-9]*)");

    private final ArtifactRepository artifactRepository;

    @Inject
    public ArtifactRawByRowKey(final ArtifactRepository repo) {
        artifactRepository = repo;
    }

    public static String getUrl(ArtifactRowKey artifactKey) {
        return "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/raw";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        boolean download = getOptionalParameter(request, "download") != null;
        boolean videoPlayback = getOptionalParameter(request, "playback") != null;

        User user = getUser(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode(getAttributeString(request, "_rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(artifactKey.toString(), user);

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        String fileName = getFileName(artifact);
        if (videoPlayback) {
            handlePartialPlayback(request, response, artifact, fileName, user);
        } else {
            String mimeType = getMimeType(artifact);
            response.setContentType(mimeType);
            if (download) {
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.addHeader("Content-Disposition", "inline; filename=" + fileName);
            }
            InputStream in = artifactRepository.getRaw(artifact, user);
            try {
                IOUtils.copy(in, response.getOutputStream());
            } finally {
                in.close();
            }
        }

        chain.next(request, response);
    }

    private void handlePartialPlayback(HttpServletRequest request, HttpServletResponse response, Artifact artifact, String fileName, User user) throws IOException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        String videoType = getRequiredParameter(request, "type");
//        InputStream in;
//        long totalLength;
//        long partialStart = 0;
//        Long partialEnd = null;
//        String range = request.getHeader("Range");
//        if (range != null) {
//            Matcher m = RANGE_PATTERN.matcher(range);
//            if (m.matches()) {
//                partialStart = Long.parseLong(m.group(1));
//                if (m.group(2).length() > 0) {
//                    partialEnd = Long.parseLong(m.group(2));
//                }
//                if (partialEnd == null) {
//                    partialEnd = partialStart + 100000 - 1;
//                }
//                response.setStatus(206);
//            }
//        }
//
//        if (videoType.equals("video/mp4")) {
//            response.setContentType("video/mp4");
//            response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".mp4");
//            in = artifactRepository.getRawMp4(artifact, user);
//            totalLength = artifactRepository.getRawMp4Length(artifact, user);
//        } else if (videoType.equals("video/webm")) {
//            response.setContentType("video/webm");
//            response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".webm");
//            in = artifactRepository.getRawWebm(artifact, user);
//            totalLength = artifactRepository.getRawWebmLength(artifact, user);
//        } else {
//            throw new RuntimeException("Invalid video type: " + videoType);
//        }
//
//        if (partialEnd == null) {
//            partialEnd = totalLength;
//        }
//
//        long partialLength = partialEnd - partialStart + 1;
//        response.addHeader("Content-Length", "" + partialLength);
//        response.addHeader("Content-Range", "bytes " + partialStart + "-" + partialEnd + "/" + totalLength);
//        if (partialStart > 0) {
//            in.skip(partialStart);
//        }
//
//        OutputStream out = response.getOutputStream();
//        copy(in, out, partialLength);
    }

    private void copy(InputStream in, OutputStream out, Long length) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while (length > 0 && (read = in.read(buffer, 0, (int) Math.min(length, buffer.length))) > 0) {
            out.write(buffer, 0, read);
            length -= read;
        }
    }

    private String getFileName(Artifact artifact) {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        return artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension();
    }

    private String getMimeType(Artifact artifact) {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        String mimeType = artifact.getGenericMetadata().getMimeType();
//        if (mimeType == null || mimeType.isEmpty()) {
//            mimeType = "application/octet-stream";
//        }
//        return mimeType;
    }
}
