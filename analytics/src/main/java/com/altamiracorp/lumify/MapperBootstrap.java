package com.altamiracorp.lumify;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class MapperBootstrap extends BootstrapBase {
    public MapperBootstrap(Configuration configuration, Properties properties, TaskInputOutputContext attemptContext) {
        super(configuration,properties,attemptContext);
    }

    public static MapperBootstrap create(final TaskInputOutputContext context) {
        checkNotNull(context);

        Configuration configuration = context.getConfiguration();
        TaskInputOutputContext attemptContext = context;

        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : configuration) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return new MapperBootstrap(configuration, properties, attemptContext);
    }
}
