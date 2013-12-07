package pe.edu.pucp.imageseg.aco;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import pe.edu.pucp.aco.imageseg.kmeans.KmeansClassifier;
import pe.edu.pucp.imageseg.ImagePixel;
import pe.edu.pucp.imageseg.aco.config.ProblemConfiguration;

public class ACOImageSegmentation {

	private int numberOfColumns;
	private int numberOfRows;
	private int numberOfAnts;
	private int numberOfSteps = ProblemConfiguration.NUMBER_OF_STEPS;

	private int[][] imageGraph;
	private double pheromoneTrails[][] = null;
	private Ant[] antColony;

	public ACOImageSegmentation(int[][] imageGraph) {
		this.numberOfRows = imageGraph.length;
		this.numberOfColumns = imageGraph[0].length;
		System.out.println("Number of Rows: " + numberOfRows);
		System.out.println("Number of Columns: " + numberOfColumns);

		// Ant Ant per every pixel
		this.numberOfAnts = this.numberOfRows * this.numberOfColumns;
		System.out.println("Number of Ants in Colony: " + numberOfAnts);
		this.imageGraph = imageGraph;
		this.pheromoneTrails = new double[numberOfRows][numberOfColumns];
		this.antColony = new Ant[numberOfAnts];

		for (int j = 0; j < antColony.length; j++) {
			antColony[j] = new Ant(numberOfSteps, numberOfRows, numberOfColumns);
		}
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
			ACOImageSegmentation acoImageSegmentation = new ACOImageSegmentation(
					imageGraph);
			System.out.println("Starting computation at: " + new Date());
			long startTime = System.nanoTime();

			// TODO(cgavidia): Should we assign this?
			acoImageSegmentation.solveProblem();
			
			System.out.println("Generating original image from matrix");
			generateImageFromArray(imageGraph,
					ProblemConfiguration.ORIGINAL_IMAGE_FILE);

			int[][] normalizedPheromoneMatrix = acoImageSegmentation
					.normalizePheromoneMatrix();
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

	private int[][] normalizePheromoneMatrix() {
		System.out.println("Normalizing pheromone matrix");

		int[][] normalizedPheromoneMatrix = new int[numberOfRows][numberOfColumns];
		double currentMin = 0;
		double currentMax = 0;
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				if (pheromoneTrails[i][j] < currentMin) {
					currentMin = pheromoneTrails[i][j];
				} else if (pheromoneTrails[i][j] > currentMax) {
					currentMax = pheromoneTrails[i][j];
				}
			}
		}

		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				normalizedPheromoneMatrix[i][j] = (int) ((pheromoneTrails[i][j] - currentMin)
						* ProblemConfiguration.GRAYSCALE_MAX_RANGE / (currentMax - currentMin));
			}
		}
		return normalizedPheromoneMatrix;
	}

	private void solveProblem() {
		// TODO(cgavidia): Not doing pheromone initialization as MMMAS for now.
		System.out.println("INITIALIZING PHEROMONE MATRIX");
		double initialPheromoneValue = ProblemConfiguration.INITIAL_PHEROMONE_VALUE;
		System.out.println("Initial pheromone value: " + initialPheromoneValue);
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				pheromoneTrails[i][j] = initialPheromoneValue;
			}
		}

		int iteration = 0;
		System.out.println("STARTING ITERATIONS");
		System.out.println("Number of iterations: "
				+ ProblemConfiguration.MAX_ITERATIONS);
		while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
			System.out.println("Current iteration: " + iteration);
			clearAntSolutions();
			buildSolutions();
			updatePheromoneTrails();
			iteration++;
		}
		System.out.println("EXECUTION FINISHED");
		// TODO(cgavidia): We're not storing best tour or it's lenght
	}

	private void updatePheromoneTrails() {
		System.out.println("UPDATING PHEROMONE TRAILS");

		System.out.println("Depositing pheromone");
		// TODO(cgavidia): Best Ant for depositing pheromone is also ignored.
		for (Ant ant : antColony) {
			double contribution = 1 / (ProblemConfiguration.COST_FUNCTION_PARAMETER_A + ProblemConfiguration.COST_FUNCTION_PARAMETER_B
					* ant.getMeanGrayScaleValue());
			for (int i = 0; i < numberOfSteps; i++) {
				ImagePixel imagePixel = ant.getPixelPath()[i];
				double newValue = pheromoneTrails[imagePixel.getxCoordinate()][imagePixel
						.getyCoordinate()]
						* ProblemConfiguration.EXTRA_WEIGHT
						+ contribution;
				pheromoneTrails[imagePixel.getxCoordinate()][imagePixel
						.getyCoordinate()] = newValue;
			}
		}

		System.out.println("Performing evaporation on all edges");
		System.out.println("Evaporation ratio: "
				+ ProblemConfiguration.EVAPORATION);
		// TODO(cgavidia): No minimum or maximum considered
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				double newValue = pheromoneTrails[i][j]
						* ProblemConfiguration.EVAPORATION;
				pheromoneTrails[i][j] = newValue;
			}
		}
	}

	private void buildSolutions() {
		System.out.println("BUILDING ANT SOLUTIONS");

		// TODO(cgavidia): We need to pick ants randomly
		for (Ant ant : antColony) {
			while (ant.getCurrentIndex() < numberOfSteps) {
				ImagePixel nextPixel = ant.selectNextPixel(pheromoneTrails,
						imageGraph);
				ant.visitPixel(nextPixel);
			}
			// TODO(cgavidia): Local search is also omitted. No recording of
			// best solutions either.
		}
	}

	private void clearAntSolutions() {
		System.out.println("CLEARING ANT SOLUTIONS");

		int antCounter = 0;
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				Ant ant = antColony[antCounter];
				ImagePixel initialPixel = new ImagePixel(i, j, imageGraph);
				ant.clear();
				ant.setCurrentIndex(0);
				ant.visitPixel(initialPixel);
				antCounter++;
			}
		}

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
}
