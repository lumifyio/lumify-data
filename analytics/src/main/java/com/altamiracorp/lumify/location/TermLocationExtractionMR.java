package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.AccumuloTermMentionInputFormat;
import com.altamiracorp.lumify.model.geoNames.GeoNamePostalCodeRepository;
import com.altamiracorp.lumify.model.geoNames.GeoNameRepository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.google.inject.Inject;
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

public class TermLocationExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TermLocationExtractionMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Configuration c = getConfiguration();
        AccumuloTermMentionInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloTermMentionInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return TermLocationExtractorMapper.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class TermLocationExtractorMapper extends LumifyMapper<Text, TermMention, Text, TermMention> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "termLocationExtractorClass";
        private GeoNameRepository geoNameRepository;
        private GraphRepository graphRepository;
        private GeoNamePostalCodeRepository geoNamePostalCodeRepository;
        private SimpleTermLocationExtractor simpleTermLocationExtractor;

        @Override
        protected void safeMap(Text key, TermMention termMention, Context context) throws Exception {
            LOGGER.info("Extracting location from: " + termMention.getRowKey().toString());

            TermMention updatedTerm;
            if (simpleTermLocationExtractor.isPostalCode(termMention)) {
                updatedTerm = simpleTermLocationExtractor.GetTermWithPostalCodeLookup(geoNamePostalCodeRepository, termMention, getUser());
            } else {
                updatedTerm = simpleTermLocationExtractor.GetTermWithLocationLookup(geoNameRepository, termMention, getUser());
            }
            if (updatedTerm != null) {
                updateGraphVertex(updatedTerm);
                context.write(new Text(TermMention.TABLE_NAME), updatedTerm);
            }
        }

        private void updateGraphVertex(TermMention termMention) {
            String graphVertexId = termMention.getMetadata().getGraphVertexId();
            if (graphVertexId != null) {
                GraphVertex vertex = graphRepository.findVertex(graphVertexId, getUser());
                if (vertex != null) {
                    Double lat = termMention.getMetadata().getLatitude();
                    Double lon = termMention.getMetadata().getLongitude();
                    if (lat != null && lon != null) {
                        vertex.setProperty(PropertyName.GEO_LOCATION, Geoshape.point(lat, lon));
                    }
                }
            }
        }

        public static void init(Job job, Class<? extends ArtifactLocationExtractor> entityExtractor) {
            job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, ArtifactLocationExtractor.class);
        }

        @Inject
        public void setGeoNameRepository(GeoNameRepository geoNameRepository) {
            this.geoNameRepository = geoNameRepository;
        }

        @Inject
        public void setGraphRepository(GraphRepository graphRepository) {
            this.graphRepository = graphRepository;
        }

        @Inject
        public void setGeoNamePostalCodeRepository(GeoNamePostalCodeRepository geoNamePostalCodeRepository) {
            this.geoNamePostalCodeRepository = geoNamePostalCodeRepository;
        }

        @Inject
        public void setSimpleTermLocationExtractor(SimpleTermLocationExtractor simpleTermLocationExtractor) {
            this.simpleTermLocationExtractor = simpleTermLocationExtractor;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new TermLocationExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
