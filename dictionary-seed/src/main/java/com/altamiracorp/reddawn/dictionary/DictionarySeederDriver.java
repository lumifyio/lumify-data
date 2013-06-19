package com.altamiracorp.reddawn.dictionary;


import org.apache.commons.cli.*;
import org.json.JSONException;

import java.io.IOException;

public class DictionarySeederDriver {

    private DictionarySearcher searcher = new DictionarySearcher();

    private CommandLine loadCommandLine(String[] args) {
        CommandLine cl;
        try {
             cl = new GnuParser().parse(createOptions(), args);
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse your input. Please make sure your options are valid or use " +
                    "--help for more information");
        }

        return cl;
    }

    private Options createOptions() {
        Options options = new Options();

        options.addOption(
                OptionBuilder
                        .withArgName("t")
                        .withLongOpt("types")
                        .withDescription("The types of terms to add to the dictionary (multiple separated by commas).\n" +
                                "Leave this blank to add all types.\n" +
                                "Valid values: place, person, organization, work, species, resource")
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("d")
                        .withLongOpt("directory")
                        .withDescription("The relative path to the directory to search. Defaults to ../dictionary-files/")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        return options;
    }

    protected String[] getTypes(CommandLine cl) {
        String[] types;
        String clParams = cl.getOptionValue("types");
        if(clParams != null && !clParams.trim().equals("")) {
            types = clParams.split(",");
            for(int i = 0; i < types.length; i++) {
                types[i] = types[i].trim();
            }
        } else {
            types = new String[]{"place", "person", "organization", "work", "species"};
        }

        return types;
    }

    protected String getSearchCategory(String type) {
        String searchCategory = "";

        if(type.equalsIgnoreCase("place")) {
            searchCategory = DictionarySearcher.PLACE;
        } else if(type.equalsIgnoreCase("person")) {
            searchCategory = DictionarySearcher.PERSON;
        } else if(type.equalsIgnoreCase("organization")) {
            searchCategory = DictionarySearcher.ORGANIZATION;
        } else if(type.equalsIgnoreCase("work")) {
            searchCategory = DictionarySearcher.WORK;
        } else if(type.equalsIgnoreCase("species")) {
            searchCategory = DictionarySearcher.SPECIES;
        } else if(type.equalsIgnoreCase("resource")) {
            searchCategory = DictionarySearcher.RESOURCE;
        } else {
            throw new RuntimeException("\"" + type + "\" is not a valid type");
        }

        return searchCategory;
    }

    public void run(String[] args) {
        CommandLine cl = loadCommandLine(args);
        String[] types = getTypes(cl);

        DictionarySearcher searcher = new DictionarySearcher();
        DictionaryEncoder encoder = new DictionaryEncoder(cl.getOptionValue("directory"));
        searcher.addEncoder(encoder);

        for(String type : types) {
            String category = getSearchCategory(type);
            System.out.println("\n\033[1mSearching for dbpedia class: " + category + "\033[0m");
            encoder.initializeDictionaryFile("en-ner-" + category + ".dict");

            long start = System.currentTimeMillis();
            searcher.search(category);
            long end = System.currentTimeMillis();

            System.out.println("\033[34mDictionary built successfully in " + (end - start) / 1000.0 + "s\033[0m");
            encoder.closeFile();
        }
    }

	public static void main(String[] args) throws IOException, JSONException {
		DictionarySeederDriver driver = new DictionarySeederDriver();
        driver.run(args);
	}
}
