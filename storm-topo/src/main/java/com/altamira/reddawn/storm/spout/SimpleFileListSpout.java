package com.altamira.reddawn.storm.spout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

/**
 * Quick, dumb spout that reads files from a local directory based on a wildcard
 * pattern. Scalability was not considered whatsoever when I wrote this, but it
 * works for testing out a topology!
 * 
 */
public class SimpleFileListSpout extends BaseRichSpout {

	// this is most likely a hack, but I simply don't care at this point
	// I WANT DATA!!!!
	private static Integer fileCount = 0;
	private SpoutOutputCollector collector;
	private File[] fileList;

	public SimpleFileListSpout(File directory, String filePattern) {
		fileList = directory.listFiles((FilenameFilter) new WildcardFileFilter(
				filePattern));
	}

	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		this.collector = collector;
	}

	public void nextTuple() {
		synchronized (fileCount) {
			if (fileCount < fileList.length) {
				String id = FilenameUtils.removeExtension(fileList[fileCount]
						.getName());
				try {
					collector
							.emit(new Values(id, readFile(fileList[fileCount])));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			fileCount++;
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("docId", "fullText"));

	}

	private String readFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

}
