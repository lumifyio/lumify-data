package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.search.SearchProvider;
import com.altamiracorp.reddawn.search.TermSearchResult;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;

// TODO: change this over to an Entity search once entities work
public class EntitySearch implements Handler, AppAware {
    private TermRepository termRepository = new TermRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String query = request.getParameter("q");
        RedDawnSession session = app.getRedDawnSession(request);
        SearchProvider searchProvider = session.getSearchProvider();
        Collection <TermSearchResult> termSearchResults = queryTerm (searchProvider, query);
        JSONObject termsJson = termsToSearchResults(termSearchResults, request);
        new Responder(response).respondWith(termsJson);
    }

    private Collection<TermSearchResult> queryTerm (SearchProvider searchProvider, String query) throws Exception{
        return searchProvider.searchTerms(query);
    }

    private JSONObject termsToSearchResults (Collection <TermSearchResult> terms, HttpServletRequest request) throws JSONException{
        JSONObject termsJson = new JSONObject();
        JSONArray person = new JSONArray();
        termsJson.put("person", person);
        JSONArray location = new JSONArray();
        termsJson.put("location", location);
        JSONArray org = new JSONArray();
        termsJson.put("organization", org);
        for (TermSearchResult termSearchResult : terms){
            JSONObject termObject = termToSearchResult (request, termSearchResult);
            String conceptLabel = termSearchResult.getRowKey().getConceptLabel();
            if (conceptLabel.toLowerCase().contains("organization")){
                org.put(termObject);
            }
            else if (conceptLabel.toLowerCase().contains("person")){
                person.put(termObject);
            }
            else if (conceptLabel.toLowerCase().contains("location")){
                location.put(termObject);
            }
            else{
                throw new RuntimeException("Unhandled entity type: " + conceptLabel);
            }
        }
        return termsJson;
    }

    private JSONObject termToSearchResult (HttpServletRequest request, TermSearchResult termSearchResult) throws JSONException{
        JSONObject termJson = new JSONObject();
        termJson.put("url", EntityByRowKey.getUrl(request, termSearchResult.getRowKey()));
        termJson.put("rowKey", termSearchResult.getRowKey().toString());
        termJson.put("sign", termSearchResult.getSign());
        termJson.put("conceptLabel", termSearchResult.getRowKey().getConceptLabel());
        return termJson;
    }
}
