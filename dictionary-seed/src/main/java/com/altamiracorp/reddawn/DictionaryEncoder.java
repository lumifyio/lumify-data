package com.altamiracorp.reddawn;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class DictionaryEncoder {

	public void run(String filename) {

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
