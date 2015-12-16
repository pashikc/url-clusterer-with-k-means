package com.chayka.machinelearning.clustering.url.kmeans;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class loads log entries into memory of the program. 
 * It is specific to used data set - clarknet access log Aug28, taken from 
 * ftp://ita.ee.lbl.gov/traces/clarknet_access_log_Aug28.gz
 *
 * @author Pavlo Chayka
 *
 */

public class DataLoader {

	public static void main(String[] args) {
		MainKMeans.main(args);
	}
	
	String dataPath;
	SessionStorage storage;

	public DataLoader(String data, SessionStorage storage) {
		this.dataPath = data;
		this.storage = storage;
	}

	public void loadData() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(dataPath));
			String line;
			int counter = 0;
			String id;
			String[] tokens;
			
			while((line = in.readLine()) != null)
			{
				tokens = line.split(" - - ");		
				id = tokens[0];
				if (tokens.length < 2 ) {
					continue;
				}
				tokens = tokens[1].split("] \"GET ");
				if (tokens.length < 2 ) {
					continue;
				}
				tokens = tokens[1].split(" HTTP");
				storage.putPair(id, tokens[0]);
				
				counter++;
			}
				
			System.out.println("Number of input entries - " + counter);
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
