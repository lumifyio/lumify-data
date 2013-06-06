package com.altamiracorp.reddawn;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class wraps the data to be used to construct the query with the user's input.
 */

public class Query
{
    private HashMap<String, String> searchItems;
    private ArrayList<String> excludedTerms;
    private ArrayList<String> requiredTerms;
    private ArrayList<String> optionalTerms;

    public Query(String country_, String startDate_, String endDate_, String geoLoc_,
                 String lowRange_, String highRange_)
    {
        //jeff, the ranges are taken in as Strings, so engine will need to parse
        searchItems = new HashMap<String, String>();
        searchItems.put("country", country_);
        searchItems.put("startDate", startDate_);
        searchItems.put("endDate", endDate_);
        searchItems.put("geoLoc", geoLoc_);
        searchItems.put("lowRange", lowRange_);
        searchItems.put("highRange", highRange_);

        excludedTerms = new ArrayList<String>();
        requiredTerms = new ArrayList<String>();
        optionalTerms = new ArrayList<String>();
    }

    public void addExcludedTerm(String term)
    {
        excludedTerms.add(term);
    }

    public void addRequiredTerm(String term)
    {
        requiredTerms.add(term);
    }

    public void addOptionalTerm(String term)
    {
        optionalTerms.add(term);
    }


    public HashMap<String, String> getSearchItems() {
        return searchItems;
    }

    public ArrayList<String> getExcludedTerms() {
        return excludedTerms;
    }

    public ArrayList<String> getRequiredTerms() {
        return requiredTerms;
    }

    public ArrayList<String> getOptionalTerms() {
        return optionalTerms;
    }

}
