package pe.edu.pucp.acoseg.cluster;

import java.io.IOException;

import pe.edu.pucp.acoseg.ProblemConfiguration;
import pe.edu.pucp.acoseg.ant.Environment;
import pe.edu.pucp.acoseg.image.ImageFileHelper;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class KmeansClassifier {

	private static final int INITIAL_CAPACITY = 0;
	private static final String DATASET_NAME = "PHEROMONE_GRAYSCALE_INFO";
	private static final String GREYSCALE_VALUE_ATTRIBUTE = "greyscaleValue";
	private static final String PHEROMONE_VALUE_ATTRIBUTE = "pheromoneValue";

	private int numberOfClusters;
	private Environment environment;

	double[] clusterAssignments;

	public KmeansClassifier(Environment environment, int numberOfClusters) {
		this.environment = environment;
		this.numberOfClusters = numberOfClusters;
	}

	public void doCluster() throws Exception {
		Instances instances = getInstancesFromMatrix();
		clusterAssignments = getClusterAssignments(instances);
	}

	public int[][] generateSegmentedImage() throws Exception {
		if (clusterAssignments == null) {
			doCluster();
		}
		int[][] resultMatrix = new int[environment.getNumberOfRows()][environment
				.getNumberOfColumns()];

		int pixelCounter = 0;
		for (int i = 0; i < environment.getNumberOfRows(); i++) {
			for (int j = 0; j < environment.getNumberOfColumns(); j++) {
				resultMatrix[i][j] = (int) ((clusterAssignments[pixelCounter] + 1)
						/ numberOfClusters * ProblemConfiguration.GRAYSCALE_MAX_RANGE);
				pixelCounter++;
			}
		}
		return resultMatrix;
	}

	public int[][] generateSegmentedImagePerCluster(double clusterNumber)
			throws Exception {
		if (clusterAssignments == null) {
			doCluster();
		}
		int[][] resultMatrix = new int[environment.getNumberOfRows()][environment
				.getNumberOfColumns()];

		int pixelCounter = 0;
		for (int i = 0; i < environment.getNumberOfRows(); i++) {
			for (int j = 0; j < environment.getNumberOfColumns(); j++) {
				int greyscaleValue = ProblemConfiguration.GRAYSCALE_MIN_RANGE;
				if (clusterAssignments[pixelCounter] == clusterNumber) {
					greyscaleValue = ProblemConfiguration.GRAYSCALE_MAX_RANGE / 2;
				}
				resultMatrix[i][j] = greyscaleValue;
				pixelCounter++;
			}
		}
		return resultMatrix;
	}

	private double[] getClusterAssignments(Instances instances)
			throws Exception {
		// TODO(cgavidia): Maybe this can be optimized with more adequate
		// parameters.

		SimpleKMeans simpleKMeans = new SimpleKMeans();
		simpleKMeans.setNumClusters(numberOfClusters);
		simpleKMeans.buildClusterer(instances);

		ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
		clusterEvaluation.setClusterer(simpleKMeans);
		clusterEvaluation.evaluateClusterer(instances);
		double[] clusterAssignments = clusterEvaluation.getClusterAssignments();
		return clusterAssignments;
	}

	@SuppressWarnings("unused")
	private Instances getInstancesFromMatrix() throws IOException {
		FastVector atributes = new FastVector();
		if (ProblemConfiguration.USE_PHEROMONE_FOR_CLUSTERING) {
			atributes.addElement(new Attribute(PHEROMONE_VALUE_ATTRIBUTE));
		}

		if (ProblemConfiguration.USE_GREYSCALE_FOR_CLUSTERING) {
			atributes.addElement(new Attribute(GREYSCALE_VALUE_ATTRIBUTE));
		}

		Instances instances = new Instances(DATASET_NAME, atributes,
				INITIAL_CAPACITY);

		int[][] normalizedPheromoneMatrix = environment
				.getNormalizedPheromoneMatrix(ProblemConfiguration.GRAYSCALE_MAX_RANGE);
		System.out.println("Generating pheromone distribution image");
		ImageFileHelper.generateImageFromArray(normalizedPheromoneMatrix,
				ProblemConfiguration.OUTPUT_DIRECTORY
						+ ProblemConfiguration.PHEROMONE_IMAGE_FILE);

		for (int i = 0; i < environment.getNumberOfRows(); i++) {
			for (int j = 0; j < environment.getNumberOfColumns(); j++) {
				Instance instance = new Instance(atributes.size());
				if (ProblemConfiguration.USE_PHEROMONE_FOR_CLUSTERING
						&& ProblemConfiguration.USE_GREYSCALE_FOR_CLUSTERING) {
					instance.setValue(0, normalizedPheromoneMatrix[i][j]);
					instance.setValue(1, environment.getImageGraph()[i][j]);
				} else if (ProblemConfiguration.USE_PHEROMONE_FOR_CLUSTERING
						&& !ProblemConfiguration.USE_GREYSCALE_FOR_CLUSTERING) {
					instance.setValue(0, normalizedPheromoneMatrix[i][j]);
				} else if (!ProblemConfiguration.USE_PHEROMONE_FOR_CLUSTERING
						&& ProblemConfiguration.USE_GREYSCALE_FOR_CLUSTERING) {
					instance.setValue(0, environment.getImageGraph()[i][j]);
				}
				instances.add(instance);
			}
		}
		return instances;
	}

	public int getNumberOfClusters() {
		return numberOfClusters;
	}

}
