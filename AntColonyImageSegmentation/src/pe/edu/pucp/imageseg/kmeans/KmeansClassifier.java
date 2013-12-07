package pe.edu.pucp.imageseg.kmeans;

import pe.edu.pucp.imageseg.aco.config.ProblemConfiguration;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author CarlosG
 *
 */
public class KmeansClassifier {

	private static final double BACKGROUND_CLASS = 0.0;
	private static final int INITIAL_CAPACITY = 0;
	private static final String DATASET_NAME = "PHEROMONE_GRAYSCALE_INFO";
	private static final String GREYSCALE_VALUE_ATTRIBUTE = "greyscaleValue";
	private static final String PHEROMONE_VALUE_ATTRIBUTE = "pheromoneValue";

	private int[][] normalizedPheromoneMatrix;
	private int[][] imageGraph;
	private int numberOfClusters;

	public KmeansClassifier(int[][] normalizedPheromoneMatrix,
			int[][] imageGraph, int numberOfClusters) {
		this.normalizedPheromoneMatrix = normalizedPheromoneMatrix;
		this.imageGraph = imageGraph;
		this.numberOfClusters = numberOfClusters;
	}

	public int[][] generateSegmentedImage() throws Exception {
		Instances instances = getInstancesFromMatrix();
		double[] clusterAssignments = getClusterAssignments(instances);
		int[][] resultMatrix = new int[imageGraph.length][imageGraph[0].length];

		int pixelCounter = 0;
		for (int i = 0; i < imageGraph.length; i++) {
			for (int j = 0; j < imageGraph[0].length; j++) {
				int grayScaleValue = ProblemConfiguration.GRAYSCALE_MAX_RANGE;
				if (clusterAssignments[pixelCounter] == BACKGROUND_CLASS) {
					grayScaleValue = ProblemConfiguration.GRAYSCALE_MIN_RANGE;
				}
				resultMatrix[i][j] = grayScaleValue;
				pixelCounter++;
			}
		}
		return resultMatrix;
	}

	private double[] getClusterAssignments(Instances instances)
			throws Exception {
		SimpleKMeans simpleKMeans = new SimpleKMeans();
		simpleKMeans.setNumClusters(numberOfClusters);
		simpleKMeans.buildClusterer(instances);

		ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
		clusterEvaluation.setClusterer(simpleKMeans);
		clusterEvaluation.evaluateClusterer(instances);
		double[] clusterAssignments = clusterEvaluation.getClusterAssignments();
		return clusterAssignments;
	}

	private Instances getInstancesFromMatrix() {
		FastVector atributes = new FastVector();
		atributes.addElement(new Attribute(PHEROMONE_VALUE_ATTRIBUTE));
		atributes.addElement(new Attribute(GREYSCALE_VALUE_ATTRIBUTE));

		Instances instances = new Instances(DATASET_NAME, atributes,
				INITIAL_CAPACITY);

		for (int i = 0; i < imageGraph.length; i++) {
			for (int j = 0; j < imageGraph[0].length; j++) {
				Instance instance = new Instance(atributes.size());
				instance.setValue(0, normalizedPheromoneMatrix[i][j]);
				instance.setValue(1, imageGraph[i][j]);
				instances.add(instance);
			}
		}
		return instances;
	}

}
