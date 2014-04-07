/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.term.resolution;

import com.bericotech.clavin.gazetteer.FeatureClass;
import com.bericotech.clavin.gazetteer.FeatureCode;
import com.bericotech.clavin.resolver.ResolvedLocation;

/**
 * This class provides simple mappings for the Lumify
 * ontology concepts &quot;country&quot;, &quot;city&quot;
 * and &quot;state&quot;.
 */
public class SimpleClavinOntologyMapper implements ClavinOntologyMapper {
    /**
     * The city URI.
     */
    private static final String CITY = "http://lumify.io/dev#city";

    /**
     * The country URI.
     */
    private static final String COUNTRY = "http://lumify.io/dev#country";

    /**
     * The state URI.
     */
    private static final String STATE = "http://lumify.io/dev#state";

    @Override
    public String getOntologyClassUri(final ResolvedLocation location, final String defaultValue) {
        String uri = defaultValue;
        FeatureClass featureClass = location.getGeoname().getFeatureClass();
        FeatureCode featureCode = location.getGeoname().getFeatureCode();
        if (featureClass == null) {
            featureClass = FeatureClass.NULL;
        }
        if (featureCode == null) {
            featureCode = FeatureCode.NULL;
        }
        switch (featureClass) {
            case A:
                switch (featureCode) {
                    case ADM1:
                        uri = STATE;
                        break;
                    case PCLI:
                        uri = COUNTRY;
                        break;
                }
                break;
            case P:
                uri = CITY;
                break;
        }
        return uri;
    }
}
