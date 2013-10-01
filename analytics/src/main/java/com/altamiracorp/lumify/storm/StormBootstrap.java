package com.altamiracorp.lumify.storm;

import com.altamiracorp.lumify.BootstrapBase;
import com.google.inject.Module;

import java.util.Map;
import java.util.Properties;

public class StormBootstrap extends BootstrapBase {
    protected StormBootstrap(Properties properties) {
        super(properties, null);
    }

    public static Module create(Map stormConf) {
        Properties props = new Properties();
        for (Object entryObj : stormConf.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            if (entry.getValue() != null) {
                props.put(entry.getKey(), entry.getValue());
            }
        }
        return new StormBootstrap(props);
    }
}
