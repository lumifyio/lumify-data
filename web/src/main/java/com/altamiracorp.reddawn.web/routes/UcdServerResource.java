package com.altamiracorp.reddawn.web.routes;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.web.WebUcdClientFactory;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UcdServerResource extends ServerResource {

  protected UcdClient<AuthorizationLabel> getUcdClient() throws AccumuloSecurityException, AccumuloException {
    // TODO this needs refactoring
    return WebUcdClientFactory.createUcdClient();
  }

  protected QueryUser<AuthorizationLabel> getQueryUser() {
    // TODO this needs configuring
    return new QueryUser<AuthorizationLabel>("U", new AuthorizationLabel());
  }

  public static String urlDecode(String s) {
    try {
      return URLDecoder.decode(s, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
