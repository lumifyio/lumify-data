package com.altamiracorp.reddawn.statementExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloSentenceInputFormat;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class StatementExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatementExtractionMR.class.getName());

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return StatementExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloSentenceInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloSentenceInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class StatementExtractorMapper extends Mapper<Text, Sentence, Text, Statement> {
        private StatementExtractor statementExtractor;
        public static final String CONF_STATEMENT_EXTRACTOR_CLASS = "statementExtractorClass";

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);

            try {
                statementExtractor = (StatementExtractor) context.getConfiguration().getClass(CONF_STATEMENT_EXTRACTOR_CLASS, SentenceBasedStatementExtractor.class).newInstance();
                statementExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void map(Text key, Sentence sentence, Context context) throws IOException, InterruptedException {
            LOGGER.info("Extracting statements for sentence: " + sentence.getRowKey().toString());

            try {
                Collection<Statement> statements = statementExtractor.extractStatements(sentence);
                writeStatements(context, statements);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void writeStatements(Context context, Collection<Statement> statements) throws IOException, InterruptedException {
            for (Statement statement : statements) {
                context.write(new Text(Statement.TABLE_NAME), statement);
            }
        }

        public static void init(Job job, Class<? extends StatementExtractor> statementExtractor) {
            job.getConfiguration().setClass(CONF_STATEMENT_EXTRACTOR_CLASS, statementExtractor, StatementExtractor.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new StatementExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
