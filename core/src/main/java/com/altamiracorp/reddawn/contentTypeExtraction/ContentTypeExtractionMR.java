package com.altamiracorp.reddawn.contentTypeExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ContentTypeExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeExtractionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return ContentTypeExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class ContentTypeExtractorMapper extends RedDawnMapper<Text, Artifact, Text, Artifact> {
        private ArtifactRepository artifactRepository = new ArtifactRepository();
        private GraphRepository graphRepository = new GraphRepository();
        public static final String CONF_CONTENT_TYPE_EXTRACTOR_CLASS = "contentTypeExtractorClass";
        private ContentTypeExtractor contentTypeExtractor;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                contentTypeExtractor = (ContentTypeExtractor) context.getConfiguration().getClass(CONF_CONTENT_TYPE_EXTRACTOR_CLASS, TikaContentTypeExtractor.class).newInstance();
                contentTypeExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            LOGGER.info("Extracting content type from artifact: " + artifact.getRowKey().toString());
            InputStream in = artifactRepository.getRaw(getSession().getModelSession(), artifact);
            if (in == null) {
                LOGGER.warn("No data found for artifact: " + artifact.getRowKey().toString());
            }
            String contentType = contentTypeExtractor.extract(in, artifact.getGenericMetadata().getFileExtension());
            if (contentType == "") {
                LOGGER.warn("No content type set for artifact: " + artifact.getRowKey().toString());
            }
            artifact.getGenericMetadata().setMimeType(contentType);

            GraphVertex graphVertex = graphRepository.findVertexByRowKey(getSession().getGraphSession(), artifact.getRowKey().toString());
            if (graphVertex != null) {
                graphVertex.setProperty(PropertyName.SUBTYPE.toString(), artifact.getType().toString().toLowerCase());
                getSession().getGraphSession().commit();
            }

            context.write(new Text(Artifact.TABLE_NAME), artifact);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ContentTypeExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

