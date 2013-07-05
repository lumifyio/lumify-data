package com.altamiracorp.reddawn.cmdline;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class FileImportTest {

    private FileImport fileImport;
    private CommandLine mockCl;

    @Before
    public void setUp() {
        fileImport = new FileImport();
        mockCl = mock(CommandLine.class);
        when(mockCl.getOptionValue(anyString())).thenReturn("something");
    }

    @Test
    public void testProcessOptionsAll() throws Exception {
        when(mockCl.getOptionValue("directory")).thenReturn("sample/directory");
        when(mockCl.getOptionValue("source")).thenReturn("Sample Source");
        when(mockCl.getOptionValue("pattern")).thenReturn("Sample Pattern");
        when(mockCl.hasOption(anyString())).thenReturn(true);

        fileImport.processOptions(mockCl);
        assertEquals(fileImport.getSource(), "Sample Source");
        assertEquals(fileImport.getPattern(), "Sample Pattern");
        assertEquals(fileImport.getDirectory(), "sample/directory");
    }

    @Test
    public void testProcessOptionsNoSource() throws Exception {
        when(mockCl.getOptionValue("directory")).thenReturn("sample/directory");
        when(mockCl.getOptionValue("pattern")).thenReturn("Sample Pattern");
        when(mockCl.hasOption("pattern")).thenReturn(true);
        when(mockCl.hasOption("source")).thenReturn(false);

        fileImport.processOptions(mockCl);
        assertEquals(fileImport.getSource(), "File Import");
        assertEquals(fileImport.getPattern(), "Sample Pattern");
        assertEquals(fileImport.getDirectory(), "sample/directory");
    }

    @Test
    public void testProcessOptionsNoPattern() throws Exception {
        when(mockCl.getOptionValue("directory")).thenReturn("sample/directory");
        when(mockCl.getOptionValue("source")).thenReturn("Sample Source");
        when(mockCl.hasOption("source")).thenReturn(true);
        when(mockCl.hasOption("pattern")).thenReturn(false);

        fileImport.processOptions(mockCl);
        assertEquals(fileImport.getSource(), "Sample Source");
        assertEquals(fileImport.getPattern(), "*");
        assertEquals(fileImport.getDirectory(), "sample/directory");
    }

    @Test
    public void testProcessOptionsOnlyDirectory() throws Exception {
        when(mockCl.getOptionValue("directory")).thenReturn("sample/directory");
        when(mockCl.hasOption(anyString())).thenReturn(false);
        fileImport.processOptions(mockCl);
        assertEquals(fileImport.getSource(), "File Import");
        assertEquals(fileImport.getPattern(), "*");
        assertEquals(fileImport.getDirectory(), "sample/directory");
    }

    @Test(expected = Exception.class)
    public void testProcessOptionsInvalid() throws Exception {
        when(mockCl.getOptionValue("directory")).thenReturn(null);
        when(mockCl.hasOption(anyString())).thenReturn(false);
        fileImport.processOptions(mockCl);
    }
}
