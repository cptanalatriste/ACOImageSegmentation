package pe.edu.pucp.imageseg.aco;

import pe.edu.pucp.imageseg.ImagePixel;
import pe.edu.pucp.imageseg.aco.config.ProblemConfiguration;

public class AntColony {

	private Ant[] antColony;
	private int numberOfAnts;
	private int numberOfSteps;
	private Environment environment;

	public AntColony(Environment environment, int numberOfSteps) {
		this.environment = environment;
		// Ant Ant per every pixel
		this.numberOfAnts = environment.getNumberOfRows()
				* environment.getNumberOfColumns();
		System.out.println("Number of Ants in Colony: " + numberOfAnts);
		this.antColony = new Ant[numberOfAnts];
		this.numberOfSteps = numberOfSteps;
		for (int j = 0; j < antColony.length; j++) {
			antColony[j] = new Ant(numberOfSteps,
					environment.getNumberOfRows(),
					environment.getNumberOfColumns());
		}
	}

	public void buildSolutions() {
		System.out.println("BUILDING ANT SOLUTIONS");

		// TODO(cgavidia): We need to pick ants randomly
		for (Ant ant : antColony) {
			while (ant.getCurrentIndex() < numberOfSteps) {
				ImagePixel nextPixel = ant.selectNextPixel(
						environment.getPheromoneTrails(),
						environment.getImageGraph());
				ant.visitPixel(nextPixel);
			}
			// TODO(cgavidia): Local search is also omitted. No recording of
			// best solutions either.
		}
	}

	public void clearAntSolutions() {
		System.out.println("CLEARING ANT SOLUTIONS");

		int antCounter = 0;
		for (int i = 0; i < environment.getNumberOfRows(); i++) {
			for (int j = 0; j < environment.getNumberOfColumns(); j++) {
				Ant ant = antColony[antCounter];
				ImagePixel initialPixel = new ImagePixel(i, j,
						environment.getImageGraph());
				ant.clear();
				ant.setCurrentIndex(0);
				ant.visitPixel(initialPixel);
				antCounter++;
			}
		}

	}

	public void depositPheromone() {
		System.out.println("Depositing pheromone");
		// TODO(cgavidia): Best Ant for depositing pheromone is also ignored.
		for (Ant ant : antColony) {
			double contribution = 1 / (ProblemConfiguration.COST_FUNCTION_PARAMETER_A + ProblemConfiguration.COST_FUNCTION_PARAMETER_B
					* ant.getMeanGrayScaleValue());
			for (int i = 0; i < numberOfSteps; i++) {
				ImagePixel imagePixel = ant.getPixelPath()[i];
				double newValue = environment.getPheromoneTrails()[imagePixel
						.getxCoordinate()][imagePixel.getyCoordinate()]
						* ProblemConfiguration.EXTRA_WEIGHT + contribution;
				environment.getPheromoneTrails()[imagePixel.getxCoordinate()][imagePixel
						.getyCoordinate()] = newValue;
			}
		}

	}
}
