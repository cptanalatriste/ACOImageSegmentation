package pe.edu.pucp.imageseg.aco;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import pe.edu.pucp.imageseg.aco.config.ProblemConfiguration;
import pe.edu.pucp.imageseg.kmeans.KmeansClassifier;

public class ACOImageSegmentation {

	private Environment environment;
	private AntColony antColony;

	public ACOImageSegmentation(Environment environment) {
		this.environment = environment;
		this.antColony = new AntColony(environment,
				ProblemConfiguration.NUMBER_OF_STEPS);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("ACO FOR IMAGE SEGMENTATION");
		System.out.println("=============================");

		try {
			String imageFile = ProblemConfiguration.IMAGE_FILE;
			System.out.println("Data file: " + imageFile);

			int[][] imageGraph = getProblemGraphFromFile(imageFile);
			Environment environment = new Environment(imageGraph);
			ACOImageSegmentation acoImageSegmentation = new ACOImageSegmentation(
					environment);
			System.out.println("Starting computation at: " + new Date());
			long startTime = System.nanoTime();

			// TODO(cgavidia): Should we assign this?
			acoImageSegmentation.solveProblem();

			System.out.println("Generating original image from matrix");
			generateImageFromArray(imageGraph,
					ProblemConfiguration.ORIGINAL_IMAGE_FILE);

			int[][] normalizedPheromoneMatrix = environment
					.getNormalizedPheromoneMatrix(ProblemConfiguration.GRAYSCALE_MAX_RANGE);
			System.out.println("Generating pheromone distribution image");
			generateImageFromArray(normalizedPheromoneMatrix,
					ProblemConfiguration.PHEROMONE_IMAGE_FILE);
			System.out.println("Starting K-means clustering");

			KmeansClassifier classifier = new KmeansClassifier(
					normalizedPheromoneMatrix, imageGraph,
					ProblemConfiguration.NUMBER_OF_CLUSTERS);
			int[][] segmentedImageAsMatrix = classifier
					.generateSegmentedImage();

			System.out.println("Generating segmented image");
			generateImageFromArray(segmentedImageAsMatrix,
					ProblemConfiguration.OUTPUT_IMAGE_FILE);

			long endTime = System.nanoTime();
			System.out.println("Finishing computation at: " + new Date());
			System.out.println("Duration (in seconds): "
					+ ((double) (endTime - startTime) / 1000000000.0));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void solveProblem() {
		this.environment.initializePheromoneMatrix();
		int iteration = 0;
		System.out.println("STARTING ITERATIONS");
		System.out.println("Number of iterations: "
				+ ProblemConfiguration.MAX_ITERATIONS);
		while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
			System.out.println("Current iteration: " + iteration);
			this.antColony.clearAntSolutions();
			this.antColony.buildSolutions();
			System.out.println("UPDATING PHEROMONE TRAILS");
			System.out.println("Depositing pheromone");
			this.antColony.depositPheromone();
			this.environment.performEvaporation();
			iteration++;
		}
		System.out.println("EXECUTION FINISHED");
		// TODO(cgavidia): We're not storing best tour or it's lenght
	}

	private static int[][] getProblemGraphFromFile(String imageFile)
			throws IOException {

		BufferedImage image = ImageIO.read(new File(imageFile));
		Raster imageRaster = image.getData();

		int[][] imageAsArray;
		int[] pixel = new int[1];
		int[] buffer = new int[1];

		imageAsArray = new int[imageRaster.getWidth()][imageRaster.getHeight()];

		for (int i = 0; i < imageRaster.getWidth(); i++)
			for (int j = 0; j < imageRaster.getHeight(); j++) {
				pixel = imageRaster.getPixel(i, j, buffer);
				imageAsArray[i][j] = pixel[0];
			}
		return imageAsArray;
	}

	private static void generateImageFromArray(
			int[][] normalizedPheromoneMatrix, String outputImageFile)
			throws IOException {
		System.out.println("Generating output image");
		BufferedImage outputImage = new BufferedImage(
				normalizedPheromoneMatrix.length,
				normalizedPheromoneMatrix[0].length,
				BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = outputImage.getRaster();
		for (int x = 0; x < normalizedPheromoneMatrix.length; x++) {
			for (int y = 0; y < normalizedPheromoneMatrix[x].length; y++) {
				raster.setSample(x, y, 0, normalizedPheromoneMatrix[x][y]);
			}
		}
		File imageFile = new File(outputImageFile);
		ImageIO.write(outputImage, "bmp", imageFile);
		System.out.println("Resulting image stored in: " + outputImageFile);
	}
}
