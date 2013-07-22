package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloSentenceInputFormat;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SentenceHighlightMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceHighlightMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return SentenceHighlightMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloSentenceInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloSentenceInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class SentenceHighlightMapper extends Mapper<Text, Sentence, Text, Sentence> {
        private EntityHighlighter entityHighlighter = new EntityHighlighter();
        private RedDawnSession session;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        public void map(Text rowKey, Sentence sentence, Context context) throws IOException, InterruptedException {
            byte[] sentenceText = sentence.getData().getText().getBytes();
            if (sentenceText == null || sentenceText.length < 1) {
                return;
            }

            try {
                LOGGER.info("Creating sentence highlight text for: " + sentence.getRowKey().toString());
                String highlightedText = entityHighlighter.getHighlightedText(session, sentence); // THIS LINE
                if (highlightedText != null) {
                    sentence.getData().setHighlightedText(highlightedText);
                    context.write(new Text(Sentence.TABLE_NAME), sentence);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new SentenceHighlightMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

}

