package pe.edu.pucp.acoseg;

public class ProblemConfiguration {

	// Credits:
	// https://www.eecs.berkeley.edu/Research/Projects/CS/vision/bsds/BSDS300/html/dataset/images/gray/296059.html

	public static final String INPUT_DIRECTORY = "C:/Users/CarlosG/Documents/GitHub/ACOImageSegmentation/AntColonyImageSegmentation/inputImg/";
	public static final String OUTPUT_DIRECTORY = "C:/Users/CarlosG/Documents/GitHub/ACOImageSegmentation/AntColonyImageSegmentation/outputImg/";

	public static final String IMAGE_FILE = "19952transverse2_64.gif";
	public static final String OUTPUT_IMAGE_FILE = "output.bmp";
	public static final String PHEROMONE_IMAGE_FILE = "pheromone.bmp";
	public static final String ORIGINAL_IMAGE_FILE = "original.bmp";
	public static final String CLUSTER_IMAGE_FILE = "cluster.bmp";

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
	public static final int NUMBER_OF_CLUSTERS = 12;

}
