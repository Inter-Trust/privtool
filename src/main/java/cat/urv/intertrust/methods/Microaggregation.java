package cat.urv.intertrust.methods;
import java.io.File;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import cat.urv.intertrust.data.*;
import cat.urv.intertrust.data.Error;

public class Microaggregation {
	private static boolean DEBUG = false;
	static DecimalFormat dFormat = new DecimalFormat("####.###");
	private static boolean binary;
	
	 /**
     * Returns the index of the two records in the dataset at greatest SSE-distance.
     * @param dataset the dataset.
     * @param matrixDistance the matrix distance.     * 
     * @return the index of the two extreme records.
     */
	private static List<Integer> getExtremeRecords(Dataset dataset) {
		double md = 0, dist;
		int r1 = 0, r2 = 1, size = dataset.numRecords();
		List<Integer> extremeRecords = new ArrayList<Integer>();
		
		for (int i=0; i<size; i++) {
			for (int j=i+1; j<size; j++) {
				dist = Distances.SSE_distance(dataset.getRecord(i), dataset.getRecord(j), dataset);
				if (dist > md){
					md = dist;
					r1 = i;
					r2 = j;
				}
			}
		}		
		
		extremeRecords.add(r1);
		extremeRecords.add(r2);	
		return extremeRecords;		
	}
		
	 /**
     * Returns the index of the num nearest records at shortest SSE-distance 
     * from the record given.
     * @param record the index of the record in the dataset.
     * @param num the number of nearest records to be found.
     * @param dataset The dataset.
     * @param matrixDistance the matrix distance
     * @return the index of the num nearest records around the record given.
     */
	private static List<Integer> getNearestRecords(int record, int num, Dataset dataset, Map<Integer, Record> unassignedNodes) {
		if (num >= dataset.numRecords()-1) {
			new Error("Too many records in getNearestRecords");			
		}
		
		List<Integer> list = new ArrayList<Integer>();
		List<Integer> nearestRec = new ArrayList<Integer>();
		
		Map<Integer, Double> nodeDistances = new HashMap <Integer, Double>();
		
		for (int i=0; i<dataset.numRecords(); i++) {
			if ((i!= record)&&(unassignedNodes.containsKey(i))) {
				nodeDistances.put(i, Distances.SSE_distance(dataset.getRecord(record), dataset.getRecord(i), dataset));
			}
		}
		
		Map<Integer, Double> sortedDistances = Utilities.sortByAscendingValues(nodeDistances);
		
		int n=0;
		for (Entry<Integer, Double> e : sortedDistances.entrySet()){
			list.add(e.getKey());
			n++;
			
			if (n == num) break;
		}

		for (int i : list) {
			if (unassignedNodes.containsKey(i)) {
				nearestRec.add(i);
				unassignedNodes.remove(i);
			}
		}
		return nearestRec;
	}
	
	public static Partition MDAV(Dataset dataset, int k) {
		Partition p = new Partition(dataset, k);
		
		Dataset dst = new Dataset(dataset);
		
		Map<Integer, Record> unassignedNodes = dataset.toMap();
		
		System.out.println("Total Size: " + unassignedNodes.size());
		
		int processedRecords = 0;
		
		while (unassignedNodes.size() >= (3*k)) {	
				
			//Average Record x (mean)
			dst.loadMeans();
			Record avRecord = new Record(dst.numAttributes()); 
			
			for (int i = 0; i < dst.numAttributes(); i++) {
				avRecord.setAttribute(i, dst.getMeanValue(i));
			}
			
			//Most distant record from x (mean)
			int xR = Distances.mostDistantRecord(avRecord, dataset, unassignedNodes);
			
			//Most distant record from x_r	
			int xS = Distances.mostDistantRecord(dataset.getRecord(xR), dataset, unassignedNodes);
			
			
			List<Integer> extremeRec = new ArrayList<Integer>();
			extremeRec.add(xR); extremeRec.add(xS);
			
			for (int rec : extremeRec) {
				unassignedNodes.remove(rec);
				dst.removeRecord(dataset.getRecord(rec));
			}
			
			//Getting K-1 nearest Records for each extreme records. 2 Groups are created.
			List<Integer> list = new ArrayList<Integer>();
			
			for (int rec : extremeRec) {	
				list = getNearestRecords(rec, p.getK()-1, dataset, unassignedNodes);
				list.add(rec);
				p.addGroup(list);
				
				for (int i : list) {
					dst.removeRecord(dataset.getRecord(i));	
				}
			}
			
			processedRecords += 2*k;
			
			if (processedRecords % 1000 == 0) {
				System.out.println(processedRecords + "/" + dataset.size() + " processed.");
			}
		}
		
		
		//End of WHILE, clustering of 3k (or less) remaining records.
		if (dst.numRecords() <= (3*k -1) && dst.numRecords() >= (2*k)) {
			//Average Record x (mean)
			Record rMean = new Record(dst.numAttributes()); 
			for (int i = 0; i < dst.numAttributes(); i++) {
				rMean.setAttribute(i, dst.getMeanValue(i));
			}
			
			int r1 = Distances.mostDistantRecord(rMean, dataset, unassignedNodes);
			
			List<Integer> list = getNearestRecords(r1, p.getK()-1, dataset, unassignedNodes);
			p.addGroup(list);
			p.addGroup(new ArrayList<Integer>(unassignedNodes.keySet()));
		} else {
			p.addGroup(new ArrayList<Integer>(unassignedNodes.keySet()));
		}
		
		return p;
	}

	
	public static Dataset MDAV_ID (Dataset dataset, int k) {
		dataset.loadVariances(false);
		
		int position = 0;
		
		for (position = 0; position < dataset.numRecords(); position++) {
			dataset.getRecord(position).setPosDataset(position);
		}
		System.out.println("Start MDAV.");
		
		Partition kpartition = Microaggregation.MDAV(dataset.getQIDataset(), k);
		
		List<Record> microRecords = new ArrayList<Record>();
		for (int i=0; i<dataset.numRecords(); i++)
			microRecords.add(new Record(dataset.getRecord(i)));
		
		List<Attribute> qiAtts = dataset.getQIAttributeList();
		
		System.out.println("Start replacing centroid in groups...");
		
		for (int i=0; i<kpartition.numGroups(); i++) {
			List<Integer> recInGroup = kpartition.getGroup(i);			
			HashMap<Integer, Double> centroids = new HashMap<Integer, Double>();
			
			int numAtt = 0;
			for (Attribute a : qiAtts) {				
				Double value = SyntheticGenerators.attributeCentroid(a, kpartition.getGroupRecords(i), numAtt);
				centroids.put(a.getNum(), value);
				numAtt++;
			}
			
			for (Integer indexRec : recInGroup) {
				for (Entry<Integer, Double> entry : centroids.entrySet())
					microRecords.get(indexRec).setAttribute(entry.getKey(), entry.getValue());
			}
		}
		
		System.out.println("End of the process.");
		return new Dataset(microRecords, dataset.getAttributeList());
	}

