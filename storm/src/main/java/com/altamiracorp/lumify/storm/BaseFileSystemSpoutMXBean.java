package com.altamiracorp.lumify.storm;

public interface BaseFileSystemSpoutMXBean {
    public int getWorkingCount();

    public int getToBeProcessedCount();

    public int getTotalProcessedCount();

    public String getPath();
}
