package com.altamiracorp.reddawn;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/6/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineFunctions {

    /**
     * Concatenates the strings in the list, separated by the connector specified
     *
     * @param list Terms to concatenate
     * @param connector String to insert in between each value
     * @return Concatenated string of terms
     */
    public static String concatenate(ArrayList<String> list, String connector) {
        String ret = "";

        for(String entry : list) {
            if(ret.length() > 0) ret += connector;
            ret += entry.replace(" ", "+");
        }

        return ret;
    }

    /**
     * Generates the query string parameters to be tacked on to the request URL
     *
     * @param params Map of Google Custom Search API keys to their values
     * @return Query string to append to URL
     */
    public static String createQueryString(Map<String, String> params) {
        String ret = "";

        // Adds GET variable to the query for each parameter in the map
        for(Map.Entry<String, String> entry : params.entrySet()) {
            ret += "&" + entry.getKey() + "=" + entry.getValue();
        }

        return ret;
    }

}
