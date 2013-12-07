package pe.edu.pucp.acoseg.image;

public class PosiblePixel {
	private ImagePixel imagePixel;
	private double probability;

	public PosiblePixel(ImagePixel imagePixel, double probability) {
		super();
		this.imagePixel = imagePixel;
		this.probability = probability;
	}

	public ImagePixel getImagePixel() {
		return imagePixel;
	}

	public void setImagePixel(ImagePixel imagePixel) {
		this.imagePixel = imagePixel;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

}
