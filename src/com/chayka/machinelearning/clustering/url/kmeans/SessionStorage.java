package com.chayka.machinelearning.clustering.url.kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class serves to introduce abstraction over {@link SessionVector}. 
 * It contains HashMap of {@link SessionVector} entries, where keys are their IDs. 
 * 
 * @author Pavlo Chayka
 *
 */

public class SessionStorage {
	Map<String, SessionVector> storage;
	public static final Random RANDOMIZER = new Random();
	
	SessionStorage() {
		storage = new HashMap<String, SessionVector>();
	}
	
	public void putPair(String id, String url) {
		String[] tokens = url.split("/");
		SessionVector v = storage.get(id);
		if (v == null) {
			v = new SessionVector(id);
			storage.put(id, v);
		}
		v.putWords(tokens);
		v.addUrl(url);
	}
	
	public SessionVector getVector(String id) {
		return storage.get(id);
	}

	public Map<String, SessionVector> getStorage() {
		return storage;
	}
	
	public SessionVector getRandomSessionVector(List<String> restricted) {
		int n;
		List<String> list = new ArrayList<String>();
		list.addAll(storage.keySet());
		String sessionId = "";
		do {
			n = RANDOMIZER.nextInt(storage.size());
			sessionId = list.get(n);
		}
		while (restricted.contains(sessionId));
		restricted.add(sessionId);
		return storage.get(list.get(n));
	}
	
}
