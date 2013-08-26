package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class SearchIndexBuilderMR extends ConfigurableMapJobBase {
    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        SearchMapper.init(job, clazz);
        return SearchMapper.class;
    }

    public static class SearchMapper extends RedDawnMapper<Text, Artifact, Text, Mutation> {
        private static final String CONF_SEARCH_INDEX_BUILDER_CLASS = "searchIndexBuilderClass";
        private SearchProvider searchProvider;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                searchProvider = (SearchProvider) context.getConfiguration().getClass(CONF_SEARCH_INDEX_BUILDER_CLASS, null).newInstance();
                searchProvider.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            searchProvider.add(artifact);
        }

        public static void init(Job job, Class searchClass) {
            job.getConfiguration().setClass(CONF_SEARCH_INDEX_BUILDER_CLASS, searchClass, SearchProvider.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new SearchIndexBuilderMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return NullOutputFormat.class;
    }
}
