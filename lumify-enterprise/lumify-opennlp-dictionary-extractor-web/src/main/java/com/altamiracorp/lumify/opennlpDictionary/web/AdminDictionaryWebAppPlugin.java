package com.altamiracorp.lumify.opennlpDictionary.web;

import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.lumify.web.WebAppPlugin;
import com.altamiracorp.miniweb.Handler;
import com.altamiracorp.miniweb.StaticResourceHandler;

import javax.servlet.ServletConfig;

public class AdminDictionaryWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletConfig config, Class<? extends Handler> authenticator, AuthenticationProvider authenticatorInstance) {
        app.get("/admin/dictionaryAdmin.html", authenticatorInstance, new StaticResourceHandler(getClass(), "/dictionaryAdmin.html", "text/html"));
        app.get("/admin/dictionary", authenticator, AdminDictionary.class);
        app.get("/admin/dictionary/concept", authenticator, AdminDictionaryByConcept.class);
        app.post("/admin/dictionary", authenticator, AdminDictionaryEntryAdd.class);
        app.post("/admin/dictionary/delete", authenticator, AdminDictionaryEntryDelete.class);
    }
}
