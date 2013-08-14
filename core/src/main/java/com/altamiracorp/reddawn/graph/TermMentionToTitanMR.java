package com.altamiracorp.reddawn.graph;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
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

    public static class TermToTitan extends RedDawnMapper<Text, Term, Text, Row> {
        private TermRepository termRepository = new TermRepository();
        private OntologyRepository ontologyRepository = new OntologyRepository();

        public void safeMap(Text rowKey, Term term, Context context) throws Exception {
            LOGGER.info("Adding term to titan: " + term.getRowKey().toString());
            for (TermMention termMention : term.getTermMentions()) {
                String conceptLabel = term.getRowKey().getConceptLabel();
                Concept concept = ontologyRepository.getConceptByName(getSession().getGraphSession(), conceptLabel);
                if (concept == null) {
                    throw new RuntimeException("Could not find concept: " + conceptLabel);
                }

                termRepository.saveToGraph(getSession().getModelSession(), getSession().getGraphSession(), term, termMention, concept.getId());
            }
            getSession().getGraphSession().commit();
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
