package com.altamiracorp.reddawn.crawler;

import java.util.ArrayList;
import java.util.TreeMap;

public class FlickrSearchEngine extends SearchEngine {

    private final String BASE_URL = "http://api.flickr.photos.search/?";
    private final String API_KEY = "06e4190d750d2386f81d1afde77d7b38"; // for Sam's account (sam wolo)
    private final String NO_KNOWN_COPYRIGHT_RESTRICTIONS_LICENSE = "7";
    private final String USGOVT_WORK_LICENSE = "8";
    private final String PRIVACY_FILTER = "public";
    private final String GEO_ACCURACY = "7"; // scale is 1 (world) to 16 (city / street)
    private final String CONTENT_TYPE_ALL = "7"; // includes photos, screenshots, and other
    private final String CONTENT_TYPE_PHOTOS_AND_SCREENSHOTS = "4";
    private final String EXTRAS = "description,license,date_upload,date_taken,owner_name,icon_server, " +
            "original_format,last_update,geo,tags,machine_tags,o_dims,views,media";
    private final String PER_PAGE = "500";


    public FlickrSearchEngine(Crawler c) {
        super(c);
    }

    @Override
    protected ArrayList<String> search(Query q, int numOfResults) {
        String queryUrl = createQueryUrl(q, 1, Integer.parseInt(PER_PAGE));
        return null;
    }

    protected String createQueryUrl(Query query, int page, int perPage) {
        TreeMap<String, String> queryParams = new TreeMap();
        queryParams.put("api_key", API_KEY);
        queryParams.put("license", NO_KNOWN_COPYRIGHT_RESTRICTIONS_LICENSE);
        queryParams.put("privacy_filter", PRIVACY_FILTER);
        queryParams.put("accuracy", GEO_ACCURACY);
        queryParams.put("content_type", CONTENT_TYPE_ALL);
        queryParams.put("content_type", CONTENT_TYPE_ALL);
        queryParams.put("extras", EXTRAS);
        queryParams.put("per_page", "" + perPage);
        queryParams.put("page", "" + page);
        String queryUrl = BASE_URL + SearchEngine.createQueryString(processQuery(query)) +
                SearchEngine.createQueryString(queryParams);
        return queryUrl;

    }

    protected TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> querySearchTerms = new TreeMap<String, String>();
        querySearchTerms.put("text", Utils.concatenate(q.getOptionalTerms(), "+") + "+" +
                Utils.concatenate(q.getRequiredTerms(), "+"));
        return querySearchTerms;
    }

    @Override
    public String getEngineName() {
        return "Flickr Search Engine";
    }

}
