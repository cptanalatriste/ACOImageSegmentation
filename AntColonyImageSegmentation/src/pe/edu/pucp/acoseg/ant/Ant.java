package pe.edu.pucp.acoseg.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pe.edu.pucp.acoseg.ProblemConfiguration;
import pe.edu.pucp.acoseg.image.ImagePixel;
import pe.edu.pucp.acoseg.image.PosiblePixel;

public class Ant {

	private int currentIndex = 0;
	private ImagePixel pixelPath[];

	// TODO(cgavidia):Visited matrix was removed because memory concerns

	public Ant(int solutionLength, int numberOfRows, int numberOfColumns) {
		this.pixelPath = new ImagePixel[solutionLength];
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public void visitPixel(ImagePixel visitedPixel) {
		pixelPath[currentIndex] = visitedPixel;
		currentIndex++;
	}

	public void clear() {
		for (int i = 0; i < pixelPath.length; i++) {
			pixelPath[i] = null;
		}
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public ImagePixel[] getPixelPath() {
		return pixelPath;
	}

	public ImagePixel selectNextPixel(double[][] pheromoneTrails,
			int[][] imageGraph) {
		// TODO(cgavidia): Not considering best-choice at all

		List<PosiblePixel> probabilities = getProbabilities(pheromoneTrails,
				imageGraph);
		Random random = new Random();
		double randomValue = random.nextDouble();
		double total = 0;
		for (PosiblePixel posiblePixel : probabilities) {
			total = total + posiblePixel.getProbability();
			if (total >= randomValue) {
				return posiblePixel.getImagePixel();
			}

		}
		return null;
	}

	private List<PosiblePixel> getProbabilities(double[][] pheromoneTrails,
			int[][] imageGraph) {
		List<PosiblePixel> pixelsWithProbabilities = new ArrayList<PosiblePixel>();
		ImagePixel currentPosition = pixelPath[currentIndex - 1];

		List<ImagePixel> neighbours = currentPosition
				.getNeighbourhood(imageGraph);
		double denominator = 0.0;
		for (ImagePixel neighbour : neighbours) {
			// We add a small number to avoid division by zero
			double heuristicValue = Math.abs(neighbour.getGreyScaleValue()
					- getMeanGrayScaleValue())
					+ ProblemConfiguration.DELTA;
			double pheromoneTrailValue = pheromoneTrails[neighbour
					.getxCoordinate()][neighbour.getyCoordinate()]
					+ ProblemConfiguration.DELTA;
			double heuristicTimesPheromone = Math.pow(heuristicValue,
					ProblemConfiguration.HEURISTIC_IMPORTANCE)
					* Math.pow(pheromoneTrailValue,
							ProblemConfiguration.PHEROMONE_IMPORTANCE);

			// Temporary, we're storing the product as probability.
			pixelsWithProbabilities.add(new PosiblePixel(neighbour,
					heuristicTimesPheromone));
			denominator = denominator + heuristicTimesPheromone;
		}

		for (PosiblePixel posiblePixel : pixelsWithProbabilities) {
			double heuristicTimesPheromone = posiblePixel.getProbability();
			// Now we're dividing by the total sum
			posiblePixel.setProbability(heuristicTimesPheromone / denominator);
		}

		return pixelsWithProbabilities;
	}

	public double getMeanGrayScaleValue() {
		double grayScaleSum = 0.0;
		for (int i = 0; i < currentIndex; i++) {
			ImagePixel currentPixel = pixelPath[i];
			grayScaleSum = grayScaleSum + currentPixel.getGreyScaleValue();
		}
		return grayScaleSum / (currentIndex);
	}
}
