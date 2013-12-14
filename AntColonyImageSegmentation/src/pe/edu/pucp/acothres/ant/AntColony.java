package pe.edu.pucp.acothres.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.acosthres.image.ImagePixel;
import pe.edu.pucp.acothres.ProblemConfiguration;

public class AntColony {

	private List<Ant> antColony;
	private int numberOfAnts;
	private int numberOfSteps;
	private Environment environment;

	public AntColony(Environment environment, int numberOfSteps) {
		this.environment = environment;
		// Ant Ant per every pixel
		this.numberOfAnts = environment.getNumberOfRows()
				* environment.getNumberOfColumns();
		System.out.println("Number of Ants in Colony: " + numberOfAnts);
		this.antColony = new ArrayList<Ant>(numberOfAnts);
		this.numberOfSteps = numberOfSteps;
		for (int j = 0; j < numberOfAnts; j++) {
			antColony.add(new Ant(numberOfSteps, environment.getNumberOfRows(),
					environment.getNumberOfColumns()));
		}
	}

	public void buildSolutions(boolean depositPheromone) throws Exception {
		System.out.println("BUILDING ANT SOLUTIONS");

		// TODO(cgavidia): We need to pick ants randomly
		if (ProblemConfiguration.RANDOMIZE_BEFORE_BUILD) {
			Collections.shuffle(antColony);
		}

		for (Ant ant : antColony) {
			while (ant.getCurrentIndex() < numberOfSteps) {
				ImagePixel nextPixel = ant.selectNextPixel(
						environment.getPheromoneTrails(),
						environment.getImageGraph());
				if (nextPixel == null) {
					throw new Exception(
							"No pixel was selected, for ant with path: "
									+ ant.pathAsString());
				}
				ant.visitPixel(nextPixel);
			}
			if (depositPheromone) {
				depositPheromoneInAntPath(ant);
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
				Ant ant = antColony.get(antCounter);
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

		if (ProblemConfiguration.ONLY_BEST_CAN_UPDATE) {
			Ant bestAnt = getBestAnt();
			depositPheromoneInAntPath(bestAnt);
		} else {
			for (Ant ant : antColony) {
				depositPheromoneInAntPath(ant);
			}
		}
	}

	private void depositPheromoneInAntPath(Ant ant) {
		double contribution = 1 / (ProblemConfiguration.COST_FUNCTION_PARAMETER_A + ProblemConfiguration.COST_FUNCTION_PARAMETER_B
				* ant.getMeanGrayScaleValue());

		for (int i = 0; i < numberOfSteps; i++) {
			ImagePixel imagePixel = ant.getPixelPath()[i];
			double newValue = environment.getPheromoneTrails()[imagePixel
					.getxCoordinate()][imagePixel.getyCoordinate()]
					* ProblemConfiguration.EXTRA_WEIGHT + contribution;
			if (ProblemConfiguration.MMAS_PHEROMONE_UPDATE
					&& newValue < ProblemConfiguration.MINIMUM_PHEROMONE_VALUE) {
				newValue = ProblemConfiguration.MINIMUM_PHEROMONE_VALUE;
			} else if (ProblemConfiguration.MMAS_PHEROMONE_UPDATE
					&& newValue > ProblemConfiguration.MAXIMUM_PHEROMONE_VALUE) {
				newValue = ProblemConfiguration.MAXIMUM_PHEROMONE_VALUE;
			}
			environment.getPheromoneTrails()[imagePixel.getxCoordinate()][imagePixel
					.getyCoordinate()] = newValue;
		}
	}

	private Ant getBestAnt() {
		Ant bestAnt = antColony.get(0);
		for (Ant ant : antColony) {
			if (ant.getMeanGrayScaleValue() < bestAnt.getMeanGrayScaleValue()) {
				bestAnt = ant;
			}
		}
		return bestAnt;

	}
}
