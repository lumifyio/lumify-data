package com.altamiracorp.lumify;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class LumifyMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LumifyMapper.class.getName());

    private boolean failOnFirstError;
    private User user = new User();

    @Override
    protected final void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        failOnFirstError = context.getConfiguration().getBoolean("failOnFirstError", false);

        final Injector injector = Guice.createInjector(new MapperBootstrap(context));

        injector.injectMembers(this);
        try {
            setup(context, injector);
        } catch (Exception ex) {
            throw new IOException("Could not setup", ex);
        }
    }

    protected abstract void setup(Context context, Injector injector) throws Exception;

    protected User getUser() {
        return user;
    }

    @Override
    public final void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {
        try {
            safeMap(key, value, context);
        } catch (Exception e) {
            LOGGER.error("map error", e);
            if (failOnFirstError) {
                throw new IOException(e);
            }
        }
    }

    protected abstract void safeMap(KEYIN key, VALUEIN value, Context context) throws Exception;

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        if (session != null) {
            session.close();
        }
        super.cleanup(context);
    }

    protected <T> T getAndInjectClassFromConfiguration(Context context, Injector injector, String configName) {
        Class<T> clazz = (Class<T>) context.getConfiguration().getClass(configName, null);
        checkNotNull(clazz, "Could not find class name in configuration with name " + configName);
        return injector.getInstance(clazz);
    }
}
