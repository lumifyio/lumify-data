package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.ucd.AccumuloTermInputFormat;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import java.io.*;

public class SearchTermIndexBuilderMR extends ConfigurableMapJobBase {

    @Override
    protected Class <? extends Mapper> getMapperClass (Job job, Class clazz){
        TermSearchMapper.init(job, clazz);
        return TermSearchMapper.class;
    }

    @Override
    protected Class <? extends InputFormat> getInputFormatClassAndInit (Job job){
        AccumuloTermInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloTermInputFormat.class;
    }

    @Override
    protected  Class <? extends OutputFormat> getOutputFormatClass (){
        return NullOutputFormat.class;
    }

    public static class TermSearchMapper extends Mapper<Text, Term, Text, Mutation> {
        private static final String CONF_TERM_SEARCH_INDEX_BUILDER_CLASS="searchTermIndexBuilder";
        private SearchProvider searchProvider;

        @Override
        protected  void setup (Context context) throws IOException, InterruptedException{
            super.setup(context);
            try{
                searchProvider = (SearchProvider) context.getConfiguration().getClass(CONF_TERM_SEARCH_INDEX_BUILDER_CLASS, NullSearchProvider.class).newInstance();
                searchProvider.setup(context);
            }
            catch (InstantiationException e){
                throw new IOException(e);
            }
            catch (IllegalAccessException e){
                throw new IOException(e);
            }
            catch (Exception e){
                throw new IOException(e);
            }
        }

        @Override
        protected void map (Text rowKey, Term term, Context context) throws IOException, InterruptedException{
            try{
                searchProvider.add(term);
            }
            catch (Exception e){
                throw new IOException(e);
            }
        }

        public static void init (Job job, Class searchClass){
            job.getConfiguration().setClass(CONF_TERM_SEARCH_INDEX_BUILDER_CLASS, searchClass,SearchProvider.class);
        }
    }

    public static void main (String [] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new SearchTermIndexBuilderMR(), args);
        if (res != 0){
            System.exit(res);
        }
    }
}
