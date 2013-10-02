package com.altamiracorp.lumify.util;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class TeeInputStream {
    private IsCloseInputStream[] tees;
    private File tempFile;

    // TODO: this can be made faster by not writing to a file first and caching the data in memory.
    public TeeInputStream(InputStream source, int splits) throws IOException {
        this.tees = new IsCloseInputStream[splits];

        copySourceToFile(source);
        for (int i = 0; i < this.tees.length; i++) {
            this.tees[i] = new IsCloseInputStream(new FileInputStream(this.tempFile));
        }
    }

    private void copySourceToFile(InputStream source) throws IOException {
        tempFile = File.createTempFile("tee", "bin");
        FileOutputStream out = new FileOutputStream(tempFile);
        try {
            IOUtils.copy(source, out);
        } finally {
            out.close();
        }
    }

    public InputStream getTee(int idx) {
        return this.tees[idx];
    }

    private boolean isClosed(int idx) {
        return this.tees[idx].isClosed();
    }

    public void close() throws IOException {
        for (InputStream tee : this.tees) {
            tee.close();
        }
        //noinspection ResultOfMethodCallIgnored
        this.tempFile.delete();
    }

    public void loopUntilTeesAreClosed() {
        boolean allClosed = false;
        while (!allClosed) {
            allClosed = true;
            for (int i = 0; i < this.tees.length; i++) {
                if (!isClosed(i)) {
                    allClosed = false;
                    break;
                }
            }
            loop();
        }
    }

    public void loop() {

    }
}
