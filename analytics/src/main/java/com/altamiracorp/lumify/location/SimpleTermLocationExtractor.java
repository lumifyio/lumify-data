package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.geoNames.*;
import com.altamiracorp.lumify.model.termMention.TermMention;

import java.util.regex.Pattern;

public class SimpleTermLocationExtractor {
    private static final String POSTAL_CODE_REGEX = "^\\d{5}$|^\\d{5}-\\d{4}$"; //US zip code
    private static final Long POSTAL_CODE_POPULATION = 1000000L; // in the absence of population data, let's make it up!
    private GeoNameAdmin1CodeRepository geoNameAdmin1CodeRepository = new GeoNameAdmin1CodeRepository();
    private GeoNameCountryInfoRepository geoNameCountryInfoRepository = new GeoNameCountryInfoRepository();

    public TermMention GetTermWithLocationLookup(ModelSession session, GeoNameRepository geoNameRepository, TermMention termMention) {
        String sign = termMention.getMetadata().getSign();
        GeoName geoName = geoNameRepository.findBestMatch(session, sign);
        Boolean termIsNotInGeoNames = geoName == null;
        if (termIsNotInGeoNames) return null;

        return populateTermMentions(termMention,
                geoName.getMetadata().getLatitude(),
                geoName.getMetadata().getLongitude(),
                getTitleFromGeoName(session, geoName),
                geoName.getMetadata().getPopulation());
    }

    public TermMention GetTermWithPostalCodeLookup(ModelSession session, GeoNamePostalCodeRepository geoNamePostalCodeRepository, TermMention termMention) {
        //we are assuming all US zip codes at this point!
        String zip = termMention.getMetadata().getSign().length() == 5 ? termMention.getMetadata().getSign() : termMention.getMetadata().getSign().substring(0, 5);
        GeoNamePostalCode postalCode = geoNamePostalCodeRepository.findByUSZipCode(session, zip);
        Boolean termIsNotValidPostalCode = postalCode == null;
        if (termIsNotValidPostalCode) return null;
        return populateTermMentions(termMention,
                postalCode.getMetadata().getLatitude(),
                postalCode.getMetadata().getLongitude(),
                getTitleFromPostalCode(postalCode),
                POSTAL_CODE_POPULATION);
    }

    private TermMention populateTermMentions(TermMention term, Double latitude, Double longitude, String title, Long population) {
        term.getMetadata().setGeoLocation(latitude, longitude);
        term.getMetadata().setGeoLocationTitle(title);
        term.getMetadata().setGeoLocationPopulation(population);
        return term;
    }

    private String getTitleFromGeoName(ModelSession session, GeoName geoName) {
        GeoNameAdmin1Code code = geoNameAdmin1CodeRepository.findByCountryAndAdmin1Code(session, geoName.getMetadata().getCountryCode(), geoName.getMetadata().getAdmin1Code());
        GeoNameCountryInfo countryInfo = geoNameCountryInfoRepository.findByCountryCode(session, geoName.getMetadata().getCountryCode());
        String countryString = geoName.getMetadata().getCountryCode();
        if (countryInfo != null) {
            countryString = countryInfo.getMetadata().getTitle();
        }

        if (code != null) {
            return geoName.getMetadata().getName() + ", " + code.getMetadata().getTitle() + ", " + countryString;
        } else {
            return geoName.getMetadata().getName() + ", " + countryString;
        }
    }

    private String getTitleFromPostalCode(GeoNamePostalCode postalCode) {
        StringBuilder sb = new StringBuilder(postalCode.getMetadata().getPlaceName());
        sb.append(", ")
                .append(postalCode.getMetadata().getAdmin1Code())
                .append(", ")
                .append(postalCode.getRowKey().getCountryCode())
                .append(" (")
                .append(postalCode.getRowKey().getPostalCode())
                .append(")");
        return sb.toString();
    }

    public boolean isPostalCode(TermMention termMention) {
        return isPostalCode(termMention.getMetadata().getSign());
    }

    private boolean isPostalCode(String location) {
        return Pattern.matches(POSTAL_CODE_REGEX, location);
    }
}
