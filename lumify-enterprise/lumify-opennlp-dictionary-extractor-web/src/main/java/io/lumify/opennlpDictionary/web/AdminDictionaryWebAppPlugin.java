package io.lumify.opennlpDictionary.web;

import com.altamiracorp.miniweb.Handler;
import com.altamiracorp.miniweb.StaticResourceHandler;
import io.lumify.web.AuthenticationHandler;
import io.lumify.web.WebApp;
import io.lumify.web.WebAppPlugin;
import io.lumify.web.privilegeFilters.AdminPrivilegeFilter;

import javax.servlet.ServletConfig;

public class AdminDictionaryWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletConfig config, Class<? extends Handler> authenticator, AuthenticationHandler authenticationHandler) {
        app.get("/admin/dictionaryAdmin.html", authenticationHandler, new StaticResourceHandler(getClass(), "/dictionaryAdmin.html", "text/html"));
        app.get("/admin/dictionary", authenticator, AdminPrivilegeFilter.class, AdminDictionary.class);
        app.get("/admin/dictionary/concept", authenticator, AdminPrivilegeFilter.class, AdminDictionaryByConcept.class);
        app.post("/admin/dictionary", authenticator, AdminPrivilegeFilter.class, AdminDictionaryEntryAdd.class);
        app.post("/admin/dictionary/delete", authenticator, AdminPrivilegeFilter.class, AdminDictionaryEntryDelete.class);
    }
}
