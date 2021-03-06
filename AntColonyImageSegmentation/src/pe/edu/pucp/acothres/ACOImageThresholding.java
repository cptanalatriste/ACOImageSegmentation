package pe.edu.pucp.acothres;

import java.util.Date;

import pe.edu.pucp.acosthres.image.ImageFileHelper;
import pe.edu.pucp.acothres.ant.AntColony;
import pe.edu.pucp.acothres.ant.Environment;
import pe.edu.pucp.acothres.cluster.KmeansClassifier;
import pe.edu.pucp.acothres.exper.TestSuite;

public class ACOImageThresholding {

	private Environment environment;
	private AntColony antColony;

	public ACOImageThresholding(Environment environment) {
		this.environment = environment;
		this.antColony = new AntColony(environment,
				ProblemConfiguration.NUMBER_OF_STEPS);
	}

	private void solveProblem() throws Exception {
		this.environment.initializePheromoneMatrix();
		int iteration = 0;
		System.out.println("STARTING ITERATIONS");
		System.out.println("Number of iterations: "
				+ ProblemConfiguration.MAX_ITERATIONS);
		while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
			System.out.println("Current iteration: " + iteration);
			this.antColony.clearAntSolutions();
			this.antColony
					.buildSolutions(ProblemConfiguration.DEPOSITE_PHEROMONE_ONLINE);
			System.out.println("UPDATING PHEROMONE TRAILS");
			if (!ProblemConfiguration.DEPOSITE_PHEROMONE_ONLINE) {
				this.antColony.depositPheromone();
			}
			this.environment.performEvaporation();
			iteration++;
		}
		System.out.println("EXECUTION FINISHED");
		// TODO(cgavidia): We're not storing best tour or it's lenght
	}

	public static int[][] getSegmentedImageAsArray(String imageFile,
			boolean generateOutputFiles) throws Exception {
		System.out.println("ACO FOR IMAGE THRESHOLDING");
		System.out.println("=============================");

		System.out.println("Data file: " + imageFile);

		int[][] imageGraph = ImageFileHelper.getImageArrayFromFile(imageFile);

		System.out.println("Generating original image from matrix");
		ImageFileHelper.generateImageFromArray(imageGraph,
				ProblemConfiguration.OUTPUT_DIRECTORY
						+ ProblemConfiguration.ORIGINAL_IMAGE_FILE);
		System.out.println("Starting background filtering process");
		imageGraph = ImageFileHelper.removeBackgroundPixels(imageGraph);

		Environment environment = new Environment(imageGraph);
		ACOImageThresholding acoImageSegmentation = new ACOImageThresholding(
				environment);
		System.out.println("Starting computation at: " + new Date());
		long startTime = System.nanoTime();

		acoImageSegmentation.solveProblem();

		System.out.println("Starting K-means clustering");

		// TODO(cgavidia): There should a method to get the number of
		// cl�sters automatically. Also, son preprocessing or postprocessing
		// would improve quality.
		KmeansClassifier classifier = new KmeansClassifier(environment,
				ProblemConfiguration.NUMBER_OF_CLUSTERS);
		int[][] segmentedImageAsMatrix = classifier.generateSegmentedImage();

		segmentedImageAsMatrix = ImageFileHelper.openImage(
				segmentedImageAsMatrix,
				ProblemConfiguration.OPENING_REPETITION_PARAMETER);

		long endTime = System.nanoTime();
		System.out.println("Finishing computation at: " + new Date());
		System.out.println("Duration (in seconds): "
				+ ((double) (endTime - startTime) / 1000000000.0));

		if (generateOutputFiles) {
			System.out.println("Generating segmented image");
			ImageFileHelper.generateImageFromArray(segmentedImageAsMatrix,
					ProblemConfiguration.OUTPUT_DIRECTORY
							+ ProblemConfiguration.OUTPUT_IMAGE_FILE);

			System.out.println("Generating images per cluster");
			for (int i = 0; i < classifier.getNumberOfClusters(); i++) {
				ImageFileHelper.generateImageFromArray(
						classifier.generateSegmentedImagePerCluster(i),
						ProblemConfiguration.OUTPUT_DIRECTORY + i + "_"
								+ ProblemConfiguration.CLUSTER_IMAGE_FILE);
			}
		}
		return segmentedImageAsMatrix;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String imageFile = ProblemConfiguration.INPUT_DIRECTORY
					+ ProblemConfiguration.IMAGE_FILE;
			ACOImageThresholding.getSegmentedImageAsArray(imageFile, true);
			new TestSuite().executeReport();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
