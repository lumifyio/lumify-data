package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.model.geoNames.GeoNameAdmin1Code;
import com.altamiracorp.lumify.model.geoNames.GeoNameAdmin1CodeRepository;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class Admin1CodesImportMR extends ConfigurableMapJobBase {

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        return TextInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return Admin1CodesImportMapper.class;
    }

    public static class Admin1CodesImportMapper extends LumifyMapper<LongWritable, Text, NullWritable, NullWritable> {
        private GeoNamesImporter geoNamesImporter = new GeoNamesImporter();
        private GeoNameAdmin1CodeRepository geoNameAdmin1CodeRepository;

        @Override
        protected void setup(Context context, Injector injector) throws IOException, InterruptedException {
        }

        @Override
        protected void safeMap(LongWritable key, Text value, Context context) throws Exception {
            geoNameAdmin1CodeRepository.save(geoNamesImporter.lineToAdmin1Code(value.toString()), getUser());
        }

        @Inject
        public void setGeoNameAdmin1CodeRepository(GeoNameAdmin1CodeRepository geoNameAdmin1CodeRepository) {
            this.geoNameAdmin1CodeRepository = geoNameAdmin1CodeRepository;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new Admin1CodesImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
