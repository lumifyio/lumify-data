/*
 * Copyright 2013 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.twitter.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseFileProcessingBolt;
import com.altamiracorp.lumify.storm.file.FileMetadata;
import com.altamiracorp.lumify.twitter.TwitterConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.codehaus.plexus.util.FileUtils;

/**
 * This bolt outputs each line of an input file it receives as
 * a new Tuple with the contents of that line in the JSON_OUTPUT_FIELD
 * field.
 */
public class TwitterFileProcessingBolt extends BaseFileProcessingBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TwitterFileProcessingBolt.class);

    /**
     * GZIP extension.
     */
    private static final String GZIP_EXTENSION = ".gz";

    /**
     * Simple JSON object regex.  This only validates that a particular String
     * starts and ends with curly braces: ^\s*{.*}\s*$
     */
    private static final Pattern JSON_OBJECT = Pattern.compile("^\\s*\\{.*\\}\\s*$");

    @Override
    protected void safeExecute(final Tuple input) throws Exception {
        FileMetadata fileMetadata = getFileMetadata(input);
        LOGGER.info("Processing file: %s (mimeType: %s)",
                fileMetadata.getFileName(), fileMetadata.getMimeType());
        processFile(input, fileMetadata.getFileName());
    }

    /**
     * Reads input files, emitting Tuples containing raw Twitter Tweet JSON
     * for each line of each input file.  This method skips any files whose
     * first line does not appear to contain a JSON object, starting and ending
     * with curly braces (e.g. {...}).  All archives and directories are recursively
     * processed.
     *
     * @param rootTuple the root tuple that new tuples will be anchored to
     * @param filename  the path to the file to process
     * @throws Exception if errors occur while processing the file
     */
    protected void processFile(final Tuple rootTuple, final String filename) throws Exception {
        LOGGER.info("Processing file: %s", filename);
        FileMetadata fileMd = new FileMetadata()
                .setFileName(filename)
                .setMimeType(getMimeType(filename));
        String filenameNoDate = fileMd.getFileNameWithoutDateSuffix().toLowerCase();
        if (isArchive(filenameNoDate)) {
            processArchive(rootTuple, fileMd);
        } else {
            OutputCollector collector = getCollector();
            InputStream is = getInputStream(filename, null);
            // if the file is a gzip compressed file, uncompress before reading
            if (filenameNoDate.endsWith(GZIP_EXTENSION)) {
                is = new GZIPInputStream(is);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, TwitterConstants.TWITTER_CHARSET));
            try {
                String tweetJson = reader.readLine();
                // only process this file if the first line appears to be a JSON string
                if (tweetJson != null && JSON_OBJECT.matcher(tweetJson).matches()) {
                    while (tweetJson != null) {
                        collector.emit(rootTuple, new Values(tweetJson.trim()));
                        tweetJson = reader.readLine();
                    }
                }
            } finally {
                reader.close();
            }
        }
    }

    /**
     * Extracts an archive file to a local temporary directory and processes
     * each contained file individually. The temporary directory will be deleted
     * when this method is complete.
     *
     * @param rootTuple the root tuple that will be used to anchor new tuples
     * @param archiveMd the file metadata for the archive file
     * @throws Exception if an error occurs processing the archive
     */
    protected void processArchive(final Tuple rootTuple, final FileMetadata archiveMd) throws Exception {
        File archiveDir = null;
        try {
            archiveDir = extractArchive(archiveMd);
            LOGGER.info("Extracted archive [%s] to temporary directory: %s",
                    archiveMd.getFileName(), archiveDir.getAbsolutePath());
            processDirectory(rootTuple, archiveDir);
        } finally {
            if (archiveDir != null) {
                FileUtils.deleteDirectory(archiveDir);
            }
        }
    }

    /**
     * Processes a local directory of files, recursively processing any subdirectories.
     *
     * @param rootTuple the root tuple to anchor new tuples to
     * @param directory the local directory to process
     * @throws Exception if an error occurs while processing the directory
     */
    protected void processDirectory(final Tuple rootTuple, final File directory) throws Exception {
        LOGGER.info("Processing files in directory: %s", directory.getAbsolutePath());
        for (File entry : directory.listFiles()) {
            if (entry.isDirectory()) {
                processDirectory(rootTuple, entry);
            } else {
                processFile(rootTuple, entry.getAbsolutePath());
            }
        }
    }
}