	public static Dataset MDAV_SWAP (Dataset dataset, int k) {
		dataset.loadVariances(false);
		
		int position = 0;
		
		for (position = 0; position < dataset.numRecords(); position++) {
			dataset.getRecord(position).setPosDataset(position);
		}
		System.out.println("Start MDAV.");
		
		Partition kpartition = Microaggregation.MDAV(dataset.getQIDataset(), k);
		
		List<Record> microRecords = new ArrayList<Record>();
		for (int i=0; i<dataset.numRecords(); i++)
			microRecords.add(new Record(dataset.getRecord(i)));
		
		List<Attribute> qiAtts = dataset.getQIAttributeList();
		
		System.out.println("Start swapping data within groups...");
		
		for (int i=0; i<kpartition.numGroups(); i++) {
			List<Integer> recInGroup = kpartition.getGroup(i);	
			List<Record> qiDst = new ArrayList<Record>();
			
			for (Integer pos : recInGroup) {
				qiDst.add(dataset.getRecord(pos));
			}
			
			Dataset group = new Dataset(qiDst, dataset.getAttributeList());
			group = DataShuffling.FishYatesShuffling(group, false);
			
			int j = 0;
			for (Integer pos : kpartition.getGroup(i)) {
				for (Attribute a : qiAtts)
					microRecords.get(pos).setAttribute(a.getNum(), Double.valueOf(group.getRecord(j).getAttribute(a.getNum()).toString()));
				
				j++;
			}
		}
		
		System.out.println("End of the process.");
		
		return new Dataset(microRecords, dataset.getAttributeList());
	}	
	
	public static Dataset IR_SWAP (Dataset dataset, int k) {
		dataset.loadVariances(false);
		
		int position = 0;
		
		for (position = 0; position < dataset.numRecords(); position++) {
			dataset.getRecord(position).setPosDataset(position);
		}
		
		List<Record> microRecords = new ArrayList<Record>();
		for (int i=0; i<dataset.numRecords(); i++)
			microRecords.add(new Record(dataset.getRecord(i)));
		
		System.out.println("Start Confidential MDAV + Swapping within groups...");
		
		for (Attribute att : dataset.getConfAttributeList()) {
			Partition kpartition = Microaggregation.MDAV(dataset.getConfidentialDataset(att), k);
					
			for (int i=0; i<kpartition.numGroups(); i++) {
				List<Integer> recInGroup = kpartition.getGroup(i);
				List<Record> confDst = new ArrayList<Record>();
				
				for (Integer pos : recInGroup) {
					confDst.add(dataset.getRecord(pos));
				}
				
				Dataset group = new Dataset(confDst, dataset.getAttributeList());
				group = DataShuffling.FishYatesShuffling(group, true);
								
				int j = 0;
				for (Integer pos : kpartition.getGroup(i)) {
					for (Attribute a : dataset.getConfAttributeList())
						microRecords.get(pos).setAttribute(a.getNum(), Double.valueOf(group.getRecord(j).getAttribute(a.getNum()).toString()));
					
					j++;
				}
			}
		
		}
		
		System.out.println("End of the process.");
		
		return new Dataset(microRecords, dataset.getAttributeList());
	}
	
