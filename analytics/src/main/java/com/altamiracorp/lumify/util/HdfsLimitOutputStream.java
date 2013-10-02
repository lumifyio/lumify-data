package com.altamiracorp.lumify.util;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class HdfsLimitOutputStream extends OutputStream {
    private static Random random = new Random();
    private final FileSystem fs;
    private final int maxSizeToStore;
    private final ByteArrayOutputStream smallOutputStream;
    private OutputStream largeOutputStream;
    private Path hdfsPath;

    public HdfsLimitOutputStream(FileSystem fs, int maxSizeToStore) {
        this.fs = fs;
        this.maxSizeToStore = maxSizeToStore;
        this.smallOutputStream = new ByteArrayOutputStream(maxSizeToStore);
    }

    private OutputStream getLargeOutputStream() throws IOException {
        if (largeOutputStream == null) {
            hdfsPath = createTempPath();
            largeOutputStream = fs.create(hdfsPath);
        }
        return largeOutputStream;
    }

    private Path createTempPath() {
        return new Path("/tmp/hdfsLimitOutputStream-" + random.nextLong());
    }

    @Override
    public void write(int b) throws IOException {
        if (this.smallOutputStream.size() < maxSizeToStore - 1) {
            this.smallOutputStream.write(b);
        } else {
            getLargeOutputStream().write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (this.smallOutputStream.size() < maxSizeToStore - b.length) {
            this.smallOutputStream.write(b);
        } else {
            getLargeOutputStream().write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (this.smallOutputStream.size() < maxSizeToStore - len) {
            this.smallOutputStream.write(b, off, len);
        } else {
            getLargeOutputStream().write(b, off, len);
        }
    }

    public boolean hasExceededSizeLimit() {
        return this.largeOutputStream != null;
    }

    public Path getHdfsPath() {
        return hdfsPath;
    }

    public byte[] getSmall() {
        return this.smallOutputStream.toByteArray();
    }
}
