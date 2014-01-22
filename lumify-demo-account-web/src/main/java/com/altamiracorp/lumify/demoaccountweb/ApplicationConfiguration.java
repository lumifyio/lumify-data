package com.altamiracorp.lumify.demoaccountweb;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.Properties;

@Singleton
public class ApplicationConfiguration {
    public static final String LUMIFY_URL      = "demoaccount.lumify-url";
    public static final String EMAIL_SUBJECT   = "demoaccount.email.subject";
    public static final String EMAIL_FROM      = "demoaccount.email.from";
    public static final String EMAIL_FROM_FULL = "demoaccount.email.from.full";
    public static final String EMAIL_SMTP_HOST = "demoaccount.email.smtp.host";
    public static final String EMAIL_SMTP_USER = "demoaccount.email.smtp.user";
    public static final String EMAIL_SMTP_PASS = "demoaccount.email.smtp.password";
    public static final String EMAIL_SMTP_SSL  = "demoaccount.email.smtp.ssl.enable";

    private Map<String, Object> properties;

    public ApplicationConfiguration(Map properties) {
        checkProperties(properties);
        this.properties = properties;
    }

    public String get(String key) {
        return (String) properties.get(key);
    }


    private void checkProperties(Map properties) {
        String[] toCheck = new String[] {
            LUMIFY_URL,
            EMAIL_SUBJECT,
            EMAIL_FROM,
            //EMAIL_FROM_FULL,
            EMAIL_SMTP_HOST,
            EMAIL_SMTP_USER,
            EMAIL_SMTP_PASS,
            //EMAIL_SMTP_SSL
        };

        for (String check : toCheck) {
            Preconditions.checkNotNull(properties.get(check), "Configuration should contain: " + check);
        }

    }
}
