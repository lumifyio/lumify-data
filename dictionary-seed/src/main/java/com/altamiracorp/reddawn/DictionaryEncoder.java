package com.altamiracorp.reddawn;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class DictionaryEncoder {

	public void run(String filename) throws IOException {
		StringBuilder dictionary = new StringBuilder();
		ArrayList<String> entities = getEntities(filename);
		dictionary.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<dictionary>");
        for (String entry : entities) {
			dictionary.append(getTokenizedEntry("\n" + entry));
			
		}
        dictionary.append("</dictionary>");
        if (writeToFile(dictionary)) {
            System.out.println("Successfully converted " + filename + " to dictionary file " + getCurrentDirectory() + filename);
        }
	}

    private String getCurrentDirectory() {
        // TODO write getCurrentDirectory
        return null;
    }

    protected boolean writeToFile(StringBuilder dictionary) {
        //TODO write writeTOFile

        return false;
    }

    protected StringBuilder getTokenizedEntry(String entry) throws IOException {

		StringBuilder formattedEntry = new StringBuilder();
		String[] tokens = tokenize(entry);

		formattedEntry.append("\t<entry>");
	    for (String token : tokens) {
			formattedEntry.append("\n\t\t<token>" +  token + "</token>");
		}
		formattedEntry.append("\n\t</entry>");
		return formattedEntry;
	}

	protected String[] tokenize(String entry) throws FileNotFoundException {
		InputStream modelIn = new FileInputStream("/Users/swoloszy/Documents/NIC/red-dawn/dictionary-seed/src/test/en-token.bin");
		TokenizerModel model = null;
		try {
			model = new TokenizerModel(modelIn);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
		Tokenizer tokenizer = new TokenizerME(model);
		return tokenizer.tokenize(entry);
	}

	protected ArrayList<String> getEntities(String filename) {
		ArrayList<String> items = new ArrayList<String>();
		File file = new File(filename);
		String line = "";
		try {
			Scanner fileReader = new Scanner(file);
			while (fileReader.hasNext()) {
				line = fileReader.nextLine();
				items.add(line);
			}
		} catch (FileNotFoundException e) {
			System.err.println("[Error] : Problem with file " + filename);
		}
		return items;
	}

}
