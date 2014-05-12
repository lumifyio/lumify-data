package io.lumify.gtd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lumify.core.cmdline.CommandLineBase;
import io.lumify.core.ingest.term.extraction.TermExtractionResult;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.mapping.DocumentMapping;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * This application reads a specified Global Terrorism Database CSV file and loads
 * the entities and relationships it contains into the configured Lumify instance.
 */
public class GtdIngest extends CommandLineBase {
    /**
     * The logger.
     */
    private static final LumifyLogger LOG = LumifyLoggerFactory.getLogger(GtdIngest.class);

    /**
     * The GTD input file option.
     */
    private static final String GTD_FILE_OPTION = "inputFile";

    /**
     * The GTD mapping path option.
     */
    private static final String GTD_MAPPING_OPTION = "mapping";

    /**
     * The GTD document mapping.
     */
    private static final String DEFAULT_GTD_MAPPING_FILE = "gtd.mapping.json";

    /**
     * The shared object mapper.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * The configured input file.
     */
    private File inputFile;

    /**
     * The configured DocumentMapping.
     */
    private DocumentMapping mapping;

    public static void main(String[] args) throws Exception {
        int res = new GtdIngest().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(final CommandLine cmd) throws Exception {
        Reader gtdIn = new InputStreamReader(new FileInputStream(inputFile), Charset.forName("UTF-8"));
        Iterator<TermExtractionResult> termIter = mapping.mapDocumentElements(gtdIn, "gtd-ingest", null, null);
        while (termIter.hasNext()) {
            LOG.info("Found: %s", termIter);
        }
        return 0;
    }

    @Override
    protected Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption(OptionBuilder
                .withLongOpt(GTD_FILE_OPTION)
                .withDescription("The GTD CSV file to import.")
                .hasArg()
                .isRequired()
                .create('i'));

        opts.addOption(OptionBuilder
                .withLongOpt(GTD_MAPPING_OPTION)
                .withDescription("The path to the GTD mapping description.")
                .hasArg()
                .create('m'));

        return opts;
    }

    @Override
    protected void processOptions(final CommandLine cmd) throws Exception {
        super.processOptions(cmd);

        String path = cmd.getOptionValue(GTD_FILE_OPTION);
        if (path == null) {
            throw new IllegalStateException("Input file (--inputFile) is required.");
        }
        inputFile = new File(path);
        if (!(inputFile.isFile() && inputFile.canRead())) {
            throw new IllegalStateException(String.format("Unable to read input file: %s", inputFile.getAbsolutePath()));
        }

        String mappingPath = cmd.getOptionValue(GTD_MAPPING_OPTION);
        InputStream mappingIn;
        if (mappingPath != null) {
            mappingIn = new FileInputStream(mappingPath);
        } else {
            mappingIn = ClassLoader.getSystemResourceAsStream(DEFAULT_GTD_MAPPING_FILE);
        }
        mapping = JSON_MAPPER.readValue(mappingIn, DocumentMapping.class);
    }
}
