package com.chayka.machinelearning.clustering.url.kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class represents a user from the input file with all his browsing history.
 * It stores disassembled links in a map of {@link String}s, counting the number 
 * of each “word” (term from the link, i.e. parts of link separated with “/”) frequency. 
 * Host from the log file (IP or name) is used as SessionVector.id.
 * 
 * @author Pavlo Chayka
 *
 */

public class SessionVector {

	String id;
	Map<String, Integer> words;
	List<String> urls;


	public SessionVector(String id) {
		this.id = id;
		words = new HashMap<String, Integer>();
		urls = new ArrayList<String>();
	}

	public void putWords(String[] s) {
		for (int i = 1; i < s.length; i++) { // Here starting at [1] - because of specifics of implementation, i.e. at [0] will be "/"
			if (words.containsKey(s[i])) {
				words.put(s[i], (words.get(s[i]) + 1));
			} else {
				words.put(s[i], 1);
			}
		}
	}
	
	public void addUrl(String newUrl) {
		urls.add(newUrl);
	}

	public void putWord(String w) {
		if (words.containsKey(w)) {
			words.put(w, (words.get(w) + 1));
		} else {
			words.put(w, 1);
		}
	}
	
	/**
	 * This function calculates similarity of 2 {@link SessionVector} objects using cosine distance.
	 * Returned value of 1.0 means the vectors are extremely similar (or it is the same vector).
	 * Returned value of 0.0 means the vectors are orthogonal.
	 * In general, cosine distance of 0.8 for v3 and v4 vs. cosine distance of 0.2 for v5 and v6 
	 * means that v3 and v4 are more similar than v5 and v6.
	 * 
	 * @return value in between 0.0 and 1.0
	 */
	public static double getCosineSimilarity(SessionVector v1, SessionVector v2) {
		HashSet<String> conjunction = new HashSet<String>(); 
		conjunction.addAll(v1.getWords().keySet());
		conjunction.retainAll(v2.getWords().keySet());
		double scalar = 0, normV1 = 0, normV2 = 0;
		for (String s: conjunction) {
			scalar += v1.getWords().get(s) * v2.getWords().get(s);
		}
		for (String k : v1.getWords().keySet()) {
			normV1 += v1.getWords().get(k) * v1.getWords().get(k);
		}
		for (String k : v2.getWords().keySet()) {
			normV2 += v2.getWords().get(k) * v2.getWords().get(k);
		}
		if (scalar == 0) {
			return 0;
		}
		return scalar / Math.sqrt(normV1 * normV2);
	}

	public Map<String, Integer> getWords() {
		return words;
	}
	
	public String toString() {
		StringBuilder res = new StringBuilder();
		String delimeter = " ; ";				
		res.append(id + delimeter);
		for (String u: urls) {
			res.append(u +  delimeter);
		}
		return res.toString();
	}
	
	public String toString2() {
		StringBuilder res = new StringBuilder();
		res.append(id + "\t\t");
		for (String s: words.keySet()) {
			res.append(" >>> " + s);
		}
		return res.toString();
	}
}
