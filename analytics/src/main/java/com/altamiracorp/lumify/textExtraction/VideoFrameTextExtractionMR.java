package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.AccumuloVideoFrameInputFormat;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VideoFrameTextExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoFrameTextExtractionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        VideoFrameTextExtractorMapper.init(job, clazz);
        return VideoFrameTextExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Configuration c = getConfiguration();
        AccumuloVideoFrameInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloVideoFrameInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class VideoFrameTextExtractorMapper extends LumifyMapper<Text, VideoFrame, Text, VideoFrame> {
        public static final String CONF_TEXT_EXTRACTOR_CLASS = "textExtractorClass";
        private TextExtractor textExtractor;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                textExtractor = (TextExtractor) context.getConfiguration().getClass(CONF_TEXT_EXTRACTOR_CLASS, TikaTextExtractor.class).newInstance();
                textExtractor.setup(context);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void safeMap(Text rowKey, VideoFrame videoFrame, Context context) throws Exception {
            LOGGER.info("Extracting text from video frame: " + videoFrame.getRowKey().toString());
            VideoFrameExtractedInfo extractedInfo = textExtractor.extract(getSession().getModelSession(), videoFrame);
            if (extractedInfo == null) {
                return;
            }

            if (extractedInfo.getText() != null) {
                videoFrame.getMetadata().setText(extractedInfo.getText());
            }

            context.write(new Text(VideoFrame.TABLE_NAME), videoFrame);
        }

        public static void init(Job job, Class<? extends TextExtractor> textExtractorClass) {
            job.getConfiguration().setClass(CONF_TEXT_EXTRACTOR_CLASS, textExtractorClass, TextExtractor.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new VideoFrameTextExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

