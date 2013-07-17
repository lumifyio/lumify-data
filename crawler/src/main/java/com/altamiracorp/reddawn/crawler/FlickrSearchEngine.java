package com.altamiracorp.reddawn.crawler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlickrSearchEngine extends SearchEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlickrSearchEngine.class);

    private static final String BASE_URL = "http://api.flickr.com/services/rest?method=flickr.photos.search";
    private static final String API_KEY = "06e4190d750d2386f81d1afde77d7b38"; // for Sam's account (sam wolo)
    private static final String NO_KNOWN_COPYRIGHT_RESTRICTIONS_LICENSE = "7";
    private static final String USGOVT_WORK_LICENSE = "8";
    private static final String PRIVACY_FILTER = "public";
    private static final String GEO_ACCURACY = "7"; // scale is 1 (world) to 16 (city / street)
    private static final String CONTENT_TYPE_ALL = "7"; // includes photos, screenshots, and other
    private static final String CONTENT_TYPE_PHOTOS_AND_SCREENSHOTS = "4";
    private static final String EXTRAS = "description,date_upload,date_taken,owner_name," +
            "original_format,last_update,geo,tags,views,media";
    private static final List<String> METADATA_ITEMS = new ArrayList<String>(Arrays.asList("title", "description",
            "dateupload", "datetaken", "ownername", "lastupdate", "tags",
            "views", "media", "latitude", "longitude", "accuracy"));
    private String resultsPerPage = "500";
    private static final int MAX_RESULTS_PER_PAGE = 500;
    private static final String FORMAT = "json";
    private static String FILE_EXTENSION = ".jpg";  // other options are png or gif but they require reformatting url
    // see http://www.flickr.com/services/api/misc.urls.html
    private TreeMap<String, TreeMap<String, String>> imageResults;
    private ArrayList<String> results;

    public FlickrSearchEngine(Crawler c) {
        super(c);
    }

    @Override
    protected List<String> search(Query q, int numOfResults) {
        imageResults = new TreeMap<String, TreeMap<String, String>>();
        results = new ArrayList<String>();
        if (numOfResults <= MAX_RESULTS_PER_PAGE) {
            resultsPerPage = "" + numOfResults;
            fetchOnePageOfQueryResults(q, 1, numOfResults);
        } else {
            for (int i = 0; i * MAX_RESULTS_PER_PAGE < numOfResults; i++) {
                int resultsLeftToGet = numOfResults - (i * MAX_RESULTS_PER_PAGE);
                int resultsToGet = (resultsLeftToGet < MAX_RESULTS_PER_PAGE) ? resultsLeftToGet : MAX_RESULTS_PER_PAGE;
                fetchOnePageOfQueryResults(q, i+1, resultsToGet);
            }
        }
        crawlResults(q, imageResults);
        return results;
    }

    private void fetchOnePageOfQueryResults(Query q, int index, int resultsToFetch) {
        String queryUrl = createQueryUrl(q, index, Integer.parseInt(resultsPerPage));
        URL url = getURL(queryUrl);
        JSONObject jsonObject = null;
        if (url != null) {
            jsonObject = getJsonObjectFromUrl(url);
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (jsonObject != null) {
            JSONArray photos = getPhotosJsonArray(jsonObject);
            for (int i = 0; i < photos.length() && resultsToFetch > 0; i++, resultsToFetch--) {
                JSONObject photo = getPhotoJsonObject(photos, i);
                if (photo != null) {
                    String photoUrl = getPhotoUrl(photo);
                    imageResults.put(photoUrl, (TreeMap<String, String>) getPhotoMetadata(photo));
                    results.add(photoUrl);
                }
            }
        }
    }

    private void crawlResults(Query q, TreeMap<String, TreeMap<String, String>> imageResults) {
        try {
            getCrawler().crawlPhotos(imageResults, q);
            StringBuilder stringBuilder = new StringBuilder();
        } catch (Exception e) {
            LOGGER.error("The crawler failed to crawl the " + getEngineName() + " on query \"" +
                    q.getQueryString() + "\" result set");
        }
    }

    private JSONObject getPhotoJsonObject(JSONArray photos, int i) {
        try {
            return photos.getJSONObject(i);
        } catch (JSONException e) {
            LOGGER.error("Could not retrieve photo information for index " + i + " on: " + photos);
            return null;
        }
    }

    private Map<String, String> getPhotoMetadata(JSONObject photo) {
    // see http://www.flickr.com/services/api/misc.urls.html
        String currentTag = "";
        try {
            Map<String, String> photoInfo = new TreeMap<String, String>();
            for (String tag : METADATA_ITEMS) {
               currentTag = tag;
               photoInfo.put(tag, photo.get(tag).toString());
            }
            return photoInfo;
        } catch (JSONException e) {
            LOGGER.error("Could not retrieve photo metadata on tag " + currentTag);
            e.printStackTrace();
            return null;
        }
    }

    private String getPhotoUrl(JSONObject photo) {
        try {
            String photoId = photo.get("id").toString();
            String farmId = photo.get("farm").toString();
            String serverId = photo.get("server").toString();
            String secret = photo.get("secret").toString();
            String photoUrl = "http://farm" + farmId + ".staticflickr.com/" + serverId
                    + "/" + photoId + "_" + secret + FILE_EXTENSION;
            return photoUrl;
        } catch (JSONException e) {
            return null;
        }
    }

    private JSONArray getPhotosJsonArray(JSONObject jsonObject) {
        try {
            return jsonObject.getJSONObject("photos").getJSONArray("photo");
        } catch (JSONException e) {
            LOGGER.error("JSON Object does not contain photos{photo[]}.");
            return null;
        }
    }

    private URL getURL(String queryUrl) {
        try {
            return new URL(queryUrl);
        } catch (Exception e) {
            LOGGER.error("Malformed search URL: " + queryUrl);
            return null;
        }
    }

    private JSONObject getJsonObjectFromUrl(URL url) {
        try {
            String content = getContent(url);
            if (content != null) {
                return new JSONObject(content);
            } else {
                return null;
            }
        } catch (JSONException e) {
            LOGGER.error("Invalid JSON response: " + url.toString());
            return null;
        }
    }

    private static String getContent(URL url) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return removeFlickerApiTags(builder);
        } catch (IOException e) {
            LOGGER.error("Bad url connection: " + url);
            return null;
        }
    }

    private static String removeFlickerApiTags(StringBuilder builder) {
        Pattern p = Pattern.compile("jsonFlickrApi\\((.+)\\)");
        Matcher m = p.matcher(builder.toString());
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    protected String createQueryUrl(Query query, int page, int perPage) {
        TreeMap<String, String> queryParams = new TreeMap();
        queryParams.put("api_key", API_KEY);
//        queryParams.put("license", NO_KNOWN_COPYRIGHT_RESTRICTIONS_LICENSE);
        queryParams.put("privacy_filter", PRIVACY_FILTER);
        queryParams.put("accuracy", GEO_ACCURACY);
        queryParams.put("content_type", CONTENT_TYPE_ALL);
        queryParams.put("extras", EXTRAS);
        queryParams.put("per_page", "" + perPage);
        queryParams.put("page", "" + page);
        queryParams.put("format", FORMAT);
        String queryUrl = BASE_URL + SearchEngine.createQueryString(processQuery(query)) +
                SearchEngine.createQueryString(queryParams);
        return queryUrl;
    }

    protected TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> querySearchTerms = new TreeMap<String, String>();
        String queryString = "";
        if (q.getOptionalTerms().size() > 0) {
            queryString += Utils.concatenate(q.getOptionalTerms(), "+");
        }
        if (q.getRequiredTerms().size() > 0) {
            queryString += "+" + Utils.concatenate(q.getRequiredTerms(), "+");
        }
        if (q.getExcludedTerms().size() > 0) {
            queryString += "-" + Utils.concatenate(q.getExcludedTerms(), "-");
        }
        querySearchTerms.put("text", queryString);
        return querySearchTerms;
    }

    @Override
    public String getEngineName() {
        return "Flickr Search Engine";
    }

}
