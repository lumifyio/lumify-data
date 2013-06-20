package com.altamiracorp.reddawn.crawler;

import java.util.ArrayList;

public class Utils {

    /**
     * Concatenates the strings in the list, separated by the connector specified
     *
     * @param list      Terms to concatenate
     * @param connector String to insert in between each value
     * @return Concatenated string of terms
     */
    public static String concatenate(ArrayList<String> list, String connector) {
        String ret = "";
        for (String entry : list) {
            if (ret.length() > 0) ret += connector;
            ret += entry.replace(" ", connector);
        }
        return ret;
    }
}
