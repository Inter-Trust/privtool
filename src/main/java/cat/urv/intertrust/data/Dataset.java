package cat.urv.intertrust.data;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Represents a dataset.
 */
public class Dataset {
	enum MODE{MDAV, MDAV_ID_SWAP, MDAV_CONFIDENTIAL};
	
	private List<Record> data;	//stores the attribute's values
	private List<Attribute> variables; //stores the name of the variables and the type
	private List<Double> valueVarAttributes = new ArrayList<Double>(); //stores the variances of all Records in Dataset.
	private List<Double> valueMeanAttributes = new ArrayList<Double>(); //stores the means of all Records in Dataset.
	
	/**
     * Default constructor.
     */
	public Dataset() {
		data = new ArrayList<Record>();
		variables = new ArrayList<Attribute>();		
	}
	
	
	/**
     * Constructor.
     * @param data the values of the dataset.
     * @param variables list of attributes.
     */
	public Dataset(List<Record> data, List<Attribute> variables) {
		this.data = data;
		this.variables = variables;		
	}
	
	
	public Dataset(Dataset dst) {
		this.data = new ArrayList<Record>(dst.toList());
		this.variables = new ArrayList<Attribute>(dst.getAttributeList());
	}


	/**
	 * Returns the attribute list. It contains the name of each attribute in the
	 * data set as well as her type.
	 * @return the attribute list.
	 */
	public List<Attribute> getAttributeList() {
		return this.variables;
	}
	
	public int getAttrNumber(String name) {
		Attribute attr = null;
		
		for (Attribute a: variables){			
			if (a.getName().equalsIgnoreCase(name)) {
				attr = a;
				break;
			}
		}
		if (attr == null) return -1;
		return attr.getNum();
	}
	
	/**
	 * Returns the attribute at the specified position in the dataset.
	 * @param index index of attribute to return. 
	 * @return the attribute at the specified position in the dataset.
	 */
	public Attribute getAttribute(int index) {
		return variables.get(index);
	}
	
	
	
	
	/**
	 * Inserts the specified record at the dataset. 
     * @param r the record to be inserted.
     */
	public void addRecord(Record r)  {		
		if (data==null) data = new ArrayList<Record>();
		if (data.size() > 0 && r.size() != data.get(0).size()) 			
			new Error("Dataset.addRecord: All records in data set must have the same size");
		
		data.add(r);				
	}
		
    /**
     * Returns the record at the specified position in the dataset.
     * @param index index of record to return. 
     * @return the record at the specified position in the dataset.
     */
    public Record getRecord(int index){    	
		if (data==null)			
			new Error("Dataset.getRecord: No data found");
		return data.get(index);
    }
    
    public void setRecord(int position, Record rec) {
    	data.set(position, rec);
    }
    
    public void setQI (List<String> attributes) {
    	int i = 0;
    	
    	for (Attribute a : getAttributeList()) {
    		if (a.getName().equalsIgnoreCase(attributes.get(i))) {
    			a.setConfidentiality("quasi_identifier");
    		}
    		i++;
    	}
    }
    
    public void setConfidential (List<String> attributes) {
    	int i = 0;
    	
    	for (Attribute a : getAttributeList()) {
    		if (a.getName().equalsIgnoreCase(attributes.get(i))) {
    			a.setConfidentiality("confidential");
    		}
    		i++;
    	}
    }
    
    public void removeRecord (Record rec) {
    	if (data==null) new Error("Dataset.removeRecord: No data found");
    	data.remove(rec);
    }
    
    
    public void removeListRecords(List<Record> list) {
    	for (Record rec : list) {
    		data.remove(rec);
    	}
    }
    /**
     * Returns a string representation of the dataset.
     * @return a string representation of the dataset.
     */
    public String toString() {
    	String s="";
    	for (Record rec : this.data) {
    		s += rec.toString()+"\n";
    	}
    	return s;
    }
    
