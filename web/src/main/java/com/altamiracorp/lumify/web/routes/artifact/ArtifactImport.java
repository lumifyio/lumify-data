package com.altamiracorp.lumify.web.routes.artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.FileImporter;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

public class ArtifactImport extends BaseRequestHandler {
    private FileImporter fileImporter = new FileImporter();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        List<Part> files = new ArrayList<Part>(request.getParts());
        if (files.size() != 1) {
            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
        }

        AppSession session = app.getAppSession(request);
        Part file = files.get(0);

        File tempFile = File.createTempFile("fileImport", ".bin");
        writeToTempFile(file, tempFile);

        List<FileImporter.Result> results = fileImporter.writePackage(session, tempFile, "File Upload");

        tempFile.delete();

        JSONObject json = new JSONObject();
        json.put("results", FileImporter.Result.toJson(results));

        respondWithJson(response, json);
    }

    private void writeToTempFile(Part file, File tempFile) throws IOException {
        InputStream in = file.getInputStream();
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
