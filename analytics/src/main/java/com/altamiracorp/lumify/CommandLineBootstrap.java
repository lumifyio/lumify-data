package com.altamiracorp.lumify;

import java.util.Properties;

public class CommandLineBootstrap extends BootstrapBase {
    protected CommandLineBootstrap(Properties properties) {
        super(properties, null);
    }

    public static CommandLineBootstrap create(Properties properties) {
        return new CommandLineBootstrap(properties);
    }
}
