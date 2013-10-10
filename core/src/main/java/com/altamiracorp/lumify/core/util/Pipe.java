package com.altamiracorp.lumify.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pipe {
    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    public static void pipe(final InputStream in, final OutputStream out, final ExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int read;
                byte[] buffer = new byte[1 * 1024 * 1024];
                try {
                    while ((read = in.read(buffer)) > 0) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    exceptionHandler.handle(e);
                }
            }
        });
    }

    public static interface ExceptionHandler {
        void handle(IOException e);
    }
}
