package com.altamiracorp.lumify.core.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TeeInputStreamTest {
    @Test
    public void testReads() throws IOException {
        byte[] temp = new byte[100];
        int readLen;
        byte[] data = createMockData(10);
        InputStream source = new ByteArrayInputStream(data);
        TeeInputStream in = new TeeInputStream(source, 2);
        InputStream[] tees = in.getTees();

        assertEquals(0, tees[0].read());
        assertEquals(0, tees[1].read());

        readLen = tees[0].read(temp, 0, 5);
        assertEquals(5, readLen);
        assertArrayEquals(Arrays.copyOfRange(data, 1, 6), Arrays.copyOfRange(temp, 0, 5));

        readLen = tees[0].read(temp);
        assertEquals(4, readLen);
        assertArrayEquals(Arrays.copyOfRange(data, 6, 10), Arrays.copyOfRange(temp, 0, 4));

        readLen = tees[1].read(temp);
        assertEquals(9, readLen);
        assertArrayEquals(Arrays.copyOfRange(data, 1, 10), Arrays.copyOfRange(temp, 0, 9));

        in.close();
    }

    @Test
    public void testLoopUntilTeesAreClosed() throws Exception {
        final byte[] data = createMockData(10);
        InputStream source = new ByteArrayInputStream(data);
        TeeInputStream in = new TeeInputStream(source, 2) {
            int loopCount = 0;

            @Override
            protected void loop() throws Exception {
                byte[] temp = new byte[10];
                int read;

                super.loop();
                switch (loopCount) {
                    case 0:
                        read = getTees()[0].read(temp);
                        assertEquals(10, read);
                        assertArrayEquals(data, temp);
                        break;
                    case 1:
                        read = getTees()[1].read(temp);
                        assertEquals(10, read);
                        assertArrayEquals(data, temp);
                        break;
                    case 2:
                        getTees()[0].close();
                        break;
                    case 3:
                        getTees()[1].close();
                        break;
                }
                loopCount++;
            }
        };

        in.loopUntilTeesAreClosed();
    }

    private byte[] createMockData(int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) i;
        }
        return data;
    }
}
