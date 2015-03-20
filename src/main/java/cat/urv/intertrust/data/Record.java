package cat.urv.intertrust.data;
import java.util.*;

/**
 * Represents a record of the dataset.
 */
public class Record {
	
	List<Double> data;
	int posDataset, idGroup;
	
	/**
     * Default constructor.
     * @param size the number of attributes.
     */
	public Record(int size) {
		data = new ArrayList<Double>();
		for (int i=0; i<size; i++) data.add(0.0);
	}
	
	
	/**
     * Constructor.
     * @param data the values of the record.
     */
	public Record(List<Double> data) {
		this.data = data;
	}
	
	public Record(Record rec) {
		data = new ArrayList<Double>();
		for (int i = 0; i<rec.getData().size(); i++) {
			data.add(rec.getData().get(i));
		}
		this.posDataset = rec.getPosDataset();
		this.idGroup = rec.getGroup();
	}
    /**
     * Sets the data of a record's attribute.
     * @param attribute the the attribute position in the record.
     * @param value the value of the attribute.
     */
	public void setAttribute(int attribute, double value) {	
		if (data==null)			
			new Error("Record not initialized");
		
		data.set(attribute, value);
	}
	
	
    /**
     * Returns the value associated with the name of the attribute.
     * @param attribute the attribute's number of the record.
     * @return the attribute's value.
     */
    public Double getAttribute(int attribute){
    	try {
    		if (data==null)			
				new Error("Attribute not found");			
    		else {
    			Double rec = data.get(attribute);
    			if (rec instanceof Double) {
    				return Double.valueOf(rec.toString());
    			} else {
    				return rec;
    			}
    		}
    			
    	} catch (IndexOutOfBoundsException e) {
 			e.printStackTrace();
 		} 
    	
    	return null;
    }
    
    
    /**
     * Returns all the values of the record.
     * @return the list of the values of the record.
     */
    public List<Double> getData(){
        return data;
    }
    
    
    /**
     * Returns a string representation of the record.
     * @return a string representation of the record.
     */
    public String toString() {
    	String s = "";
    	int i=0;
    	for (Double value: data) {
    		s+=value.toString();    		
    		i++;
    		if (i<data.size()) s+=", ";
    	}
    	return s;
    }
    
    /**
     * Returns a string representation, ready for IVEware's software.
     * @return a string representation of the record.
     */
    public String toIVEwareString(int missing) {
    	String s = "";
    	int i = 0;
    	
    	//s += this.posDataset + "\t";
    	
    	for (Double value: data) {
    		s+=value.toString();
    		
    		i++;
    		if (i<data.size()) {
    			s+="\t";
    		}
    	}
    	
    	return s;
    }
    
    
	 /**
     * Returns the number of attributes stored in the record.
     * @return the number of records of the record.
     */
    public int size() {
		return data.size();
	}


	public int getPosDataset() {
		return this.posDataset;
	}
    
    public void setPosDataset (int position) {
    	this.posDataset = position; 
    }


	public void setGroup(int group) {
		this.idGroup = group;
	}
	
	public int getGroup () {
		return this.idGroup;
	}


	public String toTabString() {
		String s = "";
    	int i=0;
    	
    	for (Double value: data) {
    		s+=value.toString();    		
    		i++;
    		if (i<data.size()) s+="\t";
    	}
    	s = s.replace('.', ',');
    	return s;
	}
}
