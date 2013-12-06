package pe.edu.pucp.aco.imageseg.config;

public class ProblemConfiguration {

	// Credits:
	// https://www.eecs.berkeley.edu/Research/Projects/CS/vision/bsds/BSDS300/html/dataset/images/gray/296059.html
	public static final String IMAGE_FILE = "C:/Users/CarlosG/workspace/AntColonyImageSegmentation/src/296059.jpg";
	public static final String OUTPUT_IMAGE_FILE = "C:/Users/CarlosG/workspace/AntColonyImageSegmentation/src/output.bmp";
	public static final double EVAPORATION = 0.5;

	// This are values from the original paper
	public static final int NUMBER_OF_STEPS = 15;
	public static final int MAX_ITERATIONS = 5;
	public static final int PHEROMONE_IMPORTANCE = 1;
	public static final int HEURISTIC_IMPORTANCE = 5;
	public static final double EXTRA_WEIGHT = 0.6;

	public static final int COST_FUNCTION_PARAMETER_A = 5000;
	public static final int COST_FUNCTION_PARAMETER_B = 10;

	public static final double INITIAL_PHEROMONE_VALUE = Float.MIN_VALUE;
	public static final double DELTA = Float.MIN_VALUE;

	public static final int GRAYSCALE_MIN_RANGE = 0;
	public static final int GRAYSCALE_MAX_RANGE = 255;

}
