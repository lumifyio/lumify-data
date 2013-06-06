package com.altamiracorp.reddawn;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class wraps the data to be used to construct the query with the user's input.
 */

public class Query
{
    private HashMap<String, String> searchItems;
    private ArrayList<String> excludedTerms;
    private ArrayList<String> requiredTerms;
    private ArrayList<String> optionalTerms;
    private final String COUNTRY_CODE_FILE = "countryCodes.txt";

    public Query()
    {
        searchItems = new HashMap<String, String>();
        excludedTerms = new ArrayList<String>();
        requiredTerms = new ArrayList<String>();
        optionalTerms = new ArrayList<String>();
    }

    private boolean isValidDate(String date)
    {
        // From the tutorial here:
        // http://www.dreamincode.net/forums/topic/14886-date-validation-using-simpledateformat/

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date testDate = null;
        try
        {
            testDate = dateFormat.parse(date);
        }
        catch (ParseException e)
        {
            System.out.println("Invalid date format: " + date);
            return false;
        }

        if (!dateFormat.format(testDate).equals(date))
        {
            System.out.println("Invalid date: " + date);
            return false;
        }
        return true;
    }


    public boolean setStartDate(String date)
    {
       if(isValidDate(date))
       {
           searchItems.put("startDate", date);
           return true;
       }
       else
       {
           return false;
       }
    }

    public boolean setEndDate(String date)
    {
        if(isValidDate(date))
        {
            searchItems.put("endDate", date);
            return true;
        }
        else
        {
            return false;
        }
    }

    private ArrayList<String> getCountryCodes(String fileName)
    {
        ArrayList<String> countryCodes = new ArrayList<String>();
        File file = new File(fileName);
        String line = "";
        try
        {
            Scanner fileReader = new Scanner(file);
            while(fileReader.hasNext())
            {
                line = fileReader.nextLine();
                countryCodes.add(line.split("\t")[1]);
            }
        }
        catch (Exception e)
        {
            System.out.println("Problem with country code file: " + fileName);
        }
        return countryCodes;
    }

    public boolean setCountry(String country_)
    {
        String country = country_.toLowerCase();
        if (getCountryCodes(COUNTRY_CODE_FILE).contains(country)) {
            searchItems.put("country", country);
            return true;
        }
        else
        {
            System.out.println("Country code incorrect: " + country);
            return false;
        }
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