	/**
	 * Executes the microhybrid algorithm.
	 * @param dataset the dataset to be perturbed.
	 * @param parms the parameter list.
	 * @param k the parameter of microaggregation.
	 * @param dirResult 
	 * @return a new perturbed dataset using the microhybrid method.
	 */
	public static Dataset microHybrid(Dataset dataset, List<Double> parms, int k, int code, File dirResult) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println(dateFormat.format(new Date()));
		
		for (int position = 0; position < dataset.numRecords(); position++) {
			dataset.getRecord(position).setPosDataset(position);
		}
		
		dataset.loadVariances(false);

		Partition kpartition = null;
		
		if (dataset.getRecord(0).getGroup() > 0) {
			kpartition = Microaggregation.createGroups(dataset,k);
		} else {
			kpartition = Microaggregation.MDAV(dataset, k);
		}
		
		if (DEBUG) System.out.println(kpartition+"\n\n-- Synthetic data --");
		
		//10times Replication.	
		for (int z = 1; z < 2; z++) {
			List<Record> c, _c;
			List<Integer> recInGroup;
			
			List<Record> v = new ArrayList<Record>();
			for (int i=0; i<dataset.numRecords(); i++)
				v.add(new Record(dataset.numAttributes()));
				
			System.out.println("Genering synthetic data...");
			for (int i=0; i<kpartition.numGroups(); i++) {
				c  = kpartition.getGroupRecords(i);
				recInGroup = kpartition.getGroup(i);
				_c = SyntheticGenerators.syntheticDataGeneration(c, kpartition.getAttributeList(), dataset);
				
				for (int j=0; j<recInGroup.size(); j++) {
					if (DEBUG) System.out.println(recInGroup.get(j)+": "+_c.get(j).toString());
					_c.get(j).setGroup(i);
					v.set(recInGroup.get(j), _c.get(j));
				}
				if (DEBUG) System.out.println("-----------");
			}
			
			new Dataset(v, dataset.getAttributeList()).toFile(dirResult.getAbsolutePath() + "/MH"+z+".data");
			Utilities.fileConversor(dirResult.getAbsolutePath() + "/MH"+z+".data",dirResult.getAbsolutePath() + "/MHExcel"+z+".data");
		}
		
		
		System.out.println("Mycrohybrid completed. - " + dateFormat.format(new Date()));
		
		return new Dataset(new ArrayList<Record>(), dataset.getAttributeList());
	}
	
	private static Partition createGroups(Dataset dataset, int k) {
		int numGroups = 0;
		for (Record rec : dataset.toList()) {
			if (rec.getGroup() > numGroups) {
				numGroups = rec.getGroup();
			}
		}
		
		Partition kPartition = new Partition(dataset, k);
		
		for (int i = 0; i <= numGroups; i++) {
			List<Integer> indexGroup = new ArrayList<Integer>();
			for (Record rec: dataset.toList()) {
				if (rec.getGroup() == i) {
					indexGroup.add(rec.getPosDataset());
				}
			}
			kPartition.addGroup(indexGroup);
		}
		
		return kPartition;
	}
    
     public static Dataset MDAVMicroHybrid (Dataset dataset, int k) {	
		dataset.loadVariances(false);
		
		int position = 0;
		
		for (position = 0; position < dataset.numRecords(); position++) {
			dataset.getRecord(position).setPosDataset(position);
		}
		
		System.out.println("Start MDAV.");
		
		Partition kpartition = Microaggregation.MDAV(dataset, k);
				
		List<Record> c, _c;
		List<Record> microRecords = new ArrayList<Record>();
		
		for (int i=0; i<dataset.numRecords(); i++)
			microRecords.add(new Record(dataset.numAttributes()));
	
			
		System.out.println("Start generating synthetic data...");
		for (int i=0; i<kpartition.numGroups(); i++) {
			//Positions of records.
			List<Integer> recInGroup = kpartition.getGroup(i);			
			c  = kpartition.getGroupRecords(i);
			_c = SyntheticGenerators.syntheticDataGeneration(c, kpartition.getAttributeList(), dataset);
				
			//Replacing the records with the synthetic ones.
			for (int j=0; j<recInGroup.size(); j++) {
				_c.get(j).setGroup(i);
				microRecords.set(recInGroup.get(j), _c.get(j));
			}
		}
		
		System.out.println("End of the process.");
		
		return new Dataset(microRecords, dataset.getAttributeList());	
	}
	
	
	public static void setBinaryMode(boolean b) {
		Microaggregation.setBinary(b);
	}

	public static boolean isBinary() {
		return binary;
	}

	public static void setBinary(boolean binary) {
		Microaggregation.binary = binary;
	}

}
