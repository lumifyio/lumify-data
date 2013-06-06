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

    /**
     * Constructor initializes data structures
     */
    public Query()
    {
        searchItems = new HashMap<String, String>();
        excludedTerms = new ArrayList<String>();
        requiredTerms = new ArrayList<String>();
        optionalTerms = new ArrayList<String>();
    }

    /**
     * Checks that date is formatted yyy-MM-dd
     *
     * From the tutorial here:
     * http://www.dreamincode.net/forums/topic/14886-date-validation-using-simpledateformat/
     *
     * @param date the date to be checked
     * @return true if correctly formatted, false otherwise
     */
    private boolean isValidDate(String date)
    {

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

    /**
     * Checks the formatting of the start date and adds the info to the query if correct.
     * @param date the starting date
     * @return true if a valid date was added to the query, false if otherwise
     */
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

    /**
     * Checks the formatting of the end date and adds the info to the query if correct,
     * @param date the ending date
     * @return true if a valid date was added to the query, false if otherwise
     */
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

    /**
     * Reads file containing country codes (each line of file contains: [country name] [tab] [country code]),
     * puts all codes into an ArrayList, and returns that ArrayList
     * @param fileName the name of the file containing the country codes
     * @return ArrayList of two letter country codes
     */
    private ArrayList<String> getCountryCodes(String fileName) throws InvalidCountryCodeException {
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
			throw new InvalidCountryCodeException("Problem with country code file: " + fileName);
        }
        return countryCodes;
    }

    /**
     * Assumes the user knows the country codes.
     * Checks that the country code entered is a valid country code.
     * If correct, adds the country code to the query.
     * @param country_ the country to be checked and added to the query
     * @return true if valid country code was added to the query, false if otherwise
     */
    public boolean setCountry(String country_) throws InvalidCountryCodeException {
        String country = country_.toLowerCase();
        if (getCountryCodes(COUNTRY_CODE_FILE).contains(country)) {
            searchItems.put("country", country);
            return true;
        }
        else
        {
            throw new InvalidCountryCodeException("Country code incorrect: " + country);
        }
    }

    /**
     * Takes in an integer lowRange value and adds it to the query.
     * @param lowValue the integer at the low end of a range
     * @return true if successful
     */
    public boolean setLowRange(int lowValue)
    {
        searchItems.put("lowRange", Integer.toString(lowValue));
        return true;
    }

    /**
     * Takes in an integer highRange value and adds it to the query.
     * @param highValue the integer at the high end of a range
     * @return true if successful
     */
    public boolean setHighRange(int highValue)
    {
        searchItems.put("lowRange", Integer.toString(highValue));
        return true;
    }

    /**
     * Adds a term that should NOT appear in search results.
     * @param term the term to be excluded
     */
    public void addExcludedTerm(String term)
    {
        excludedTerms.add(term);
    }

    /**
     * Adds a term that MUST appear in search results.  (AND)
     * @param term the term to be required
     */
    public void addRequiredTerm(String term)
    {
        requiredTerms.add(term);
    }

    /**
     * Adds a term that MAY appear in search results. (OR)
     * @param term the term to be considered
     */
    public void addOptionalTerm(String term)
    {
        optionalTerms.add(term);
    }

    /**
     *  Gets the HashMap of search parameters (excluding search terms)
     * @return HashMap of search parameters
     */
    public HashMap<String, String> getSearchItems() {
        return searchItems;
    }

    /**
     * Gets the list of terms that should not appear in the search results
     * @return ArrayList of excluded terms
     */
    public ArrayList<String> getExcludedTerms() {
        return excludedTerms;
    }

    /**
     * Gets the list of terms that should appear in the search results
     * @return ArrayList of required terms
     */
    public ArrayList<String> getRequiredTerms() {
        return requiredTerms;
    }

    /**
     * Gets the list of terms that could appear in the search results
     * @return ArrayList of optional terms
     */
    public ArrayList<String> getOptionalTerms() {
        return optionalTerms;
    }

	class IncorrectTimeFormatException extends Exception
	{
		public IncorrectTimeFormatException(String message)
		{
			super(message);
		}


	}

	class InvalidCountryCodeException extends Exception
	{
		public InvalidCountryCodeException(String message)
		{
			super(message);
		}


	}
}
