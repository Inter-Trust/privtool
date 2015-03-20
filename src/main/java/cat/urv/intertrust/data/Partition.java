package cat.urv.intertrust.data;
import java.util.*;
import java.io.*;

/**
 * Represents a partition.
 */
public class Partition {
	
	List<List<Integer>> groups;
	Dataset dataset;
	int k;
	//DecimalFormat dFormat = new DecimalFormat("####.###");
	
	
	/**
	 * Constructor.
	 * @param dataset the dataset.
	 * @param k microaggregation parameter.
	 */
	public Partition(Dataset dataset, int k) {
		groups = new ArrayList<List<Integer>>();
		this.dataset = dataset;
		this.k = k;
	}
	
	
	
	/**
	 * Constructor
	 * @param dataset the dataset.
	 * @param groups list of group records in the dataset which makes a partition.
	 * @param k microaggregation parameter.
	 */
	public Partition(Dataset dataset, List<List<Integer>> groups, int k) {
		this.groups = groups;
		this.dataset = dataset;
		this.k = k;
	}
	
	
	/**
	 * Returns the microaggregation parameter.
	 * @return the microaggregation parameter.
	 */
	public int getK() {
		return this.k;
	}	
	
	/**
	 * Sets the microaggregation parameter.
	 */
	public void setK(int k) {
		this.k = k;
	}
	
	/**
	 * Creates a new group with the dataset records.
	 * @param records the records of the dataset that will set up the new group.
	 */
	public void addGroup(List<Integer> records) {
		groups.add(records);
	}
	
	
	/**
	 * Check if the partition is well formed in terms of microaggregation. 
	 * Each group must have between k and 2k-1 records.
	 * @return TRUE if the partition is well formed in terms of microaggregation, 
	 * and FALSE otherwise.
	 */
	public boolean wellConstructed () {
		for (List<Integer> g : groups) {
			if ((g.size()<k)&&(g.size() != 0)) return false;
		}
		return true;
	}
	
	
	/**
	 * Returns the records in a group.
	 * @param index the group number.
	 * @return the records in the group.
	 */
	public List<Record> getGroupRecords(int index) {
		 List<Record> list =  new LinkedList<Record>();
	
		for (int i: groups.get(index)) {
			list.add(dataset.getRecord(i));
		}
		return list;
	}
	
	/**
	 * Returns the Dataset indexes' records in a group.
	 * @param index the group number.
	 * @return the records in the group.
	 */
	public List<Integer> getIndexGroupRecords (int index) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i: groups.get(index)) {
			list.add(dataset.getRecord(i).getPosDataset());
		}
		
		return list;
	}
	
	/**
	 * Return the list of indexes of those records in the dataset that belongs to 
	 * a group.
	 * @param index the group number.
	 * @return the list of indexes of those records in the dataset that belongs to 
	 * the group.
	 */
	public List<Integer> getGroup(int index) {
		return groups.get(index);
	}
	
	/**
	 * Remove a specified group of the partition.
	 * @param index the group number.
	 */
	public void removeGroup(int index) {
		groups.remove(index);
	}
	
	
	/**
	 * Merge two groups.
	 * @param g1 the group number of the first group to be merged.
	 * @param g2 the group number of the second group to be merged.
	 */
	public void merge(int g1, int g2){
		for (Integer r : this.getGroup(g2)) {
			this.getGroup(g1).add(r);
		}
		//removeGroup(g2);
	}
	
	
	
	/**
	 * Return the number of groups in the partition.
	 * @return the number of groups in the partition.
	 */
	public int numGroups() {
		return groups.size();
	}	
	
	
	
	/**
	 * Return the number of records in the partition.
	 * @return the number of records in the partition.
	 */
	public int numRecords() {		
		return dataset.numRecords();
	}
	
	 /**
     * Returns the number of attributes that records stores in the dataset have.
     * @return the number of attributes.
     */
	public int numAttributes() {		
		return dataset.numAttributes();
	}
	
	
	
    /**
     * Returns a string representation of the partition.
     * @return a string representation of the partition.
     */
    public String toString() {
    	String s = "-- Partition --\n";    	
    	for (List<Integer> g: groups) {
	    	for (Integer i : g) {
	    		s += i+": "+dataset.getRecord(i).toString()+"\n";
	    	}
	    	s += "---------------\n";
    	}

    	return s;
    }
    
    
    /**
     * Returns all the records in the partition.
     * @return all the records in the partition.
     */
    public Dataset getAllRecords() {
    	return dataset;
    }
        
    /**
     * Returns the list of a specific attributes values of the records that belong to the group.
     * @param group the group number
     * @param attr the attribute.
     * @return the list of a specific attributes values of the records that belong to the group.
     */
    public List<String> getGroupAttributeValues (int group, int attr) {
    	List<Integer> groupElements = this.getGroup(group);
    	
    	List<String> recordsInGroup = new LinkedList<String>();
    	
    	for (int i: groupElements) {
    		recordsInGroup.add(dataset.getRecord(i).getAttribute(attr).toString());
    	}
		return recordsInGroup;
	}
    
    public boolean isIdenticGroupAttribute (List<Integer> groupElements, int attr) {
    	   	
    	String att = dataset.getRecord(groupElements.get(0)).getAttribute(attr).toString();
    	//int index = dataset.getAttribute(attr).nominalToNumeric(att);
    	
    	for (int i: groupElements) {
    		/*if (index != dataset.getAttribute(attr).nominalToNumeric(dataset.getRecord(i).getAttribute(attr).toString())) {
    			return false;
    		}*/
    		if (!att.equals(dataset.getRecord(i).getAttribute(attr).toString())) return false; 
    	}
    	
    	return true;
    }
    
    
    /**
     * Returns the size of a group.
     * @param group the group number.
     * @return the size of a group.
     */
    public int groupSize(int group) {
    	return this.getGroup(group).size();
    }
       
 	/**
	 * Returns the attribute list. It contains the name of each attribute in the
	 * data set as well as her type.
	 * @return the attribute list.
	 */
	public List<Attribute> getAttributeList() {
		return dataset.getAttributeList();
	}
	
	public void toClusters(File directory) {
		int gr = 0;
		try {
			for (List<Integer> group : groups) {
				FileWriter f = new FileWriter (directory+ "/Cluster" + gr + ".txt");
				BufferedWriter bw = new BufferedWriter(f);
				
				for (int rec : group) {
					
					bw.write(rec + ": ");
					for (int l = 0; l < dataset.getAttributeList().size(); l++){
						bw.write(dataset.getRecord(rec).getAttribute(l).toString());
						bw.write(", ");
					}
					bw.write("GR_" + dataset.getRecord(rec).getGroup());
					bw.newLine();
				}
				
				bw.flush();
				bw.close();
			}
			
		} catch (IOException io) {
			
		}
	}
	
	public List<List<Integer>> getGroups () {
		return this.groups;
	}
}
