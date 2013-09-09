package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.thinkaurelius.titan.core.attribute.Geoshape;
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
import java.util.List;

public class ArtifactLocationExtractorMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactLocationExtractorMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
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

    public static class ArtifactLocationExtractorMapper extends LumifyMapper<Text, Artifact, Text, Row> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "artifactLocationExtractorClass";
        private ArtifactLocationExtractor entityExtractor;
        private TermMentionRepository termMentionRepository = new TermMentionRepository();
        private GraphRepository graphRepository = new GraphRepository();

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

        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            LOGGER.info("Extracting location from: " + artifact.getRowKey().toString());

            List<TermMention> termAndTermMentions = termMentionRepository.findByArtifactRowKey(getSession().getModelSession(), artifact.getRowKey().toString());
            entityExtractor.extract(artifact, termAndTermMentions);
            updateGraphVertex(artifact);
            context.write(new Text(Artifact.TABLE_NAME), artifact);
        }

        private void updateGraphVertex(Artifact artifact) {
            String graphVertexId = artifact.getGenericMetadata().getGraphVertexId();
            if (graphVertexId != null) {
                GraphVertex vertex = graphRepository.findVertex(getSession().getGraphSession(), graphVertexId);
                if (vertex != null) {
                    Double lat = artifact.getDynamicMetadata().getLatitude();
                    Double lon = artifact.getDynamicMetadata().getLongitude();
                    if (lat != null && lon != null) {
                        vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(lat, lon));
                    }
                }
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
