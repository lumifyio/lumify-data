package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DevFileSystemSpout extends BaseFileSystemSpout {
    public static final String DATADIR_CONFIG_NAME = "datadir";
    private File dataDir;
    private Queue<File> files;

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        super.open(conf, topologyContext, collector);
        this.dataDir = new File((String) conf.get(DATADIR_CONFIG_NAME));
        this.files = new LinkedList<File>();

        IOFileFilter fileFilter = new WildcardFileFilter("*");
        IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        Iterator<File> fileIterator = FileUtils.iterateFiles(this.dataDir, fileFilter, directoryFilter);

        while (fileIterator.hasNext()) {
            try {
                File f = fileIterator.next();
                if (f.isFile() && !isSupportingFile(f) && !f.getName().startsWith(".")) {
                    this.files.add(f);
                }
            } catch (Exception ex) {
                collector.reportError(ex);
            }
        }
    }

    @Override
    public void nextTuple() {
        if (this.files.size() == 0) {
            Utils.sleep(100);
            return;
        }

        File f = files.remove();
        emit(f.getAbsolutePath());
    }
}
