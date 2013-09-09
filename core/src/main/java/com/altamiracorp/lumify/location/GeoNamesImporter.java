package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.cmdline.CommandLineBase;
import com.altamiracorp.lumify.model.geoNames.*;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GeoNamesImporter extends CommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesImporter.class.getName());
    private GeoNameRepository geoNameRepository = new GeoNameRepository();
    private GeoNameAdmin1CodeRepository geoNameAdmin1CodeRepository = new GeoNameAdmin1CodeRepository();
    private GeoNameCountryInfoRepository geoNameCountryInfoRepository = new GeoNameCountryInfoRepository();
    private GeoNamePostalCodeRepository geoNamePostalCodeRepository = new GeoNamePostalCodeRepository();
    private String fileName;
    private String admin1CodeFileName;
    private String countryInfoFileName;
    private String postalCodeFileName;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new GeoNamesImporter(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        this.fileName = cmd.getOptionValue("filename");
        this.admin1CodeFileName = cmd.getOptionValue("admin1code");
        this.countryInfoFileName = cmd.getOptionValue("countryinfo");
        this.postalCodeFileName = cmd.getOptionValue("postalcode");
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("filename")
                        .withDescription("The GeoNames file to import")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("admin1code")
                        .withDescription("The GeoNames admin1 code file to import")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("countryinfo")
                        .withDescription("The GeoNames Country Info file to import")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("postalcode")
                        .withDescription("The GeoNames Postal Codes file to import")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        AppSession session = createSession();
        session.getModelSession().initializeTables();

        File f = new File(this.fileName);
        writeFile(session, new FileInputStream(f));

        File admin1CodeFile = new File(this.admin1CodeFileName);
        writeAdmin1CodeFile(session, new FileInputStream(admin1CodeFile));

        File countryInfoFile = new File(this.countryInfoFileName);
        writeCountryInfoFile(session, new FileInputStream(countryInfoFile));

        File postalCodeFile = new File(this.postalCodeFileName);
        writePostalCodeFile (session, new FileInputStream(postalCodeFile));

        session.close();
        return 0;
    }

    private void writeFile(AppSession session, InputStream in) throws IOException, MutationsRejectedException {
        LOGGER.info("Importing GeoNames.");

        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        String line;
        int count = 0;
        List<GeoName> geoNames = new ArrayList<GeoName>();
        while ((line = br.readLine()) != null) {
            geoNames.add(lineToGeoName(line));
            count++;
            if ((count % 1000) == 0) {
                geoNameRepository.saveMany(session.getModelSession(), geoNames);
                geoNames.clear();
                LOGGER.info("Imported " + count + " of ~8500000  items.");
            }
        }
        geoNameRepository.saveMany(session.getModelSession(), geoNames);
        geoNames.clear();
        LOGGER.info("Imported " + count + " of ~8500000  items.");

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
     * 11 admin2 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
     * 12 admin3 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
     * 13 admin4 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
     * 14 population        : bigint (8 byte int)
     * 15 elevation         : in meters, integer
     * 16 dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
     * 17 timezone          : the timezone id (see file timeZone.txt) varchar(40)
     * 18 modification date : date of last modification in yyyy-MM-dd format
     */
    public GeoName lineToGeoName(String line) {
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
        GeoNameMetadata metadata = geoName.getMetadata()
                .setName(name)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setPopulation(population);

        if (StringUtils.isNotBlank(featureClass)) {
            metadata.setFeatureClass(featureClass);
        }
        if (StringUtils.isNotBlank(featureCode)) {
            metadata.setFeatureCode(featureCode);
        }
        if (StringUtils.isNotBlank(countryCode)) {
            metadata.setCountryCode(countryCode);
        }
        if (StringUtils.isNotBlank(alternateCountryCodes)) {
            metadata.setAlternateCountryCodes(alternateCountryCodes);
        }
        if (StringUtils.isNotBlank(admin1Code)) {
            metadata.setAdmin1Code(admin1Code);
        }
        if (StringUtils.isNotBlank(admin2Code)) {
            metadata.setAdmin2Code(admin2Code);
        }
        if (StringUtils.isNotBlank(admin3Code)) {
            metadata.setAdmin3Code(admin3Code);
        }
        if (StringUtils.isNotBlank(admin4Code)) {
            metadata.setAdmin4Code(admin4Code);
        }
        return geoName;
    }


    private void writeAdmin1CodeFile(AppSession session, InputStream in) throws IOException, MutationsRejectedException {
        LOGGER.info("Importing GeoNames Admin1 Codes.");

        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        String line;
        int count = 0;
        List<GeoNameAdmin1Code> admin1Codes = new ArrayList<GeoNameAdmin1Code>();
        while ((line = br.readLine()) != null) {
            admin1Codes.add(lineToAdmin1Code(line));
            count++;
            if ((count % 1000) == 0) {
                geoNameAdmin1CodeRepository.saveMany(session.getModelSession(), admin1Codes);
                admin1Codes.clear();
                LOGGER.info("Imported " + count + " of ~4000  items.");
            }
        }
        geoNameAdmin1CodeRepository.saveMany(session.getModelSession(), admin1Codes);
        admin1Codes.clear();
        LOGGER.info("Imported " + count + " of ~4000  items.");

        LOGGER.info("Saved " + count + " records");
    }

    public GeoNameAdmin1Code lineToAdmin1Code(String line) {
        String[] parts = line.split("\t");
        String keyString = parts[0];
        String title = parts[1];

        String[] keyStringParts = keyString.split("\\.");
        String countryCode = keyStringParts[0];
        String admin1Code = keyStringParts[1];

        GeoNameAdmin1CodeRowKey key = new GeoNameAdmin1CodeRowKey(countryCode, admin1Code);
        GeoNameAdmin1Code result = new GeoNameAdmin1Code(key);
        result.getMetadata().setTitle(title);
        return result;
    }

    private void writeCountryInfoFile(AppSession session, InputStream in) throws IOException, MutationsRejectedException {
        LOGGER.info("Importing GeoNames Country Info.");

        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        String line;
        int count = 0;
        List<GeoNameCountryInfo> countryInfos = new ArrayList<GeoNameCountryInfo>();
        while ((line = br.readLine()) != null) {
            GeoNameCountryInfo countryInfo = lineToCountryInfo(line);
            if (countryInfo == null) {
                continue;
            }
            countryInfos.add(countryInfo);
            count++;
            if ((count % 100) == 0) {
                geoNameCountryInfoRepository.saveMany(session.getModelSession(), countryInfos);
                countryInfos.clear();
                LOGGER.info("Imported " + count + " of ~400  items.");
            }
        }
        geoNameCountryInfoRepository.saveMany(session.getModelSession(), countryInfos);
        countryInfos.clear();
        LOGGER.info("Imported " + count + " of ~400  items.");

        LOGGER.info("Saved " + count + " records");
    }

    public GeoNameCountryInfo lineToCountryInfo(String line) {
        if (line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split("\t");
        String countryCode = parts[0];
        String title = parts[4];

        GeoNameCountryInfoRowKey key = new GeoNameCountryInfoRowKey(countryCode);
        GeoNameCountryInfo result = new GeoNameCountryInfo(key);
        result.getMetadata().setTitle(title);
        return result;
    }

    private void writePostalCodeFile(AppSession session, FileInputStream in) throws IOException, MutationsRejectedException {
        LOGGER.info("Importing GeoNames Postal Code Info.");

        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        String line;
        int count = 0;
        List<GeoNamePostalCode> postalCodes = new ArrayList<GeoNamePostalCode>();
        while ((line = br.readLine()) != null) {
            GeoNamePostalCode postalCode = lineToPostalCode(line);
            if (postalCode == null) {
                continue;
            }
            postalCodes.add(postalCode);
            count++;
            if ((count % 100) == 0) {
                geoNamePostalCodeRepository.saveMany(session.getModelSession(), postalCodes);
                postalCodes.clear();
                LOGGER.info("Imported " + count + " of ~43630  items.");
            }
        }
        geoNamePostalCodeRepository.saveMany(session.getModelSession(), postalCodes);
        postalCodes.clear();
        LOGGER.info("Imported " + count + " of ~43630  items.");

        LOGGER.info("Saved " + count + " records");
    }

    public GeoNamePostalCode lineToPostalCode(String line) {
        if (line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split("\t");
        String countryCode = parts[0];
        String postalCodeString = parts[1];
        String placeName = parts[2];
        String admin1Name = parts[3];
        String latitude = parts[9];
        String longitude = parts[10];

        GeoNamePostalCodeRowKey key = new GeoNamePostalCodeRowKey(countryCode, postalCodeString);
        GeoNamePostalCode postalCode = new GeoNamePostalCode(key);
        postalCode.getMetadata().setPlaceName(placeName);
        postalCode.getMetadata().setAdmin1Code(admin1Name);
        postalCode.getMetadata().setLatitude(Double.parseDouble(latitude));
        postalCode.getMetadata().setLongitude(Double.parseDouble(longitude));

        return postalCode;
    }
}
