package com.altamiracorp.lumify.tools.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TimePeriodTest {
    @Test
    public void testToString() {
        assertEquals("100ms", new TimePeriod(100).toString());
        assertEquals("5.05s", new TimePeriod((5 * 1000) + 45).toString());
        assertEquals("8m 5.05s", new TimePeriod((8 * 60 * 1000) + (5 * 1000) + 45).toString());
        assertEquals("22h 8m", new TimePeriod((22 * 60 * 60 * 1000) + (8 * 60 * 1000) + (5 * 1000) + 45).toString());
        assertEquals("8d 22h", new TimePeriod((8 * 24 * 60 * 60 * 1000) + (22 * 60 * 60 * 1000) + (8 * 60 * 1000) + (5 * 1000) + 45).toString());
    }
}
