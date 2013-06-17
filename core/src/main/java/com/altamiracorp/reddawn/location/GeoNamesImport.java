package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.altamiracorp.reddawn.model.geoNames.GeoName;
import com.altamiracorp.reddawn.model.geoNames.GeoNameRepository;
import com.altamiracorp.reddawn.model.geoNames.GeoNameRowKey;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

public class GeoNamesImport extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesImport.class.getName());
    private GeoNameRepository geoNameRepository = new GeoNameRepository();
    private String fileName;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new GeoNamesImport(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected void processOptions(CommandLine cmd) {
        super.processOptions(cmd);
        this.fileName = cmd.getOptionValue("filename");
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withArgName("d")
                        .withLongOpt("filename")
                        .withDescription("The GeoNames file to import")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        RedDawnSession redDawnSession = createRedDawnSession();
        redDawnSession.getModelSession().initializeTables();

        File f = new File(this.fileName);
        writeFile(redDawnSession, new FileInputStream(f));

        redDawnSession.close();
        return 0;
    }

    private void writeFile(RedDawnSession redDawnSession, InputStream in) throws IOException, MutationsRejectedException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            writeLine(redDawnSession, line, count);
            count++;
        }
        LOGGER.info("Saved " + count + " records");
    }

    /**
     * The main 'geoname' table has the following fields :
     * ---------------------------------------------------
     * 0  geonameid         : integer id of record in geonames database
     * 1  name              : name of geographical point (utf8) varchar(200)
     * 2  asciiname         : name of geographical point in plain ascii characters, varchar(200)
     * 3  alternatenames    : alternatenames, comma separated varchar(5000)
     * 4  latitude          : latitude in decimal degrees (wgs84)
     * 5  longitude         : longitude in decimal degrees (wgs84)
     * 6  feature class     : see http://www.geonames.org/export/codes.html, char(1)
     * 7  feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
     * 8  country code      : ISO-3166 2-letter country code, 2 characters
     * 9  cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
     * 10 admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
     * 11 admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
     * 12 admin3 code       : code for third level administrative division, varchar(20)
     * 13 admin4 code       : code for fourth level administrative division, varchar(20)
     * 14 population        : bigint (8 byte int)
     * 15 elevation         : in meters, integer
     * 16 dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
     * 17 timezone          : the timezone id (see file timeZone.txt) varchar(40)
     * 18 modification date : date of last modification in yyyy-MM-dd format
     */
    private void writeLine(RedDawnSession redDawnSession, String line, int count) {
        String[] parts = line.split("\t");
        String id = parts[0];
        String name = parts[1];
        String asciiname = parts[2];
        String alternatenames = parts[3];
        double latitude = Double.parseDouble(parts[4]);
        double longitude = Double.parseDouble(parts[5]);
        String featureClass = parts[6];
        String featureCode = parts[7];
        String countryCode = parts[8];
        String alternateCountryCodes = parts[9];
        String admin1Code = parts[10];
        String admin2Code = parts[11];
        String admin3Code = parts[12];
        String admin4Code = parts[13];
        long population = Long.parseLong(parts[14]);
        String elevation = parts[15];
        String dem = parts[16];
        String timezone = parts[17];
        String modificationDate = parts[18];

        GeoNameRowKey rowKey = new GeoNameRowKey(name, id);
        GeoName geoName = new GeoName(rowKey);
        geoName.getMetadata()
                .setName(name)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setPopulation(population);
        geoNameRepository.save(redDawnSession.getModelSession(), geoName);
        LOGGER.info("[" + count + "] Saving " + name + " (" + latitude + "," + longitude + ")");
    }
}