    /** Writes a File with all the records of the Dataset. **/
    public void toFile(String namefile) {
     	FileWriter f = null;		
		
		try {
			f = new FileWriter (namefile);
		
			for (Record rec : this.data) {
	    		f.write(rec.toString()+"\n");
	    	}
			f.close();		
	    
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public Dataset getConfidentialDataset(Attribute confidential) {
    	List<Attribute> confidentials = new ArrayList<Attribute>();
    	confidentials.add(confidential);
    	
    	List<Record> records = new ArrayList<Record>();
    	for (Record rec : data) {
        	List<Double> atts = new ArrayList<Double>();
        	int i = 0;
    		for (Attribute a : getAttributeList()) {
        		if (a.getName().equalsIgnoreCase(confidential.getName())) atts.add(rec.getAttribute(i));
        		i++;
        	}
        	
    		Record r = new Record(atts);
    		r.setPosDataset(rec.getPosDataset());
    		r.setGroup(rec.getGroup());
    		
    		records.add(r);
    	}
    	
    	Dataset confDst = new Dataset(records, confidentials);
    	confDst.loadVariances(false);
    	    	
    	return confDst;
    }
    
    public Dataset getQIDataset() {
    	List<Attribute> quasiidentifiers = new ArrayList<Attribute>();
    	for (Attribute a : getAttributeList()) {
    		if (a.isQuasiIdentifier()) quasiidentifiers.add(a);
    	}
    	
    	List<Record> records = new ArrayList<Record>();
    	for (Record rec : data) {
        	List<Double> atts = new ArrayList<Double>();
        	int i = 0;
    		for (Attribute a : getAttributeList()) {
        		if (a.isQuasiIdentifier()) atts.add(rec.getAttribute(i));
        		i++;
        	}
        	
    		Record r = new Record(atts);
    		r.setPosDataset(rec.getPosDataset());
    		r.setGroup(rec.getGroup());
    		
    		records.add(r);
    	}
    	
    	Dataset qiDst = new Dataset(records, quasiidentifiers);
    	qiDst.loadVariances(false);
    	
    	return qiDst;
    }
    
    /**
     * Sets the attributes specification (name and type) from a file.
     * @param namefile the name of the file.
     * @return 
     */
    void loadAttributes (String[] vars) throws Exception {
		
    	try {
			for (String var : vars) {	
				String varName = correctString(var);
				variables.add(new Attribute(variables.size(), varName));
			}
		} catch (Exception e) {
			for (int i = 0; i < variables.size(); i++) 
				variables.add(new Attribute(variables.size(), "ATT"+i));
		}
    }
    
    
    /**
     * Sets the values of the dataset from a file.
     * @param namefile the name of the file.
     */
    public void loadValues(String datafile) throws Exception{
    	FileReader fdata = null;
    	
		fdata = new FileReader (datafile);
		BufferedReader bdata = new BufferedReader(fdata);
		
		StringBuffer sb = new StringBuffer();
		String[] array;
		String[] values;
		String line, attrValue;
		Attribute attr;
					
		line=bdata.readLine();
		loadAttributes(line.split("\t"));
		
		while((line=bdata.readLine())!=null) {
			sb.append(line);
			if (!line.endsWith(";")) sb.append(";");
		}

        //read the values	         
         Record rec;
         int i, numAttAdded;
         
         values = sb.toString().split(";");
         
         for (String v : values) {						
			array = v.split(", ");
			if (array.length == 1){
				array = v.split("\t");
			}
			
			 rec = new Record(variables.size());
			 i = 0;
			 numAttAdded = 0;
			 for (String s : array) {
				 attrValue = correctString(s);
				 attrValue = attrValue.replace(',', '.');
				 
				 attr = this.getAttribute(i);
			 	 
				 if (attr.validValue(attrValue)) {
					 rec.setAttribute(i, Double.valueOf(attrValue));
					 numAttAdded++;
				 } else if (attr.nullValue(attrValue)) {
					 attrValue = attr.getRandom(this).toString();
					 rec.setAttribute(i, Double.valueOf(attrValue));
					 numAttAdded++;
				 } 
				 else new Exception("Dataset.loadValues: error loading record "+this.numRecords()+". Attribute "+i+" '"+attrValue+"' is not a correct value");
				  
				 i++;				 
				 
			 }
			 
			 if (numAttAdded == variables.size()) {
				 this.addRecord(rec);
			 }
			 else {
				 new Exception("Dataset.loadValues: Format error in "+datafile);
			 }
		 }	
		
		 fdata.close();
    }    
    
    
    /**
     * Sets the values of the dataset from a file.
     * @param namefile the name of the file.
     */
    public void loadRecords(String datafile, List<Integer> records, Map<Integer, Integer> inValAttr, boolean impute) {
    	FileReader fdata = null;
    	
    	try {
    		fdata = new FileReader (datafile);
			BufferedReader bdata = new BufferedReader(fdata);
			
			StringBuffer sb = new StringBuffer();
			String[] array;
			String[] values;
			String line, attrValue;
			//read the file
			line = bdata.readLine();
			while((line=bdata.readLine())!=null) {
				sb.append(line);
				sb.append(";");
			}

	        //read the values	         
	         Record rec;
	         int i, numAttAdded;
	         int r = 0;
	         
	         values = sb.toString().split(";");
	         for (String v : values) {						
				array = v.split(", ");
				if (array.length == 1){
					array = v.split("\t");
				}
				
				rec = new Record(variables.size());
				 i = 0;
				 numAttAdded = 0;
				 for (String s : array) {
					 attrValue = correctString(s);
		
					 if ((inValAttr == null)||((inValAttr != null)&&(inValAttr.containsKey(Integer.valueOf(i))))) {
						 int indexAtt = inValAttr.get(Integer.valueOf(i));
						 this.getAttribute(indexAtt);
					 	 
						 rec.setAttribute(indexAtt, Double.valueOf(attrValue));
						 
						 numAttAdded++;
					 } 
					 i++;
					 
					 if (attrValue.startsWith("gr_")) {
						 rec.setGroup(Integer.valueOf(attrValue.substring(3)));
					 }
					 
				 }
				 if (numAttAdded == variables.size()) {
					 this.setRecord(records.get(r), rec);
					 r++;
				 }
				 else {
					 new Exception("Dataset.loadValues: Format error in "+datafile);
				 }
				 
			 }	
	        
			
			 fdata.close();
	
		} catch (FileNotFoundException e) {
			new Exception("Dataset.loadValues: File "+datafile+ " not exist");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
			
    }
    
    
    /**
     * Returns a String without blank character at the beginning and 
     * at the end of a String.
     * @param text the String to be cleaned.
     * @return the same String without blank character at the beginning and 
     * at the end.
     */
	private String correctString (String text) {
		int ini=0, fi=0;		
		
		for (int i=0; i<text.length(); i++) {
			if (text.charAt(i) != ' ') {
				ini = i;
				break;
			}
		}
		
		for (int i=text.length()-1; i>=0; i--) {
			if (text.charAt(i) != ' ') {
				fi = i+1;
				break;
			}
		}
		
		return text.substring(ini, fi).toLowerCase();
	}
	
	public void loadPredictors (List<String> predictors) {
		for (int i = 0; i < variables.size(); i++) {
			if (predictors.contains(variables.get(i).getName().toLowerCase())) {
				variables.get(i).setPredictor("predictor");
			} else {
				variables.get(i).setPredictor("response");
			}
		}
	}
	
	
    /**
     * Checks if the attribute exists.
     * @param name the name of the attribute.
     * @return TRUE if the attribute with the specific name exists, 
     * and FALSE otherwise.
     */
	public boolean existAttribute(String name) {
		for (Attribute a: variables){
			if (a.getName().equalsIgnoreCase(name)) return true;
		}
		return false;
	}    

   /**
    * Loads and creates a dataset.
    * @param datafile file containing the data values.
    * @param namefile file containing the names and types of attributes.
    * @param hierarchiesfile file containing the hierarchies and ordinal values 
    * of attributes.
    */
	public void load(String datafile) throws Exception  {

		//create an empty dataset
		new Dataset();		
		
		//load the values of the dataset
		this.loadValues(datafile);
		
		//load the total Variances of the different attributes.
		this.loadVariances(false);
		System.out.println("Dataset loaded properly.");
	}

          /**
     * Returns the number of records stored in the dataset.
     * @return the number of records in the dataset.
     */
	public int numRecords() {
		return data.size();
	}
	
	
	 /**
     * Returns the number of attributes that records stores in the dataset have.
     * @return the number of attributes.
     */
	public int numAttributes() {
		return data.get(0).size();
	}
	
	 /**
     * Returns a list with the values of a specific attribute.
     * @param index the index of attribute.
     * @return a list with the values of the specific attribute.
     */
	public List<String> getAttributeValues (int index) {
		List<String> list = new ArrayList<String>();		
		try {
			
			for (int i=0; i<this.numRecords(); i++) {
				Record r = this.getRecord(i);
				list.add(r.getAttribute(index).toString());
			}			
			
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} 			
		return list;
	}	
	

	/**
	 * Loads the Variances of all the Attributes over the entire Dataset.
	 * @param dataset
	 */
	public void loadVariances(boolean statistics) {
		double s2 = 0;		
		
		for (int d = 0; d < this.numAttributes(); d++) {
			s2 = Operations.numericVariance(this.getAttributeValues(d));
			
			valueVarAttributes.add(s2);
		}
	}
	
	public void loadMeans() {
		valueMeanAttributes.clear();
		
		double s2 = 0;		
		
		for (int d = 0; d < this.numAttributes(); d++) {
			s2 = Operations.averageString(this.getAttributeValues(d));
			
			valueVarAttributes.add(s2);
		}
	}
	
	public void addTotalVariance(double variance) {
		valueVarAttributes.add(variance);
	}
	
	public double getVarianceAttribute (int index) {
		return valueVarAttributes.get(index);
	}
	
	 /**
     * Returns the dataset as a list of record Doubles.
     * @return the list of records stored in the dataset.
     */
    public List<Record> toList() {
    	return data;
    }
    
    
    /**
     * Returns the dataset as a map of record Doubles. The key values will be the 
     * index position in the dataset.
     * @return the dataset as a map of record Doubles.
     */
    public Map<Integer, Record> toMap() {
    	int i=0;
    	
    	Map<Integer, Record> map = new HashMap<Integer, Record>();
    	
    	for (Record r : this.data) {
    		map.put(i, r);
    		//map.put(r.getPosDataset(), r);
    		i++;
    	}
    	return map;
    }

    public int size() {
    	return data.size();
    }

	public double computeCovarianceContinuous(List<String> valuesVar1, List<String> valuesVar2) {
		double covariance = 0.0, cen1 = 0.0, cen2 = 0.0;
				
		double mean1 = Operations.averageString(valuesVar1), mean2 = Operations.averageString(valuesVar2);
		
		double value = 0.0;
		for (int i = 0; i < valuesVar1.size(); i++) {
			cen1 = Double.valueOf(valuesVar1.get(i));
			cen2 = Double.valueOf(valuesVar2.get(i));
			
			value = (cen1 - mean1) * (cen2-mean2);
			
			covariance += value; 
		}
			
		return (covariance/valuesVar1.size());
	}
	
	/** GRufian (24-01-2012) --- Mean of the ith Attribute. **/
	public double getMeanAttribute(int i) {
		List<String> values = this.getAttributeValues(i);
		List<Double> doubleValues = new ArrayList<Double>();
		for (String v : values) {
			doubleValues.add(Double.parseDouble(v));
		}
		
		return Operations.average(doubleValues);
	}
			
	public static Double toIndexAttribute(String value, Attribute att) {
		if (!att.nullValue(value)) {
			return Math.floor(Double.parseDouble(value));
			
		} else {
			return 0.0;
		}
	}


	public Double getMeanValue(int i) {
		return this.valueMeanAttributes.get(i);
	}


	public void toFileTab(String nameFile) {
     	FileWriter f = null;		
		
		try {
			f = new FileWriter (nameFile);
		
			for (Record rec : this.data) {
	    		f.write(rec.toTabString()+"\n");
	    	}
			
			f.close();		
	    
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	public List<Attribute> getQIAttributeList() {
		List<Attribute> qiAtts = new ArrayList<Attribute>();
		
		for (Attribute a : getAttributeList()) {
			if (a.isQuasiIdentifier()) qiAtts.add(a);
		}
		
		return qiAtts;
	}


	public List<Attribute> getConfAttributeList() {
		List<Attribute> confidentialAtts = new ArrayList<Attribute>();
		
		for (Attribute a : getAttributeList()) {
			if (a.isConfidential()) confidentialAtts.add(a);
		}
		
		return confidentialAtts;
	}

	public double[] getAttributeDoubleValues(int attribute) {
		double[] values = new double[data.size()];
		
		for (int i=0; i<this.numRecords(); i++) {
			Record r = this.getRecord(i);
			values[i] = Double.valueOf(r.getAttribute(attribute).toString());
		}	
		
		
		return values;
	}

	public String[][] toStringArray() {
		String[][] result = new String[data.size()][this.getAttributeList().size()];
		
		for (int i = 0; i < data.size(); i++) {
			List<Double> variables = data.get(i).getData();
			for (int j = 0; j < variables.size(); j++) {
				result[i][j] = variables.get(j).toString();
			}
		}
		
		return result;
	}

	public double[][] toDoubleArray() {
		double[][] result = new double[data.size()][this.getAttributeList().size()];
		
		for (int i = 0; i < data.size(); i++) {
			List<Double> variables = data.get(i).getData();
			for (int j = 0; j < variables.size(); j++) {
				result[i][j] = Double.valueOf(variables.get(j).toString()).doubleValue();
			}
		}
		
		return result;
	}
	
	public List<Integer> getPredictors() {
		List<Integer> indexes = new ArrayList<Integer>();
		
		for (Attribute a : variables) {
			if (a.isPredictor()) {
				indexes.add(a.getNum());
			}
		}
		
		return indexes;
	}


	public List<Integer> getResponses() {
		List<Integer> indexes = new ArrayList<Integer>();
		
		for (Attribute a : variables) {
			if (!a.isPredictor()) {
				indexes.add(a.getNum());
			}
		}
		
		return indexes;
	}


	public void setAttribute(int numAtt, double[] column) {
		int i = 0;
		for (Record r : data) {
			r.setAttribute(numAtt, column[i]);
			i++;
		}
	}
	
	public void setAttribute(int numAtt, String[] column) {
		int i = 0;
		for (Record r : data) {
			r.setAttribute(numAtt, Double.valueOf(column[i]));
			i++;
		}
	}

}
