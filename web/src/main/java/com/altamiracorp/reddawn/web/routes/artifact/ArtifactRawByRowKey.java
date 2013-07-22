package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
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

public class ArtifactRawByRowKey implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactRawByRowKey.class.getName());
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=([0-9]*)-([0-9]*)");

    ArtifactRepository artifactRepository = new ArtifactRepository();
    private WebApp app;

    public static String getUrl(HttpServletRequest request, ArtifactRowKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/raw";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        boolean download = request.getParameter("download") != null;
        boolean videoPlayback = request.getParameter("playback") != null;

        RedDawnSession session = app.getRedDawnSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey.toString());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        String fileName = getFileName(artifact);
        if (videoPlayback) {
            handlePartialPlayback(request, response, session, artifact, fileName);
        } else {
            String mimeType = getMimeType(artifact);
            response.setContentType(mimeType);
            if (download) {
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.addHeader("Content-Disposition", "inline; filename=" + fileName);
            }
            InputStream in = artifactRepository.getRaw(session.getModelSession(), artifact);
            try {
                IOUtils.copy(in, response.getOutputStream());
            } finally {
                in.close();
            }
        }

        chain.next(request, response);
    }

    private void handlePartialPlayback(HttpServletRequest request, HttpServletResponse response, RedDawnSession session, Artifact artifact, String fileName) throws IOException {
        String videoType = request.getParameter("type");
        InputStream in;
        long totalLength;
        long partialStart = 0;
        Long partialEnd = null;
        String range = request.getHeader("Range");
        if (range != null) {
            Matcher m = RANGE_PATTERN.matcher(range);
            if (m.matches()) {
                partialStart = Long.parseLong(m.group(1));
                if (m.group(2).length() > 0) {
                    partialEnd = Long.parseLong(m.group(2));
                }
                if (partialEnd == null) {
                    partialEnd = partialStart + 100000 - 1;
                }
                response.setStatus(206);
            }
        }

        if (videoType.equals("video/mp4")) {
            response.setContentType("video/mp4");
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".mp4");
            in = artifactRepository.getRawMp4(session.getModelSession(), artifact);
            totalLength = artifactRepository.getRawMp4Length(session.getModelSession(), artifact);
        } else if (videoType.equals("video/webm")) {
            response.setContentType("video/webm");
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".webm");
            in = artifactRepository.getRawWebm(session.getModelSession(), artifact);
            totalLength = artifactRepository.getRawWebmLength(session.getModelSession(), artifact);
        } else {
            throw new RuntimeException("Invalid video type: " + videoType);
        }

        if (partialEnd == null) {
            partialEnd = totalLength;
        }

        long partialLength = partialEnd - partialStart + 1;
        response.addHeader("Content-Length", "" + partialLength);
        response.addHeader("Content-Range", "bytes " + partialStart + "-" + partialEnd + "/" + totalLength);
        if (partialStart > 0) {
            in.skip(partialStart);
        }

        OutputStream out = response.getOutputStream();
        copy(in, out, partialLength);
    }

    private void copy(InputStream in, OutputStream out, Long length) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while (length > 0 && (read = in.read(buffer, 0, (int) Math.min(length, buffer.length))) > 0) {
            out.write(buffer, 0, read);
            length -= read;
        }
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    private String getFileName(Artifact artifact) {
        return artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension();
    }

    private String getMimeType(Artifact artifact) {
        String mimeType = artifact.getGenericMetadata().getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }
}
