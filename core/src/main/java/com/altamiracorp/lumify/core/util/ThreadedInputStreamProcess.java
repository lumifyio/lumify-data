package com.altamiracorp.lumify.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThreadedInputStreamProcess<TResult, TData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedInputStreamProcess.class.getName());

    private final Thread[] workerThreads;
    private final ThreadedTeeInputStreamWorker<TResult, TData>[] workers;
    private final String[] workerNames;

    public ThreadedInputStreamProcess(String threadNamePrefix, Collection<ThreadedTeeInputStreamWorker<TResult, TData>> workersCollection) {
        this.workers = new ThreadedTeeInputStreamWorker[workersCollection.size()];
        this.workerThreads = new Thread[workersCollection.size()];
        this.workerNames = new String[workersCollection.size()];
        int i = 0;
        for (ThreadedTeeInputStreamWorker<TResult, TData> worker : workersCollection) {
            this.workers[i] = worker;
            this.workerThreads[i] = new Thread(worker);
            String workerName = worker.getName();
            if (workerName == null) {
                workerName = "" + i;
            }
            this.workerNames[i] = workerName;
            this.workerThreads[i].setName(threadNamePrefix + "-" + workerName);
            this.workerThreads[i].start();
            i++;
        }
    }

    public List<ThreadedTeeInputStreamWorker.WorkResult<TResult>> doWork(InputStream source, TData data) throws Exception {
        TeeInputStream teeInputStream = new TeeInputStream(source, this.workerNames);
        try {
            for (int i = 0; i < this.workers.length; i++) {
                this.workers[i].enqueueWork(teeInputStream.getTees()[i], data);
            }
            teeInputStream.loopUntilTeesAreClosed();
        } finally {
            teeInputStream.close();
        }

        ArrayList<ThreadedTeeInputStreamWorker.WorkResult<TResult>> results = new ArrayList<ThreadedTeeInputStreamWorker.WorkResult<TResult>>();
        for (ThreadedTeeInputStreamWorker<TResult, TData> worker : this.workers) {
            results.add(worker.dequeueResult());
        }
        return results;
    }

    public void stop() {
        LOGGER.debug("stopping all workers");
        for (ThreadedTeeInputStreamWorker<TResult, TData> worker : this.workers) {
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
