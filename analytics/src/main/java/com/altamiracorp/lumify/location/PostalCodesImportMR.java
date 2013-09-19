package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.model.geoNames.GeoNamePostalCode;
import com.altamiracorp.lumify.model.geoNames.GeoNamePostalCodeRepository;
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

public class PostalCodesImportMR extends ConfigurableMapJobBase {

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        return TextInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return PostalCodesImportMapper.class;
    }

    public static class PostalCodesImportMapper extends LumifyMapper<LongWritable, Text, NullWritable, NullWritable> {
        private GeoNamesImporter geoNamesImporter = new GeoNamesImporter();
        private GeoNamePostalCodeRepository geoNamePostalCodeRepository;

        @Override
        protected void setup(Context context, Injector injector) throws IOException, InterruptedException {
        }

        @Override
        protected void safeMap(LongWritable key, Text value, Context context) throws Exception {
            geoNamePostalCodeRepository.save(geoNamesImporter.lineToPostalCode(value.toString()), getUser());
        }

        @Inject
        public void setGeoNamePostalCodeRepository(GeoNamePostalCodeRepository geoNamePostalCodeRepository) {
            this.geoNamePostalCodeRepository = geoNamePostalCodeRepository;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new PostalCodesImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
