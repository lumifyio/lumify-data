package com.altamiracorp.lumify.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThreadedInputStreamProcess<T, TData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedInputStreamProcess.class.getName());

    private final Thread[] workerThreads;
    private final ThreadedTeeInputStreamWorker<T, TData>[] workers;

    public ThreadedInputStreamProcess(String threadNamePrefix, Collection<ThreadedTeeInputStreamWorker<T, TData>> workersCollection) {
        this.workers = new ThreadedTeeInputStreamWorker[workersCollection.size()];
        this.workerThreads = new Thread[workersCollection.size()];
        int i = 0;
        for (ThreadedTeeInputStreamWorker<T, TData> worker : workersCollection) {
            this.workers[i] = worker;
            this.workerThreads[i] = new Thread(worker);
            String workerName = worker.getName();
            if (workerName == null) {
                workerName = "" + i;
            }
            this.workerThreads[i].setName(threadNamePrefix + "-" + workerName);
            this.workerThreads[i].start();
            i++;
        }
    }

    public List<ThreadedTeeInputStreamWorker.WorkResult<T>> doWork(InputStream source, TData data) throws IOException {
        TeeInputStream teeInputStream = new TeeInputStream(source, this.workers.length);
        try {
            for (int i = 0; i < this.workers.length; i++) {
                this.workers[i].enqueueWork(teeInputStream.getTees()[i], data);
            }
            teeInputStream.loopUntilTeesAreClosed();
        } finally {
            teeInputStream.close();
        }

        ArrayList<ThreadedTeeInputStreamWorker.WorkResult<T>> results = new ArrayList<ThreadedTeeInputStreamWorker.WorkResult<T>>();
        for (ThreadedTeeInputStreamWorker<T, TData> worker : this.workers) {
            results.add(worker.dequeueResult());
        }
        return results;
    }

    public void stop() {
        LOGGER.debug("stopping all workers");
        for (ThreadedTeeInputStreamWorker<T, TData> worker : this.workers) {
            worker.stop();
        }
        for (Thread t : this.workerThreads) {
            try {
                LOGGER.debug("joining thread: " + t);
                t.join();
            } catch (Exception ex) {
                LOGGER.error("Could not join thread: " + t, ex);
            }
        }
    }
}
