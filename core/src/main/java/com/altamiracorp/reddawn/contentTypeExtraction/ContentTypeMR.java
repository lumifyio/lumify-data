package com.altamiracorp.reddawn.contentTypeExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class ContentTypeMR  extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeMR.class.getName());

    @Override
    protected Class getMapperClass (Job job, Class clazz){
        return ContentTypeMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit (Job job){
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(),
                getZookeeperInstanceName());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class ContentTypeMapper extends Mapper<Text, Artifact, Text, Artifact>{
        private ArtifactRepository artifactRepository = new ArtifactRepository();
        public static final String CONF_CONTENT_TYPE_EXTRACTOR_CLASS = "contentTypeExtractorClass";
        private ContentTypeExtractor contentTypeExtractor;
        private RedDawnSession session;

        public ContentTypeExtractor getContentTypeExtractor() {
            return contentTypeExtractor;
        }

        public void setContentTypeExtractor(ContentTypeExtractor contentTypeExtractor) {
            this.contentTypeExtractor = contentTypeExtractor;
        }

        @Override
        protected void setup (Context context) throws IOException, InterruptedException{
            super.setup(context);
            try {
                contentTypeExtractor = (ContentTypeExtractor) context.getConfiguration().getClass(CONF_CONTENT_TYPE_EXTRACTOR_CLASS,TikaContentTypeExtractor.class).newInstance();
                contentTypeExtractor.setup(context);
                session = ConfigurableMapJobBase.createRedDawnSession(context);
            }
            catch (InstantiationException e) {
                throw new IOException(e);
            }
            catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        public void map (Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try{
                LOGGER.info ("Extracting content type from artifact: " + artifact.getRowKey().toString());
                InputStream in = artifactRepository.getRaw(session.getModelSession(), artifact);
                if (in == null){
                    LOGGER.warn("No data found for artifact: " + artifact.getRowKey().toString());
                    return;
                }
                String contentType = contentTypeExtractor.extract(in);
                artifact.getGenericMetadata().setMimeType(contentType);
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            }
            catch (Exception e){
                throw new IOException(e);
            }
        }

        public static void init (Job job, Class<? extends TikaContentTypeExtractor> tikaContentTypeExtractor){
            job.getConfiguration().setClass(CONF_CONTENT_TYPE_EXTRACTOR_CLASS, tikaContentTypeExtractor, TikaContentTypeExtractor.class);
        }

        public RedDawnSession getSession() {
            return session;
        }

        public void setSession(RedDawnSession session) {
            this.session = session;
        }

        public ArtifactRepository getArtifactRepository() {
            return artifactRepository;
        }

        public void setArtifactRepository(ArtifactRepository artifactRepository) {
            this.artifactRepository = artifactRepository;
        }
    }

    public static void main (String [] args) throws Exception {
        int res = ToolRunner.run (CachedConfiguration.getInstance(), new ContentTypeMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
