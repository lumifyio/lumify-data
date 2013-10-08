package com.altamiracorp.lumify.storm.document;

import org.apache.hadoop.fs.FileSystem;

class AdditionalWorkData {

    private String mimeType;
    private String fileName;
    private FileSystem hdfsFileSystem;

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public FileSystem getHdfsFileSystem() {
        return hdfsFileSystem;
    }

    public void setHdfsFileSystem(FileSystem hdfsFileSystem) {
        this.hdfsFileSystem = hdfsFileSystem;
    }
}
