package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.geoNames.GeoName;
import com.altamiracorp.reddawn.model.geoNames.GeoNameRepository;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;

public class SimpleTermLocationExtractor {
    public Term GetTermWithLocationLookup(Session session, GeoNameRepository geoNameRepository, Term term) {
        // to do: filter terms based on concept == 'location' rather than != 'person', however
        // concept isn't standardized, so it could be 'country', 'state', 'place', etc
        boolean isPerson = term.getRowKey().getConceptLabel().toLowerCase().equals("person");
        if (isPerson) return null;

        String sign = term.getRowKey().getSign();
        GeoName geoName = geoNameRepository.findBestMatch(session, sign);
        Boolean termIsNotInGeoNames = geoName == null;
        if (termIsNotInGeoNames) return null;

        for (TermMention termMention : term.getTermMentions()) {
            Double latitude = geoName.getMetadata().getLatitude();
            Double longitude = geoName.getMetadata().getLongitude();
            termMention.setGeoLocation(latitude, longitude);
        }
        return term;
    }
}
