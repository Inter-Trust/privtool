package cat.urv.intertrust.methods;
import java.util.*;

import cat.urv.intertrust.data.*;

public class SyntheticGenerators {
	

	/**
	 * Returns the centroid of a list of records.
	 * @param records the list of records.
	 * @param attrList the list of attributes the records have.
	 * @param mode 
	 * @return the centroid of the list of records.
	 */
	public static Record centroid(List<Record> records, List<Attribute> attrList) {
		
		Record record = new Record(attrList.size());
		for (Attribute a: attrList) {
			List<String> attributeValues = new ArrayList<String>();	    	
	    	
			//Adding possible attribute values of the list of records.
			for (Record r: records) {
	    		attributeValues.add(r.getAttribute(a.getNum()).toString());
	    	}
	    	
			//Numerical Centroid.
			double average = Operations.average(Utilities.listStringToDouble(attributeValues));
			if (Utilities.isNumericInteger(attributeValues.get(0)))
				record.setAttribute(a.getNum(), (int)Math.rint(average));
			else record.setAttribute(a.getNum(), average);

		}
		return record;
		
	}

	public static Double attributeCentroid (Attribute attribute, List<Record> records, int index) {
		List<String> attributeValues = new ArrayList<String>();	    	
    	
		//Adding possible attribute values of the list of records.
		for (Record r: records) {
    		attributeValues.add(r.getAttribute(index).toString());
    	}
		
		double average = Operations.average(Utilities.listStringToDouble(attributeValues));
		if (Utilities.isNumericInteger(attributeValues.get(0)))
			return Math.rint(average);
		
		else return average;
	} 
	
	/**
	 * Returns the centroid of a list of records.
	 * @param records the list of records.
	 * @param attrList the list of attributes the records have.
	 * @param mode 
	 * @return the centroid of the list of records.
	 */
	public static Record centroid(List<Record> records, List<Attribute> attrList, Dataset dataset, String mode) {
		
		Record record = new Record(attrList.size());
		for (Attribute a: attrList) {
			List<String> attributeValues = new ArrayList<String>();	    	
	    	
			//Adding possible attribute values of the list of records.
			for (Record r: records) {
	    		attributeValues.add(r.getAttribute(a.getNum()).toString());
	    	}
	    	
			double average = Operations.average(Utilities.listStringToDouble(attributeValues));
			if (Utilities.isNumericInteger(attributeValues.get(0)))
				record.setAttribute(a.getNum(), (int)Math.rint(average));
			else record.setAttribute(a.getNum(), average);
		}
		return record;
		
	}
	
	/**
	 * Given a list of records, returns a synthetic list of records.
	 * @param recordList list of records to be perturbed.
	 * @param t parameter between 0 and 1 (see the paper).
	 * @param attrList the list of attributes the records have. 
	 * @return synthetic list of records.
	 */
	public static List<Record> syntheticDataGeneration(List<Record> recordList, List<Attribute> attrList, Dataset dataset) {
		List<Record> _c = new ArrayList<Record>();
		Dataset c = new Dataset(recordList, attrList);
		c.loadVariances(false);
		
		Record _x;
		int n = attrList.size();
		int l = 0;
		
		List<List<String>> clusterValues = new ArrayList<List<String>>();
		Map<String, Double> maxDistances = new HashMap<String, Double>(); 
		
		for (Attribute a: attrList) {
			clusterValues.add(c.getAttributeValues(l));
			for (String value : clusterValues.get(l)) {
				if (!maxDistances.containsKey(value))
					maxDistances.put(value, maxDistance(value, a, c.getAttributeValues(l), dataset.getVarianceAttribute(l)));
			}
			
			l++;
		}
		
		for (Record x: recordList) {
			_x = new Record(n);
			l=0;
			for (Attribute a: attrList){
				if (a.isConfidential()) {
					_x.setAttribute(l, hybridAttribute(x.getAttribute(l).toString(), clusterValues.get(l), a, dataset.getVarianceAttribute(l), 
							maxDistances.get(x.getAttribute(l).toString())));
				}
				else _x.setAttribute(l, Double.valueOf(x.getAttribute(l).toString()));
				
				l++;
			}
			_c.add(_x);
		}
		
		return _c;
	}
	
	/**
	 * Return an hybrid value of a given attribute value.
	 * @param value the attribute value to make an hybrid value.
	 * @param attrValues list of possible values the attribute can take.
	 * @param attr the attribute.
	 * @param t parameter between 0 and 1 (see the paper).
	 * @return  an hybrid value of a given attribute value.
	 */
	private static double hybridAttribute(String value, List<String> attrValues, Attribute attr, double varAttribute, double maxDistance){
		
		double d = Double.MAX_VALUE;
		double maxV, minV;
		List<Double> aux;
		
		Random r = new Random(System.nanoTime());
		double doubleHybridValue = 0;
		double dmax = maxDistance; 
		
		aux = Utilities.listStringToDouble(attrValues);
		maxV = Utilities.max(aux);
		minV = Utilities.min(aux);
		
		while (d > dmax) {
			doubleHybridValue = r.nextDouble()*(maxV-minV)+minV;				
			d = Distances.SSE_AttrValue(Double.valueOf(value), doubleHybridValue, attr, attrValues, varAttribute);
		}
		
		return doubleHybridValue;

	}
	
	/**
	 * Returns the maximum distance between an attribute value and the values taken by attribute.
	 * @param value the attribute value.
	 * @param attribute the attribute.
	 * @param attrValues list of possible values the attribute can take.
	 * @return the maximum distance between an attribute value and the values taken by attribute.
	 */
	private static double maxDistance(String value, Attribute attribute, List<String> attrValues, double varAttribute) {
		double max = 0, sse;
		
		for (String s: attrValues) {
			sse = Distances.SSE_AttrValue(Double.valueOf(value), Double.valueOf(s), attribute, attrValues, varAttribute);	
			if (sse > max) max = sse;
		}
			
		return max;
		
	}
	
}
