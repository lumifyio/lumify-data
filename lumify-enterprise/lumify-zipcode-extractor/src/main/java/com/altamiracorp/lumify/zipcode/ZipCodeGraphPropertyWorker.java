package com.altamiracorp.lumify.zipcode;

import com.altamiracorp.lumify.core.ingest.graphProperty.RegexGraphPropertyWorker;

public class ZipCodeGraphPropertyWorker extends RegexGraphPropertyWorker {
    private static final String ZIPCODE_REG_EX = "\\b\\d{5}-\\d{4}\\b|\\b\\d{5}\\b";
    private static final String LOCATION_TYPE = "http://lumify.io/dev#location";

    public ZipCodeGraphPropertyWorker() {
        super(ZIPCODE_REG_EX, LOCATION_TYPE);
    }
}
