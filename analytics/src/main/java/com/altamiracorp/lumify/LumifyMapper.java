package com.altamiracorp.lumify;

import com.altamiracorp.lumify.core.user.User;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class LumifyMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LumifyMapper.class.getName());

    private AppSession session;
    private boolean failOnFirstError;
    private User user = new User();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        session = ConfigurableMapJobBase.createAppSession(context);
        failOnFirstError = context.getConfiguration().getBoolean("failOnFirstError", false);
    }

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

    public AppSession getSession() {
        return session;
    }
}
