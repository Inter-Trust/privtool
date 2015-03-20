package cat.urv.intertrust.data;

import java.text.DecimalFormat;
import java.util.*;

public class Distances<T> {
	
	public static int mostDistantRecord(Record rec, Dataset dataset, Map<Integer, Record> unassignedNodes) {
		double md = 0, dist;
		int r1 = 0;
		
		for (int i : unassignedNodes.keySet()) {
			dist = Distances.SSE_distance(rec, dataset.getRecord(i), dataset);
			if (dist > md){
				md = dist;
				r1 = i;
			}
		}
		
		return r1;
	}
	
	public static int leastDistantRecord(Record recOriginal, Record recSynth, Dataset dataset) {
		double md = Double.MAX_VALUE, dist;
		int r1 = 0;
		
		double distOriginal = Distances.SSE_distance(recOriginal, recSynth, dataset);
				
		for (Record rec2 : dataset.toList()) {
			dist = Distances.SSE_distance(recSynth, rec2, dataset);
			if (dist < md){
				md = dist;
				r1 = rec2.getPosDataset();
				
				if (md < distOriginal) {
					return -1;
				}
			}
		}
		
		return r1;
	}
	/**
	 * Computes the SSE-distance between two records of a dataset.
	 * @param x1 the index of the first record in the dataset.
	 * @param x2 the index of the second record in the dataset.
	 * @param dataset the dataset.
	 * @return SSE-distance between the two records of a dataset.
	 */
	public static double SSE_distance(Record x1, Record x2, Dataset dataset) {
		double sse = 0;
		double num = 0, den = 0;		
		
		for (int d = 0; d < dataset.numAttributes(); d++) {					
			List<Double> records = new ArrayList<Double>();
			records.add(Double.valueOf(x1.getAttribute(d).toString()));
			records.add(Double.valueOf(x2.getAttribute(d).toString()));
		
			num = Operations.otherNumVariance(records);
			
			
			den = dataset.getVarianceAttribute(d);
			if (num!=0 && den!=0) sse += num/den;
			
			
			num = 0;
			den = 0;
		}
		return Math.sqrt(sse);
	}

	public static double SSE_distance_Hybrid(Record x1, Record x2, Dataset dataset) {
		double sse = 0;
		double num = 0, den = 0;

		
		for (int d = 0; d < dataset.numAttributes(); d++) {					
			List<String> records = new ArrayList<String>();
			records.add(x1.getAttribute(d).toString());
			records.add(x2.getAttribute(d).toString());
			
			num = Operations.numericVariance(records);
			den = dataset.getVarianceAttribute(d);
			if (num!=0 && den!=0) sse += num/den;
			
			
			num = 0;
			den = 0;
			d++;
		}
		
		
		return Math.sqrt(sse);
	}
	
