package com.altamiracorp.lumify.core.util;

import java.io.IOException;
import java.io.InputStream;

public class TeeInputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;
    private final InputStream source;
    private final MyInputStream[] tees;
    private final byte[] cyclicBuffer;
    private int cyclicBufferOffsetIndex; /* Index into the buffer for which cyclicBufferOffset represents */
    private long cyclicBufferOffset; /* Offset of the source input stream that begins the cyclic buffer */
    private int cyclicBufferValidSize; /* number of bytes in the cyclicBuffer which are valid */
    private final Object cyclicBufferLock = new Object();
    private boolean sourceComplete;

    public TeeInputStream(InputStream source, int splits) throws IOException {
        this(source, splits, DEFAULT_BUFFER_SIZE);
    }

    public TeeInputStream(InputStream source, int splits, int bufferSize) throws IOException {
        this.source = source;
        this.cyclicBuffer = new byte[bufferSize];
        this.cyclicBufferOffsetIndex = 0;
        this.cyclicBufferOffset = 0;
        this.cyclicBufferValidSize = 0;
        this.sourceComplete = false;
        this.tees = new MyInputStream[splits];
        for (int i = 0; i < this.tees.length; i++) {
            this.tees[i] = new MyInputStream();
        }
    }

    public InputStream[] getTees() {
        return this.tees;
    }

    private boolean isClosed(int idx) {
        return this.tees[idx].isClosed();
    }

    public void close() throws IOException {
        for (InputStream tee : this.tees) {
            tee.close();
        }
    }

    public void loopUntilTeesAreClosed() throws Exception {
        boolean allClosed = false;
        while (!allClosed) {
            allClosed = true;
            for (int i = 0; i < this.tees.length; i++) {
                if (!isClosed(i)) {
                    allClosed = false;
                    break;
                }
            }
            loop();
        }
    }

    protected void loop() throws Exception {
        synchronized (cyclicBufferLock) {

            // TODO: shouldn't need to do this each loop. Should really only be done if a read occurs.
            updateOffsets();

            if (!sourceComplete && cyclicBufferValidSize < cyclicBuffer.length) {
                int readOffset = cyclicBufferOffsetIndex + cyclicBufferValidSize;
                int readLen = cyclicBuffer.length - cyclicBufferValidSize;

                // read from readOffset to end of buffer
                int partialRedLen = Math.min(cyclicBuffer.length - readOffset, readLen);
                if (partialRedLen > 0) {
                    int read = source.read(cyclicBuffer, readOffset, partialRedLen);
                    if (read == -1) {
                        sourceComplete = true;
                    } else {
                        cyclicBufferValidSize += read;
                        readLen -= read;
                        readOffset += read;
                    }
                }

                // wrap and read from the beginning of the buffer
                if (!sourceComplete && readLen > 0 && readOffset >= cyclicBuffer.length) {
                    readOffset = readOffset % cyclicBuffer.length;
                    int read = source.read(cyclicBuffer, readOffset, readLen);
                    if (read == -1) {
                        sourceComplete = true;
                    } else {
                        cyclicBufferValidSize += read;
                    }
                }

                cyclicBufferLock.notifyAll();
            } else {
                cyclicBufferLock.wait(100);
            }
        }
    }

    private void updateOffsets() {
        synchronized (cyclicBufferLock) {
            long lowestOffset = findLowestTeeOffset();
            if (lowestOffset > cyclicBufferOffset) {
                int delta = (int) (lowestOffset - cyclicBufferOffset);
                cyclicBufferOffset += delta;
                cyclicBufferOffsetIndex += delta;
                cyclicBufferOffsetIndex = cyclicBufferOffsetIndex % cyclicBuffer.length;
                cyclicBufferValidSize -= delta;
            }
        }
    }

    private long findLowestTeeOffset() {
        synchronized (cyclicBufferLock) {
            long lowestOffset = Long.MAX_VALUE;
            for (MyInputStream tee : tees) {
                if (tee.offset < lowestOffset) {
                    lowestOffset = tee.offset;
                }
            }
            return lowestOffset;
        }
    }

    public int getMaxNonblockingReadLength(int teeIndex) {
        return tees[teeIndex].getMaxNonblockingReadLength();
    }

    private class MyInputStream extends InputStream {
        private boolean closed;
        private long offset;

        public MyInputStream() {
            this.closed = false;
            this.offset = 0;
        }

        @Override
        public int read() throws IOException {
            synchronized (TeeInputStream.this.cyclicBufferLock) {
                if (closed) {
                    return -1;
                }

                int result = readInternal();
                if (result != -1) {
                    this.offset++;
                }
                TeeInputStream.this.cyclicBufferLock.notifyAll();
                return result;
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            synchronized (TeeInputStream.this.cyclicBufferLock) {
                if (closed) {
                    return -1;
                }
                if (b.length == 0 || len == 0) {
                    return 0;
                }

                int readLength = readInternal(b, off, len);
                if (readLength != -1) {
                    this.offset += readLength;
                }
                TeeInputStream.this.cyclicBufferLock.notifyAll();
                return readLength;
            }
        }

        private int readInternal() throws IOException {
            synchronized (cyclicBufferLock) {
                if (offset < cyclicBufferOffset) {
                    throw new IOException("attempting to read previous data is not permitted. offset: " + offset + ", cyclicBufferOffset: " + cyclicBufferOffset);
                }
                while (getMaxNonblockingReadLength() <= 0) {
                    if (sourceComplete) {
                        return -1;
                    }
                    try {
                        cyclicBufferLock.wait();
                    } catch (InterruptedException e) {
                        throw new IOException("Cyclic buffer wait failed", e);
                    }
                }
                int readOffset = (int) (offset - cyclicBufferOffset + cyclicBufferOffsetIndex) % cyclicBuffer.length;
                return cyclicBuffer[readOffset];
            }
        }

        private int readInternal(byte[] b, int off, int len) throws IOException {
            synchronized (cyclicBufferLock) {
                if (offset < cyclicBufferOffset) {
                    throw new IOException("attempting to read previous data is not permitted. offset: " + offset + ", cyclicBufferOffset: " + cyclicBufferOffset);
                }
                while (getMaxNonblockingReadLength() <= 0) {
                    if (sourceComplete) {
                        return -1;
                    }
                    try {
                        cyclicBufferLock.wait();
                    } catch (InterruptedException e) {
                        throw new IOException("Cyclic buffer wait failed", e);
                    }
                }
                int readOffset = (int) (offset - cyclicBufferOffset + cyclicBufferOffsetIndex) % cyclicBuffer.length;
                int readLen = Math.min(len, getMaxNonblockingReadLength());
                int bytesRead = 0;

                // read from readOffset to end of buffer
                int partialReadLen = Math.min(cyclicBuffer.length - readOffset, readLen);
                if (partialReadLen > 0) {
                    System.arraycopy(cyclicBuffer, readOffset, b, off, partialReadLen);
                    readLen -= partialReadLen;
                    off += partialReadLen;
                    readOffset += partialReadLen;
                    bytesRead += partialReadLen;
                }

                // read from start of buffer to readLen
                if (readLen > 0) {
                    readOffset = readOffset % cyclicBuffer.length;
                    System.arraycopy(cyclicBuffer, readOffset, b, off, readLen);
                    bytesRead += readLen;
                }

                return bytesRead;
            }
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                synchronized (TeeInputStream.this.cyclicBufferLock) {
                    this.closed = true;
                    TeeInputStream.this.cyclicBufferLock.notifyAll();
                }
            }
        }

        public boolean isClosed() {
            return this.closed;
        }

        public int getMaxNonblockingReadLength() {
            synchronized (TeeInputStream.this.cyclicBufferLock) {
                return (int) (cyclicBufferValidSize - (offset - cyclicBufferOffset));
            }
        }
    }
}
