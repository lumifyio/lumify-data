package com.altamiracorp.reddawn;

import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class RedDawnMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedDawnMapper.class.getName());

    private RedDawnSession session;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        session = ConfigurableMapJobBase.createRedDawnSession(context);
    }

    @Override
    public final void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {
        try {
            safeMap(key, value, context);
        } catch (Exception e) {
            LOGGER.error("map error", e);
            throw new IOException(e);
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

    public RedDawnSession getSession() {
        return session;
    }
}
