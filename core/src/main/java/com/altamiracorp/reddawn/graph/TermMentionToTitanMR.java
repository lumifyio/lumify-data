package com.altamiracorp.reddawn.graph;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.ontology.Concept;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.ucd.AccumuloTermInputFormat;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
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

public class TermMentionToTitanMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TermMentionToTitanMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloTermInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloTermInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return TermToTitan.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class TermToTitan extends Mapper<Text, Term, Text, Row> {
        private RedDawnSession session;
        private TermRepository termRepository = new TermRepository();
        private OntologyRepository ontologyRepository = new OntologyRepository();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        public void map(Text rowKey, Term term, Context context) throws IOException, InterruptedException {
            LOGGER.info("Adding term to titan: " + term.getRowKey().toString());
            try {
                for (TermMention termMention : term.getTermMentions()) {
                    String conceptLabel = term.getRowKey().getConceptLabel();
                    Concept concept = ontologyRepository.getConceptByName(session.getGraphSession(), conceptLabel);
                    if (concept == null) {
                        throw new RuntimeException("Could not find concept: " + conceptLabel);
                    }

                    termRepository.saveToGraph(session.getModelSession(), session.getGraphSession(), term, termMention, concept.getId());
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            session.close();
            super.cleanup(context);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new TermMentionToTitanMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
