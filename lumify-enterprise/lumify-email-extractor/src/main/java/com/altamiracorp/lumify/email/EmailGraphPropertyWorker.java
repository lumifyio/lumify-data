package com.altamiracorp.lumify.email;

import com.altamiracorp.lumify.core.ingest.graphProperty.RegexGraphPropertyWorker;

public class EmailGraphPropertyWorker extends RegexGraphPropertyWorker {
    private static final String EMAIL_REG_EX = "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
    private static final String EMAIL_TYPE = "http://lumify.io/dev#emailAddress";

    public EmailGraphPropertyWorker() {
        super(EMAIL_REG_EX, EMAIL_TYPE);
    }
}
