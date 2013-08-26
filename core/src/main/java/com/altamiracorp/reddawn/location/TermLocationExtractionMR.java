package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.AccumuloTermMentionInputFormat;
import com.altamiracorp.reddawn.model.geoNames.GeoNamePostalCodeRepository;
import com.altamiracorp.reddawn.model.geoNames.GeoNameRepository;
import com.altamiracorp.reddawn.model.termMention.TermMention;
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
        AccumuloTermMentionInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
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

    public static class TermLocationExtractorMapper extends RedDawnMapper<Text, TermMention, Text, TermMention> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "termLocationExtractorClass";
        GeoNameRepository geoNameRepository = new GeoNameRepository();
        GeoNamePostalCodeRepository geoNamePostalCodeRepository = new GeoNamePostalCodeRepository();
        SimpleTermLocationExtractor simpleTermLocationExtractor = new SimpleTermLocationExtractor();

        @Override
        protected void safeMap(Text key, TermMention termMention, Context context) throws Exception {
            TermMention updatedTerm;
            if (simpleTermLocationExtractor.isPostalCode(termMention)) {
                updatedTerm = simpleTermLocationExtractor.GetTermWithPostalCodeLookup(getSession().getModelSession(), geoNamePostalCodeRepository, termMention);
            } else {
                updatedTerm = simpleTermLocationExtractor.GetTermWithLocationLookup(getSession().getModelSession(), geoNameRepository, termMention);
            }
            if (updatedTerm != null) {
                LOGGER.info("Extracting location from: " + termMention.getRowKey().toString());
                context.write(new Text(TermMention.TABLE_NAME), updatedTerm);
            }
        }

        public static void init(Job job, Class<? extends ArtifactLocationExtractor> entityExtractor) {
            job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, ArtifactLocationExtractor.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new TermLocationExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
