package com.altamiracorp.lumify.web.routes.admin;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.dictionary.DictionaryEntry;
import com.altamiracorp.lumify.model.dictionary.DictionaryEntryRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class AdminDictionary extends BaseRequestHandler {

    private DictionaryEntryRepository dictionaryEntryRepository;

    @Inject
    public AdminDictionary (DictionaryEntryRepository dictionaryEntryRepository) {
        this.dictionaryEntryRepository = dictionaryEntryRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);

        List<DictionaryEntry> dictionary = dictionaryEntryRepository.findAll(user);
        JSONArray entries = new JSONArray();
        JSONObject results = new JSONObject();
        for (DictionaryEntry entry : dictionary) {
            entries.put(entry.toJson());
        }

        results.put("entries",entries);

        respondWithJson(response,results);
    }
}
