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
    private static final String CITY = "city";
    
    /**
     * The country URI.
     */
    private static final String COUNTRY = "country";
    
    /**
     * The state URI.
     */
    private static final String STATE = "state";
    
    @Override
    public String getOntologyClassUri(final ResolvedLocation location) {
        String uri = null;
        if (location.geoname.featureClass == FeatureClass.P)
            uri = CITY;
        else if (location.geoname.featureClass == FeatureClass.A) {
            if (location.geoname.featureCode == FeatureCode.PCLI) {
                uri = COUNTRY;
            } else if (location.geoname.featureCode == FeatureCode.ADM1) {
                uri = STATE;
            }
        }
        return uri;
    }
}
