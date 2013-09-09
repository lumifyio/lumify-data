package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.FileImporter;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ArtifactImport implements Handler, AppAware {
    private WebApp app;
    private FileImporter fileImporter = new FileImporter();

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        List<Part> files = new ArrayList<Part>(request.getParts());
        if (files.size() != 1) {
            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
        }
        Part file = files.get(0);

        File tempFile = File.createTempFile("fileImport", ".bin");
        writeToTempFile(file, tempFile);

        fileImporter.writePackage(session, tempFile, "File Upload");

        tempFile.delete();

        new Responder(response).respondWith("OK");
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
