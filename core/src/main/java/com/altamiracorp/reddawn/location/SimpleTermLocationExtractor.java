package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.geoNames.*;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;

public class SimpleTermLocationExtractor {
    private GeoNameAdmin1CodeRepository geoNameAdmin1CodeRepository = new GeoNameAdmin1CodeRepository();
    private GeoNameCountryInfoRepository geoNameCountryInfoRepository = new GeoNameCountryInfoRepository();

    public Term GetTermWithLocationLookup(Session session, GeoNameRepository geoNameRepository, Term term) {
        String sign = term.getRowKey().getSign();
        GeoName geoName = geoNameRepository.findBestMatch(session, sign);
        Boolean termIsNotInGeoNames = geoName == null;
        if (termIsNotInGeoNames) return null;

        for (TermMention termMention : term.getTermMentions()) {
            Double latitude = geoName.getMetadata().getLatitude();
            Double longitude = geoName.getMetadata().getLongitude();
            termMention.setGeoLocation(latitude, longitude);
            termMention.setGeoLocationTitle(getTitle(session, geoName));
            termMention.setGeoLocationPopulation(geoName.getMetadata().getPopulation());
        }
        return term;
    }

    private String getTitle(Session session, GeoName geoName) {
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
}
