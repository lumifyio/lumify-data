package com.altamiracorp.lumify.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

public abstract class ThreadedTeeInputStreamWorker<T, TData> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedTeeInputStreamWorker.class.getName());
    private boolean stopped;
    private Queue<Work> workItems = new LinkedList<Work>();
    private Queue<WorkResult<T>> workResults = new LinkedList<WorkResult<T>>();

    @Override
    public final void run() {
        stopped = false;
        try {
            while (!stopped) {
                if (workItems.size() == 0) {
                    Thread.sleep(100);
                    continue;
                }
                Work work = workItems.remove();
                InputStream in = work.getIn();
                try {
                    T result = doWork(in, work.getData());
                    workResults.add(new WorkResult<T>(result, null));
                } catch (Exception ex) {
                    workResults.add(new WorkResult<T>(null, ex));
                } finally {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        workResults.add(new WorkResult<T>(null, ex));
                    }
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("thread was interrupted", ex);
        }
    }

    protected abstract T doWork(InputStream work, TData data) throws Exception;

    public void enqueueWork(InputStream in, TData data) {
        workItems.add(new Work(in, data));
    }

    public WorkResult<T> dequeueResult() {
        return workResults.remove();
    }

    public void stop() {
        stopped = true;
    }

    public String getName() {
        return null;
    }

    private class Work {
        private final InputStream in;
        private final TData data;

        public Work(InputStream in, TData data) {
            this.in = in;
            this.data = data;
        }

        private InputStream getIn() {
            return in;
        }

        private TData getData() {
            return data;
        }
    }

    public static class WorkResult<T> {
        private final T result;
        private final Exception error;

        public WorkResult(T result, Exception error) {
            this.result = result;
            this.error = error;
        }

        public Exception getError() {
            return error;
        }

        public T getResult() {
            return result;
        }
    }
}
