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

    //PRELIMINARY TESTING ONLY! REMOVE MAIN
    //Try to convert this to JUnit testing tomorrow, or whichever kind of testing the team uses.
    public static void main( String[] args )   throws Exception
    {
        System.out.println( "Starting..." );
        Query q1 = new Query("US", "Monday", "Friday", "as;ldkfj", "100", "400");
        System.out.println(q1.getSearchItems().toString());
        q1.addExcludedTerm("excludedTerm1");
        System.out.println("Excluded: " + q1.getExcludedTerms().toString());
        q1.addOptionalTerm("optionalTerm1");
        System.out.println("Optional: " + q1.getOptionalTerms().toString());
        q1.addRequiredTerm("requiredTerm1");
        System.out.println("Required:" + q1.getRequiredTerms().toString());
    }
}
