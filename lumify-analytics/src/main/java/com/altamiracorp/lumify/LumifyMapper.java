package com.altamiracorp.lumify;

import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.lumify.core.FrameworkUtils;
import com.altamiracorp.lumify.core.InjectHelper;
import com.altamiracorp.lumify.core.model.GraphSession;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class LumifyMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LumifyMapper.class);
    private boolean failOnFirstError;
    private User user;
    private ModelSession modelSession;
    private GraphSession graphSession;
    private SearchProvider searchProvider;

    @Override
    protected final void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);
        user = new SystemUser();
        failOnFirstError = context.getConfiguration().getBoolean(ConfigurableMapJobBase.FAIL_FIRST_ERROR, false);

        InjectHelper.inject(this, new InjectHelper.ModuleMaker() {
            @Override
            public Module createModule() {
                return MapperBootstrap.create(context);
            }
        });

        FrameworkUtils.initializeFramework(InjectHelper.getInjector(), user);

        try {
            setup(context, InjectHelper.getInjector());
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
        try {
            modelSession.close();
            graphSession.close();
            searchProvider.close();
        } catch (Exception ex) {
            throw new IOException("Could not close", ex);
        }
        super.cleanup(context);
    }

    protected <T> T getAndInjectClassFromConfiguration(Context context, Injector injector, String configName) {
        Class<T> clazz = (Class<T>) context.getConfiguration().getClass(configName, null);
        checkNotNull(clazz, "Could not find class name in configuration with name " + configName);
        return injector.getInstance(clazz);
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }

    @Inject
    public void setGraphSession(GraphSession graphSession) {
        this.graphSession = graphSession;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}
