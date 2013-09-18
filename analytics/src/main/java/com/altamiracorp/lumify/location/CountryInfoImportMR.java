package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.model.geoNames.GeoNameCountryInfo;
import com.altamiracorp.lumify.model.geoNames.GeoNameCountryInfoRepository;
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

public class CountryInfoImportMR extends ConfigurableMapJobBase {
    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        return TextInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return CountryInfoImportMapper.class;
    }

    public static class CountryInfoImportMapper extends LumifyMapper<LongWritable, Text, NullWritable, NullWritable> {
        private GeoNameCountryInfoRepository geoNameCountryInfoRepository = new GeoNameCountryInfoRepository();
        private GeoNamesImporter geoNamesImporter = new GeoNamesImporter();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            getSession().getModelSession().initializeTable(GeoNameCountryInfo.TABLE_NAME);
        }

        @Override
        protected void safeMap(LongWritable key, Text value, Context context) throws Exception {
            geoNameCountryInfoRepository.save(geoNamesImporter.lineToCountryInfo(value.toString()));
        }

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new CountryInfoImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
