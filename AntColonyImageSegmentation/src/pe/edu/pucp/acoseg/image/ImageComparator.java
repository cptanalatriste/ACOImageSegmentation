package pe.edu.pucp.acoseg.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.acoseg.ProblemConfiguration;

public class ImageComparator {

	private static final int GREYSCALE_POSITIVE_THRESHOLD = 15;

	private String description;
	private double truePositives = 0;
	private double falsePositives = 0;
	private double falseNegatives = 0;

	public ImageComparator(String description, int[][] referenceImage,
			int[][] imageToValidate) throws Exception {
		this.description = description;
		if (referenceImage.length != imageToValidate.length
				|| referenceImage[0].length != imageToValidate[0].length) {
			throw new Exception("Images are not comparable");
		}

		for (int i = 0; i < referenceImage.length; i++) {
			for (int j = 0; j < referenceImage[0].length; j++) {

				if (referenceImage[i][j] >= GREYSCALE_POSITIVE_THRESHOLD
						&& imageToValidate[i][j] >= GREYSCALE_POSITIVE_THRESHOLD) {
					truePositives++;
				} else if (referenceImage[i][j] >= GREYSCALE_POSITIVE_THRESHOLD
						&& imageToValidate[i][j] < GREYSCALE_POSITIVE_THRESHOLD) {
					falseNegatives++;
				} else if (referenceImage[i][j] < GREYSCALE_POSITIVE_THRESHOLD
						&& imageToValidate[i][j] >= GREYSCALE_POSITIVE_THRESHOLD) {
					falsePositives++;
				}
			}
		}
	}

	public ImageComparator(String description, String referenceImageFile,
			String imageToValidateFile) throws IOException, Exception {
		this(description, ImageFileHelper
				.getImageArrayFromFile(referenceImageFile), ImageFileHelper
				.getImageArrayFromFile(imageToValidateFile));
	}

	public double getBuildingDetectionPercentage() {
		return truePositives / (truePositives + falseNegatives);
	}

	public double getBranchingFactor() {
		return falsePositives / truePositives;
	}

	public String resultAsString() {
		return description + ": BDP = " + getBuildingDetectionPercentage()
				+ " BF = " + getBranchingFactor();
	}

	public static void main(String[] args) {
		try {
			List<ImageComparator> testSuite = new ArrayList<ImageComparator>();
			testSuite.add(new ImageComparator("CSF",
					ProblemConfiguration.INPUT_DIRECTORY
							+ "csf_21130transverse1_64.gif",
					ProblemConfiguration.OUTPUT_DIRECTORY + "10_cluster.bmp"));
			testSuite.add(new ImageComparator("Grey Matter",
					ProblemConfiguration.INPUT_DIRECTORY
							+ "grey_20342transverse1_64.gif",
					ProblemConfiguration.OUTPUT_DIRECTORY + "7_cluster.bmp"));
			testSuite.add(new ImageComparator("White Matter",
					ProblemConfiguration.INPUT_DIRECTORY
							+ "white_20358transverse1_64.gif",
					ProblemConfiguration.OUTPUT_DIRECTORY + "11_cluster.bmp"));

			for (ImageComparator comparator : testSuite) {
				System.out.println(comparator.resultAsString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
