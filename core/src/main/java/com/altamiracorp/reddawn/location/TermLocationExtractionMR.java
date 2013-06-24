package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.geoNames.GeoNameRepository;
import com.altamiracorp.reddawn.ucd.AccumuloTermInputFormat;
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
import java.util.List;

public class TermLocationExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TermLocationExtractionMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloTermInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloTermInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return TermLocationExtractorMapper.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class TermLocationExtractorMapper extends Mapper<Text, Term, Text, Term> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "termLocationExtractorClass";
        private RedDawnSession session;
        GeoNameRepository geoNameRepository = new GeoNameRepository();
        SimpleTermLocationExtractor simpleTermLocationExtractor = new SimpleTermLocationExtractor();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
            LOGGER.info("Beginning term location extraction");
        }

        @Override
        protected void map(Text key, Term term, Context context) throws IOException, InterruptedException {
            try {
                Term updatedTerm = simpleTermLocationExtractor.GetTermWithLocationLookup(session.getModelSession(), geoNameRepository, term);
                if (updatedTerm != null) {
                    LOGGER.info("Extracting location from: " + term.getRowKey().toString());
                    context.write(new Text(Term.TABLE_NAME), updatedTerm);
                }

            } catch (Exception e) {
                throw new IOException(e);
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
