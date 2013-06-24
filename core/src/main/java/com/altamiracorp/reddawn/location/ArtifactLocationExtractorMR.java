package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.AccumuloTermInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
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
import java.util.Collection;

public class ArtifactLocationExtractorMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactLocationExtractorMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloTermInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloTermInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        ArtifactLocationExtractorMapper.init(job, clazz);
        return ArtifactLocationExtractorMapper.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class ArtifactLocationExtractorMapper extends Mapper<Text, Term, Text, Row> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "artifactLocationExtractorClass";
        private ArtifactLocationExtractor entityExtractor;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                entityExtractor = (ArtifactLocationExtractor) context.getConfiguration().getClass(CONF_ENTITY_EXTRACTOR_CLASS, SimpleArtifactLocationExtractor.class).newInstance();
                entityExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        public void map(Text rowKey, Term term, Context context) throws IOException, InterruptedException {
            LOGGER.info("Extracting location from : " + term.getRowKey().toString());

            try {
                Collection<Artifact> artifacts = entityExtractor.extract(term);
                writeArtifacts(context, artifacts);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void writeArtifacts(Mapper.Context context, Collection<Artifact> artifacts) throws IOException, InterruptedException {
            for (Artifact artifact : artifacts) {
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            }
        }

        public static void init(Job job, Class<? extends ArtifactLocationExtractor> entityExtractor) {
            job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, ArtifactLocationExtractor.class);
        }

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ArtifactLocationExtractorMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
