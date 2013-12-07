package pe.edu.pucp.imageseg.aco;

import pe.edu.pucp.imageseg.aco.config.ProblemConfiguration;

public class Environment {

	private int numberOfColumns;
	private int numberOfRows;

	private int[][] imageGraph;
	private double pheromoneTrails[][] = null;

	public Environment(int[][] imageGraph) {
		super();
		this.numberOfRows = imageGraph.length;
		this.numberOfColumns = imageGraph[0].length;
		System.out.println("Number of Rows: " + numberOfRows);
		System.out.println("Number of Columns: " + numberOfColumns);
		this.imageGraph = imageGraph;
		this.pheromoneTrails = new double[numberOfRows][numberOfColumns];
	}

	public int[][] getImageGraph() {
		return imageGraph;
	}

	public double[][] getPheromoneTrails() {
		return pheromoneTrails;
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void initializePheromoneMatrix() {
		// TODO(cgavidia): Not doing pheromone initialization as MMMAS for now.
		System.out.println("INITIALIZING PHEROMONE MATRIX");
		double initialPheromoneValue = ProblemConfiguration.INITIAL_PHEROMONE_VALUE;
		System.out.println("Initial pheromone value: " + initialPheromoneValue);
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfColumns; j++) {
				pheromoneTrails[i][j] = initialPheromoneValue;
			}
		}
	}

	public void performEvaporation() {
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

	public int[][] getNormalizedPheromoneMatrix(int expectedMaximum) {
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
						* expectedMaximum / (currentMax - currentMin));
			}
		}
		return normalizedPheromoneMatrix;
	}

}
