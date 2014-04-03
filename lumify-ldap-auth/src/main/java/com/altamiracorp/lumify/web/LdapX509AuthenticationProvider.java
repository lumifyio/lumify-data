package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.user.UserProvider;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.inject.Inject;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

public class LdapX509AuthenticationProvider extends X509AuthenticationProvider {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LdapX509AuthenticationProvider.class);

    @Inject
    public LdapX509AuthenticationProvider(final UserRepository userRepository, final UserProvider userProvider) {
        super(userRepository, userProvider);
    }

    @Override
    protected String getUsername(X509Certificate cert) {
        String dn = cert.getSubjectX500Principal().getName();
        try {
            return getCn(dn);
        } catch (InvalidNameException e) {
            LOGGER.error("Unable to parse CN from X509 certificate DN: %s", dn);
            return null;
        }
    }

    private String getCn(String dn) throws InvalidNameException {
        LdapName ldapDN = new LdapName(dn);
        for (Rdn rdn : ldapDN.getRdns()) {
            if (rdn.getType().equalsIgnoreCase("CN")) {
                return rdn.getValue().toString();
            }
        }
        return null;
    }

    @Override
    public boolean login(HttpServletRequest request) {
        return false;
    }

}
