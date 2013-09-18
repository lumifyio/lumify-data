package com.altamiracorp.lumify.search;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
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

    public static class SearchMapper extends LumifyMapper<Text, Artifact, Text, Mutation> {
        private static final String CONF_SEARCH_INDEX_BUILDER_CLASS = "searchIndexBuilderClass";
        private SearchProvider searchProvider;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                searchProvider = (SearchProvider) context.getConfiguration().getClass(CONF_SEARCH_INDEX_BUILDER_CLASS, null).newInstance();
                searchProvider.setup(context);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            searchProvider.add(artifact);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            try {
                searchProvider.teardown();
            } catch (Exception e) {
                throw new IOException(e);
            }
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
        Configuration c = getConfiguration();
        AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return NullOutputFormat.class;
    }
}
