package oo.com.iseu.UI.Basic;

public class SliceResultInformation {
	public boolean isCorrect;
	public boolean isRedundant;
	public double correctness;
	
	public SliceResultInformation(boolean isCorrect,boolean isRedundant, double correctness) {
		super();
		this.isCorrect = isCorrect;
		this.isRedundant = isRedundant;
		this.correctness = correctness;
	}
	
}
