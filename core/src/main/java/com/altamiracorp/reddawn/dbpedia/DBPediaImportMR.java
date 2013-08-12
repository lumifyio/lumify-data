package com.altamiracorp.reddawn.dbpedia;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloDBPediaInputFormat;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.dbpedia.DBPedia;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class DBPediaImportMR extends ConfigurableMapJobBase {
    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return DBPediaImportMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloDBPediaInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloDBPediaInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

    public static class DBPediaImportMapper extends Mapper<Text, DBPedia, Text, Row> {
        private TermRepository termRepository = new TermRepository();
        private StatementRepository statementRepository = new StatementRepository();
        private DBPediaHelper dbPediaHelper = new DBPediaHelper();
        private RedDawnSession session;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        public void map(Text rowKey, DBPedia dbPedia, Context context) throws IOException, InterruptedException {
            String dbpediaSourceArtifactRowKey = session.getModelSession().getDbpediaSourceArtifactRowKey();

            Term term = dbPediaHelper.createTerm(dbPedia, dbpediaSourceArtifactRowKey);
            if (term == null) {
                return;
            }

            DBPediaHelper.CreateStatementsResult createStatementsResult = dbPediaHelper.createStatements(session, term, dbPedia, dbpediaSourceArtifactRowKey);

            System.out.println("Importing: " + dbPedia);
            termRepository.save(session.getModelSession(), term);
            termRepository.saveMany(session.getModelSession(), createStatementsResult.terms);
            statementRepository.saveMany(session.getModelSession(), createStatementsResult.statements);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new DBPediaImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

