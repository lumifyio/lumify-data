package com.altamiracorp.lumify.contentTypeExtraction;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ContentTypeExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeExtractionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return ContentTypeExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Configuration c = getConfiguration();
        AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class ContentTypeExtractorMapper extends LumifyMapper<Text, Artifact, Text, Artifact> {
        private ArtifactRepository artifactRepository;
        private GraphRepository graphRepository;
        public static final String CONF_CONTENT_TYPE_EXTRACTOR_CLASS = "contentTypeExtractorClass";
        private ContentTypeExtractor contentTypeExtractor;

        @Override
        protected void setup(Context context, Injector injector) throws IllegalAccessException, InstantiationException {
            contentTypeExtractor = (ContentTypeExtractor) context.getConfiguration().getClass(CONF_CONTENT_TYPE_EXTRACTOR_CLASS, TikaContentTypeExtractor.class).newInstance();
            contentTypeExtractor.setup(context);
        }

        @Override
        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            LOGGER.info("Extracting content type from artifact: " + artifact.getRowKey().toString());
            InputStream in = artifactRepository.getRaw(artifact, getUser());
            if (in == null) {
                LOGGER.warn("No data found for artifact: " + artifact.getRowKey().toString());
            }
            String contentType = contentTypeExtractor.extract(in, artifact.getGenericMetadata().getFileExtension());
            if (contentType.equals("")) {
                LOGGER.warn("No content type set for artifact: " + artifact.getRowKey().toString());
            }
            artifact.getGenericMetadata().setMimeType(contentType);

            GraphVertex graphVertex = graphRepository.findVertexByRowKey(artifact.getRowKey().toString(), getUser());
            if (graphVertex != null) {
                graphVertex.setProperty(PropertyName.SUBTYPE.toString(), artifact.getType().toString().toLowerCase());
                getSession().getGraphSession().commit();
            }

            context.write(new Text(Artifact.TABLE_NAME), artifact);
        }

        @Inject
        public void setArtifactRepository(ArtifactRepository artifactRepository) {
            this.artifactRepository = artifactRepository;
        }

        @Inject
        public void setGraphRepository(GraphRepository graphRepository) {
            this.graphRepository = graphRepository;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ContentTypeExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

