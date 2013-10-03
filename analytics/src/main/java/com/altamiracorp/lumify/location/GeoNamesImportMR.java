package com.altamiracorp.lumify.location;

import java.io.IOException;

import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.ToolRunner;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.model.geoNames.GeoNameRepository;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class GeoNamesImportMR extends ConfigurableMapJobBase {

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        return TextInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return GeoNamesImportMapper.class;
    }

    public static class GeoNamesImportMapper extends LumifyMapper<LongWritable, Text, NullWritable, NullWritable> {
        private GeoNameRepository geoNameRepository;
        private GeoNamesImporter geoNamesImporter;

        @Override
        protected void setup(Context context, Injector injector) throws IOException, InterruptedException {
        }

        @Override
        protected void safeMap(LongWritable key, Text value, Context context) throws Exception {
            geoNameRepository.save(geoNamesImporter.lineToGeoName(value.toString()), getUser());
        }

        @Inject
        public void setGeoNameRepository(GeoNameRepository geoNameRepository) {
            this.geoNameRepository = geoNameRepository;
        }

        @Inject
        public void setGeoNamesImporter(GeoNamesImporter geoNamesImporter) {
            this.geoNamesImporter = geoNamesImporter;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new GeoNamesImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
