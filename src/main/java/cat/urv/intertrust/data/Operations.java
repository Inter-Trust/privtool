package cat.urv.intertrust.data;

import java.util.ArrayList;
import java.util.List;


public class Operations {
    /**
     * Computes the average of the sample
     * @param values the sample values
     * @return the average of the sample
     */
	public static double average(List<Double> values) {
		double av = 0;
		for (double i : values) {
			av += i;
		}
		return av/values.size();
	}
	
	public static double averageString (List<String> values)  {
		double av = 0;
		for (String i : values) {
			av += Double.parseDouble(i);
		}
		
		return av/values.size();
	}
	
	
   /**
    * Computes the numeric variance of a sample.
    * @param valueList the sample values.
    * @return the numeric variance of the sample.
    */
	public static double numericVariance(List<String> valueList) {
		List<Double> values = new ArrayList<Double>();
		for (String s: valueList) {
			values.add(Double.parseDouble(s));
		}
		
		return variance(values);
	}
	

	public static double otherNumVariance(List<Double> valueList) {
		double variance = 0.0;
		double mean = 0.0;
		
		for (double value: valueList) {
			variance += (value*value);
			mean += value;
		}
		
		variance = variance / valueList.size();
		mean = mean / valueList.size();
		variance = variance - (mean*mean);
		
		return variance;
	}
	
	/**
	 * Computes the variance.
	 * @param values the values
	 * @return
	 */
	private static double variance(List<Double> values) {
		
		double av = average(values);
		double sum = 0;
		for (double i : values) {
			sum += ((i-av)*(i-av));
		}
		return sum/values.size();
	}
}
