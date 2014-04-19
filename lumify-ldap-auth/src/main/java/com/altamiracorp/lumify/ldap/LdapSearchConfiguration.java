package com.altamiracorp.lumify.ldap;

import com.altamiracorp.lumify.core.config.Configurable;
import com.altamiracorp.lumify.core.exception.LumifyException;
import com.unboundid.ldap.sdk.SearchScope;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class LdapSearchConfiguration {
    private String userSearchBase;
    private SearchScope userSearchScope;
    private List<String> userAttributes;
    private String userCertificateAttribute;
    private String groupSearchBase;
    private SearchScope groupSearchScope;
    private String userSearchFilter;
    private String groupRoleAttribute;
    private String groupSearchFilter;

    @Configurable(name = "user-search-base")
    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    @Configurable(name = "user-search-scope")
    public void setUserSearchScope(String userSearchScope) {
        this.userSearchScope = toSearchScope(userSearchScope);
    }

    @Configurable(name = "user-attributes")
    public void setUserAttributes(String userAttributes) {
        this.userAttributes = Arrays.asList(userAttributes.split(","));
    }

    @Configurable(name = "user-search-filter", defaultValue = "(cn=${cn})")
    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    @Configurable(name = "user-certificate-attribute", defaultValue = "userCertificate;binary")
    public void setUserCertificateAttribute(String userCertificateAttribute) {
        this.userCertificateAttribute = userCertificateAttribute;
    }

    @Configurable(name = "group-search-base")
    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    @Configurable(name = "group-search-scope")
    public void setGroupSearchScope(String groupSearchScope) {
        this.groupSearchScope = toSearchScope(groupSearchScope);
    }

    @Configurable(name = "group-role-attribute", defaultValue = "cn")
    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    @Configurable(name = "group-search-filter", defaultValue = "(uniqueMember=${dn})")
    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public SearchScope getUserSearchScope() {
        return userSearchScope;
    }

    public List<String> getUserAttributes() {
        return userAttributes;
    }

    public String getUserCertificateAttribute() {
        return userCertificateAttribute;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public SearchScope getGroupSearchScope() {
        return groupSearchScope;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    private SearchScope toSearchScope(String searchScope) {
        try {
            Field f = SearchScope.class.getField(searchScope.toUpperCase());
            return (SearchScope) f.get(null);
        } catch (Exception e) {
            throw new LumifyException("unable to configure search scope", e);
        }
    }
}
