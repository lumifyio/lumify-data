/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.clavin;

import com.bericotech.clavin.resolver.ResolvedLocation;

/**
 * A strategy for mapping Clavin ResolvedLocation objects to Lumify
 * ontology concepts.
 */
public interface ClavinOntologyMapper {
    /**
     * Map the Clavin resolved location to a known Lumify ontology concept.
     * @param location the resolved location
     * @param defaultValue the default value if the concept cannot be resolved
     * @return the URI of the Lumify ontology concept or the default value
     * if it cannot be resolved to a known concept
     */
    String getOntologyClassUri(final ResolvedLocation location, final String defaultValue);
}
