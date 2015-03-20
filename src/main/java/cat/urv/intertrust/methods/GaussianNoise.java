package cat.urv.intertrust.methods;

import java.util.Random;

import cat.urv.intertrust.data.Dataset;
import cat.urv.intertrust.data.Record;

public class GaussianNoise {
	
	/**
	 * @param dataset: The Dataset to be perturbed.
	 * @param mean: Mean of the Gaussian distribution.
	 * @param variance: Variance of the Gaussian distribution.
	 * @return A perturbed Dataset with a custom Gaussian noise.
	 */
	public static Dataset addGaussianNoise (Dataset dataset, double mean, double variance) {
		Random rand = new Random();
		double noise = rand.nextGaussian() * Math.sqrt(variance) + mean;
		Dataset output = new Dataset();
		
		for (Record r : dataset.toList()) {
			Record outR = new Record(dataset.numAttributes());
			
			for (int d = 0; d < dataset.numAttributes(); d++) {
				double value = Double.valueOf(r.getAttribute(d).toString()) + noise;
				outR.setAttribute(d, value);
			}
			
			output.addRecord(outR);
		}
		
		return output;
	}
}
