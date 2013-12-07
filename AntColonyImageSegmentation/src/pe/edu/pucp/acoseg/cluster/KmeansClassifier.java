package pe.edu.pucp.acoseg.cluster;

import java.io.IOException;

import pe.edu.pucp.acoseg.ProblemConfiguration;
import pe.edu.pucp.acoseg.ant.Environment;
import pe.edu.pucp.acoseg.image.ImageUtilities;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class KmeansClassifier {

	private static final double BACKGROUND_CLASS = 0.0;
	private static final int INITIAL_CAPACITY = 0;
	private static final String DATASET_NAME = "PHEROMONE_GRAYSCALE_INFO";
	private static final String GREYSCALE_VALUE_ATTRIBUTE = "greyscaleValue";
	private static final String PHEROMONE_VALUE_ATTRIBUTE = "pheromoneValue";

	private int numberOfClusters;
	private Environment environment;

	public KmeansClassifier(Environment environment, int numberOfClusters) {
		this.environment = environment;
		this.numberOfClusters = numberOfClusters;
	}

	public int[][] generateSegmentedImage() throws Exception {
		Instances instances = getInstancesFromMatrix();
		double[] clusterAssignments = getClusterAssignments(instances);
		int[][] resultMatrix = new int[environment.getNumberOfRows()][environment
				.getNumberOfColumns()];

		int pixelCounter = 0;
		for (int i = 0; i < environment.getNumberOfRows(); i++) {
			for (int j = 0; j < environment.getNumberOfColumns(); j++) {
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

	private Instances getInstancesFromMatrix() throws IOException {
		FastVector atributes = new FastVector();
		atributes.addElement(new Attribute(PHEROMONE_VALUE_ATTRIBUTE));
		atributes.addElement(new Attribute(GREYSCALE_VALUE_ATTRIBUTE));

		Instances instances = new Instances(DATASET_NAME, atributes,
				INITIAL_CAPACITY);

		int[][] normalizedPheromoneMatrix = environment
				.getNormalizedPheromoneMatrix(ProblemConfiguration.GRAYSCALE_MAX_RANGE);
		System.out.println("Generating pheromone distribution image");
		ImageUtilities.generateImageFromArray(normalizedPheromoneMatrix,
				ProblemConfiguration.PHEROMONE_IMAGE_FILE);

		for (int i = 0; i < environment.getNumberOfRows(); i++) {
			for (int j = 0; j < environment.getNumberOfColumns(); j++) {
				Instance instance = new Instance(atributes.size());
				instance.setValue(0, normalizedPheromoneMatrix[i][j]);
				instance.setValue(1, environment.getImageGraph()[i][j]);
				instances.add(instance);
			}
		}
		return instances;
	}

}
