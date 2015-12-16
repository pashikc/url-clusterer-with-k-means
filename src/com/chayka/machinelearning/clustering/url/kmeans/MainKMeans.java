package com.chayka.machinelearning.clustering.url.kmeans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * The main class that implements K-means for URL clustering.
 * 
 * @author Pavlo Chayka
 *
 */

public class MainKMeans {

	public static final String PATH_TO_DATA = "Aug28.log";
	public static final String OUTPUT_FOLDER = "out";

	/*
	 * During testing and research, I concluded that the most appropriate K for this specific data set 
	 * should be equal to 26 (the number of clusters) 
	 */	
	private int KclusterNumber = 26;
	
	/**
	 * This parameter was introduced for testing purposes. It serves to limit the number of words to take out 
	 * from a cluster's most frequent terms in its sessions to calculate the new centroid.
	 * It works faster with low number, but can work as well with 100s, but x3-5 times slower.
	 */
	private static final int TOP_N = 10;
	private DataLoader loader;
	private SessionStorage storage;
	private List<UrlCluster> clusters;
	private Map<Double, List<UrlCluster>> sortedClusters;

	private static Map<Integer, Double> bestKs = new TreeMap<Integer, Double>();

	public static void main(String[] args) {
		new MainKMeans().start();
	}

	public void start() {
		System.out.println("Starting ...");

		loadData();
		System.out.println("Distinct sessions: " + storage.getStorage().size());

		setUpAuxiliaryTools();
		clusterData();
		
		System.out.println("Writing clusters to files...");
		printClusters();
		System.out.println("Done");

	}

	private void setUpAuxiliaryTools() {
		sortedClusters = new TreeMap<Double, List<UrlCluster>>(Collections.reverseOrder());
	}

	public void loadData() {
		System.out.println("Loading the data");
		long startTime = System.currentTimeMillis();

		storage = new SessionStorage();
		loader = new DataLoader(PATH_TO_DATA, storage);
		loader.loadData();

		long loadTime = System.currentTimeMillis() - startTime;
		System.out.println("Data loaded in " + (loadTime/1000) + " sec");
	}

	public void clusterData(int newK) {
		KclusterNumber = newK;
		clusterData();
	}

	public void clusterData() {

		clusters = new ArrayList<UrlCluster>();
		boolean done = false;
		long clusterStartTime = System.currentTimeMillis();

		// set random centroids for each cluster
		UrlCluster c;		
		List<String> selectedCentroids = new ArrayList<String>();
		for (int i = 0; i < KclusterNumber; i++) {
			c = new UrlCluster(i);
			SessionVector v = storage.getRandomSessionVector(selectedCentroids);
			c.setCentroid(v);
			clusters.add(c);
		}

		double lastDistance = -1;
		for (int iterationCounter = 0; !done && iterationCounter < 8; iterationCounter++) {
			long iterationStartTime = System.currentTimeMillis();
			sortedClusters.clear();
			//Clear clusters and remember their centroids
			List<SessionVector> oldCentroids = new ArrayList<SessionVector>();
			for (UrlCluster cl: clusters) {
				cl.clear();
				oldCentroids.add(cl.getCentroid());
			}

			//Assign each SessionVector to the closer cluster
			assignSessionVectorsToClusters();

			//Calculate new centroids
			calculateNewCentroids();


			List<SessionVector> newCentroids = new ArrayList<SessionVector>();
			for (UrlCluster cl: clusters) {
				newCentroids.add(cl.getCentroid());
			}

			//Calculates total distance between new and old centroids
			double distance = 0;

			for(int i = 0; i < oldCentroids.size(); i++) {
				SessionVector old = oldCentroids.get(i);
				SessionVector newer = newCentroids.get(i);
				double increment = SessionVector.getCosineSimilarity(old, newer);

				if (!sortedClusters.containsKey(increment)) {
					sortedClusters.put(increment, new ArrayList<UrlCluster>());
				}
				sortedClusters.get(increment).add(clusters.get(i));

				distance += increment;
			}
			System.out.println("Centroid distances: " + Math.round(distance * 10)/10.0 + "\t" + "for K = " + KclusterNumber + "\t time "
					+ ((System.currentTimeMillis() - iterationStartTime)/1000) + " sec");

			if(distance == 1.0 * KclusterNumber || lastDistance == distance) {
				done = true;
			}

			bestKs.put(KclusterNumber, Math.round(distance * 10)/10.0);

			lastDistance = distance;
		}
		System.out.println("Full time of clustering: " + ((System.currentTimeMillis() - clusterStartTime)/1000) + " sec");
	}

	private void calculateNewCentroids() {
		List<SessionVector> sessionList;
		for(UrlCluster cl: clusters) {
			sessionList = cl.getSessions();
			// put all words from session vectors into a single map
			Map<String, Integer> wordFrequencyCounter = new HashMap<String, Integer>();
			for (SessionVector v: sessionList) {
				Set<String> parts = v.getWords().keySet();
				for (String str: parts) {
					if (wordFrequencyCounter.containsKey(str)) {
						wordFrequencyCounter.put(str, wordFrequencyCounter.get(str) + 1);
					} else {
						wordFrequencyCounter.put(str, 1);
					}
				}
			}
			// get TOP_N frequent terms
			String[] top = getTopOccurrences(wordFrequencyCounter, TOP_N);
			SessionVector v = new SessionVector("centroidVector");
			v.putWords(top);
			cl.setCentroid(v);
		}
	}

	public void assignSessionVectorsToClusters() {
		double cousineDistance = 0;
		double max = 0;
		int clusterNum;

		for(SessionVector v : storage.getStorage().values()) {
			max = 0;
			clusterNum = 0; // default cluster is 0's, for those where cosine distance equals zero

			List<Double> testTrace = new ArrayList<Double>();
			for(int i = 0; i < KclusterNumber; i++) {
				UrlCluster cl = clusters.get(i);
				cousineDistance = SessionVector.getCosineSimilarity(v, cl.getCentroid());
				testTrace.add(cousineDistance);
				if(cousineDistance > max){
					max = cousineDistance;
					clusterNum = i;
				}
			}

			clusters.get(clusterNum).addSession(v);
		}
	}

	private static String[] getTopOccurrences(Map<String, Integer> unsortFrequentWords, int n) {
		List<Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortFrequentWords.entrySet());

		// Sort list with comparator, to compare the Map values in REVERSE ORDER, i.e. top-down
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		int resultSize = n <= list.size() ? n : list.size();
		String[] result = new String[resultSize];
		for (int i = 0; i < n && i < resultSize; i++) {
			result[i] = list.get(i).getKey();
		}	
		return result;
	}

	private void printClusters() {
		String directoryName = OUTPUT_FOLDER + System.currentTimeMillis()/1000;
		try {
			File directory = new File(directoryName);
			if (directory.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}

			// TODO: should sort entries inside a cluster?

			// output goes here
			File file;
			UrlCluster cl;
			BufferedWriter bw;
			FileWriter fw;
			int i = 1;
			for(Map.Entry<Double,List<UrlCluster>> entry : sortedClusters.entrySet()) {
				List<UrlCluster> alikeClusters = entry.getValue();
				for (int j = 0; j < alikeClusters.size(); j++) {
					cl = alikeClusters.get(j);
					file = new File(directoryName + "//" + i + ".txt");
					if (!file.exists()) {
						file.createNewFile();
					}
					fw = new FileWriter(file.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					for (SessionVector v: cl.getSessions()) {
						bw.write(v.toString() + "\n");
					}
					bw.close();
					i++;
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