	public static double SSE_Distance_Attr(Record x1, Record x2, int indexAtt, Dataset dataset) {
		double sse = 0;
		double num = 0, den = 0;
		List<String> records;

		records = new ArrayList<String>();
		records.add(x1.getAttribute(indexAtt).toString());
		records.add(x2.getAttribute(indexAtt).toString());		
					
		num = Operations.numericVariance(records);
		den = dataset.getVarianceAttribute(indexAtt);
		if (num!=0 && den!=0) sse += num/den;
		
		
		num = 0;
		den = 0;
		
		return Math.sqrt(sse);
	}

	
	/**
	 * Returns the SSE between two values belonging to the same attribute.
	 * @param x1 the first value of the attribute.
	 * @param x2 the second value of the attribute.
	 * @param attribute the attribute.
	 * @param attrValues list of possible values the attribute can take.
	 * @return the SSE between two values belonging to the same attribute.
	 */
	public static double SSE_AttrValue(Double x1, Double x2, Attribute attribute, List<String> attrValues, double attVariance) {		
		//creem primer registre
		List<Double> attr = new ArrayList<Double>(); 
		attr.add(x1);
	
		
		Record r1 = new Record(attr);
		
		//creem segon registre
		
		attr = new ArrayList<Double>();
			attr.add(x2);
		
		
		Record r2 = new Record(attr);
		
		//atribut amb el que treballem
		List<Attribute> variables = new ArrayList<Attribute>(); 
		variables.add(attribute);
		
		//llista de tots els valors possible
		List<Record> records = new ArrayList<Record>();
		for (String s: attrValues) {
			attr = new ArrayList<Double>();
			attr.add(Double.valueOf(s));
			
			records.add(new Record(attr));	
		}
		
		Dataset dataset = new Dataset(records, variables);
		dataset.addTotalVariance(attVariance);
	 
		return SSE_distance_Hybrid(r1, r2, dataset);
	}
	
	
	public static void informationLoss (Dataset real, Dataset synthetic, List<Integer> codeAttributes) {
		DecimalFormat dFormat = new DecimalFormat("####.#####");
		
		real.loadVariances(true);
		synthetic.loadVariances(true);
		
		double value = 0.0;
		for (int i  = 0; i < real.getAttributeList().size(); i++) {
			if (codeAttributes.contains(i)) {
				
				double original = real.getMeanAttribute(i);
				double synth = synthetic.getMeanAttribute(i);
				
				if (original == 0) {
					value = Math.abs(synth-original)/(Math.abs(original+1));	
				} else {
					value = Math.abs(synth-original)/Math.abs(original);
				}
				
				System.out.println("MEAN AT " + i + "\t" + dFormat.format(original) + "\t" + dFormat.format(synth) + "\t" + dFormat.format(value));
			}
		}
		
		for (int i  = 0; i < real.getAttributeList().size(); i++) {
			if (codeAttributes.contains(i)) {
				double original = real.getVarianceAttribute(i);
				double synth = synthetic.getVarianceAttribute(i);
				
				if (original == 0) {
					value = Math.abs(synth-original)/(Math.abs(original+1));	
				} else {
					value = Math.abs(synth-original)/Math.abs(original);
				}
				
				System.out.println("VARIANCE AT " + i + "\t" + dFormat.format(original) + "\t" + dFormat.format(synth) + "\t" + dFormat.format(value));
			}
		}
		
		double covarianceReal = 0.0;
		double covarianceSynth = 0.0;
		
		System.out.println("Covariance Matrix:");
		for (int i = 0; i < real.getAttributeList().size(); i++) {
				for (int j = 0; j < real.getAttributeList().size(); j++) {
				if (codeAttributes.contains(i) && codeAttributes.contains(j)) {
					covarianceReal = real.computeCovarianceContinuous(real.getAttributeValues(i), real.getAttributeValues(j));
					covarianceSynth = synthetic.computeCovarianceContinuous(synthetic.getAttributeValues(i), synthetic.getAttributeValues(j));
					
					if (covarianceReal == 0) {
						value = Math.abs(covarianceSynth-covarianceReal)/(Math.abs(covarianceReal+1));	
					} else {
						value = Math.abs(covarianceSynth-covarianceReal)/Math.abs(covarianceReal);
					}
					
					System.out.println(dFormat.format(covarianceReal) + "\t" + dFormat.format(covarianceSynth) + "\t" +dFormat.format(value) + "\t");
				}
			}
			System.out.println();
		}
	}

	
	
	public static void SSECompute(Dataset originalDataset, Dataset perturbedDataset) {
		int d = 0;
		
		double distanceAtt = 0.0;
		Record r1, r2;
		
		for (Attribute a : originalDataset.getAttributeList()) {			
			for (int i = 0; i < originalDataset.size(); i++) {
				r1 = originalDataset.getRecord(i);
				r2 = perturbedDataset.getRecord(i);
				
				distanceAtt += Math.pow(Double.parseDouble(r2.getAttribute(d).toString()) - Double.parseDouble(r1.getAttribute(d).toString()), 2);
			}
			
			System.out.println(a.getName() + " SSE Value: \t" + distanceAtt);
			
			d++;
			distanceAtt = 0.0;
		}
		
	}
}
