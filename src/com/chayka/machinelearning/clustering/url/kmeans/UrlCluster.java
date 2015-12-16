package com.chayka.machinelearning.clustering.url.kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This class represents a cluster object. 
 * It stores a list of assigned {@link SessionVector} elements and has 
 * a special {@link SessionVector} centroid field which represents its centroid.
 * 
 * @author Pavlo Chayka
 *
 */

public class UrlCluster {
	
	public List<SessionVector> sessions;
	public SessionVector centroid;
	public int id;

	//Creates a new Cluster
	public UrlCluster(int id) {
		this.id = id;
		sessions = new ArrayList<SessionVector>();
		this.centroid = null;
	}
 
	public List<SessionVector> getSessions() {
		return sessions;
	}
	
	public void addSession(SessionVector s) {
		sessions.add(s);
	}
 
	public void setSessions(List<SessionVector> sessions) {
		this.sessions = sessions;
	}
 
	public SessionVector getCentroid() {
		return centroid;
	}
 
	public void setCentroid(SessionVector session) {
		this.centroid = session;
	}
 
	public int getId() {
		return id;
	}
	
	public void clear() {
		sessions.clear();
	}
	
	public List<SessionVector> getSortedSessions() {
		List<SessionVector> res = new ArrayList<SessionVector>();
		Map<Double, List<SessionVector>> m = new TreeMap<Double, List<SessionVector>>(Collections.reverseOrder());
		
		for (SessionVector v: sessions) {
			double distToCenter = SessionVector.getCosineSimilarity(v, centroid);
			if (!m.containsKey(distToCenter)) {
				m.put(distToCenter, new ArrayList<SessionVector>());
			}
			m.get(distToCenter).add(v);
		}
		for (Entry<Double, List<SessionVector>> item: m.entrySet()) {
			List<SessionVector> list = item.getValue();
			for (SessionVector v: list) {
				res.add(v);
			}
		}		
		return res;
	}
	
	public void plotCluster() {
		System.out.println("[Cluster: " + id+"]");
		System.out.println("[Centroid: " + centroid + "]");
		System.out.println("[Sessions: \n");
		for(SessionVector v: sessions) {
			System.out.println(v);
		}
		System.out.println("]");
	}
 
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UrlCluster other = (UrlCluster) obj;
		if (id != other.id)
			return false;
		return true;
	}
}