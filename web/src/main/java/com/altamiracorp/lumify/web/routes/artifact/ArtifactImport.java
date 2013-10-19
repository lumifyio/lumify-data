package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ArtifactImport extends BaseRequestHandler {
    private final FileImporter fileImporter;

    @Inject
    public ArtifactImport(FileImporter fileImporter) {
        this.fileImporter = fileImporter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        InputStream fis;
        if (request.getContentType().equals(MediaType.MULTIPART_FORM_DATA)) {
            List<Part> files = new ArrayList<Part>(request.getParts());
            if (files.size() != 1) {
                throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
            }

            Part file = files.get(0);
            fis = file.getInputStream();
        } else {
            fis = request.getInputStream();
        }
        File tempFile = File.createTempFile("fileImport", ".bin");
        writeToTempFile(fis, tempFile);

        List<FileImporter.Result> results = fileImporter.writePackage(tempFile, "File Upload", user);

        tempFile.delete();

        JSONObject json = new JSONObject();
        json.put("results", FileImporter.Result.toJson(results));

        respondWithJson(response, json);
    }

    private void writeToTempFile(InputStream in, File tempFile) throws IOException {
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            try {
                IOUtils.copy(in, out);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
