package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ArtifactLocationExtractorMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactLocationExtractorMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Configuration c = getConfiguration();
        AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
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
        private TermMentionRepository termMentionRepository;
        private GraphRepository graphRepository;

        @Override
        protected void setup(Context context, Injector injector) throws Exception {
            entityExtractor = getAndInjectClassFromConfiguration(context, injector, CONF_ENTITY_EXTRACTOR_CLASS);
            entityExtractor.setup(context);
        }

        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            LOGGER.info("Extracting location from: " + artifact.getRowKey().toString());

            List<TermMention> termAndTermMentions = termMentionRepository.findByGraphVertexId(artifact.getRowKey().toString(), getUser());
            entityExtractor.extract(artifact, termAndTermMentions);
            updateGraphVertex(artifact);
            context.write(new Text(Artifact.TABLE_NAME), artifact);
        }

        private void updateGraphVertex(Artifact artifact) {
            throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//            String graphVertexId = artifact.getGenericMetadata().getGraphVertexId();
//            if (graphVertexId != null) {
//                GraphVertex vertex = graphRepository.findVertex(graphVertexId, getUser());
//                if (vertex != null) {
//                    Double lat = artifact.getDynamicMetadata().getLatitude();
//                    Double lon = artifact.getDynamicMetadata().getLongitude();
//                    if (lat != null && lon != null) {
//                        vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(lat, lon));
//                    }
//                    String geoLocationTitle = artifact.getDynamicMetadata().getGeoLocationTitle();
//                    if (geoLocationTitle != null) {
//                        vertex.setProperty(PropertyName.GEO_LOCATION_DESCRIPTION, geoLocationTitle);
//                    }
//                }
//            }
        }

        public static void init(Job job, Class<? extends ArtifactLocationExtractor> entityExtractor) {
            job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, ArtifactLocationExtractor.class);
        }

        @Inject
        public void setTermMentionRepository(TermMentionRepository termMentionRepository) {
            this.termMentionRepository = termMentionRepository;
        }

        @Inject
        public void setGraphRepository(GraphRepository graphRepository) {
            this.graphRepository = graphRepository;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ArtifactLocationExtractorMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
