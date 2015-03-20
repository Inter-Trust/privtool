package cat.urv.intertrust.data;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



/**
 * Represents an attribute.
 */
public class Attribute {
	enum CONFIDENTIALITY{confidential, quasi_identifier, identifier};
	enum PREDICTOR{predictor, response};
	
	private String name;
	private CONFIDENTIALITY confidentiality;
	private PREDICTOR predictor;
	private int num;
	
	/**
     * Constructor.
     * @param name the name of the attribute.
     * @param type the type of the attribute ("continuous", "ordinal" or "nominal").
     */
	public Attribute(int num, String name) {
		this.name = name;
		this.confidentiality = CONFIDENTIALITY.valueOf("identifier");
		this.num = num;
	}

	
	/**
     * Returns the num of the attribute.
     * @return the num of the attribute.
     */
	public int getNum() {
		return num;
	}
	
	
    /**
     * Returns the name of the attribute.
     * @return the name of the attribute.
     */
	public String getName() {
		return name;
	}

	
    /**
     * Sets the name of the attribute.
     * @param name the name of the attribute.
     */
	public void setName(String name) {
		this.name = name;
	}
	
	public void setConfidentiality(String type) {
		this.confidentiality = CONFIDENTIALITY.valueOf(type);
	}
	
	public void setPredictor(String type) {
		this.predictor = PREDICTOR.valueOf(type);
	}
		
	public boolean isPredictor() {
		if (predictor.equals(PREDICTOR.predictor)) return true;
		else return false;
	}
	
	/**
     * Checks if the attribute is confidential.
     * @return TRUE if the attribute is confidential, and FALSE otherwise.
     */
	public boolean isConfidential() {
		return (confidentiality.equals(CONFIDENTIALITY.confidential));
	}
	
	public boolean isQuasiIdentifier() {
		return (confidentiality.equals(CONFIDENTIALITY.quasi_identifier));
	}
	
	 /**
     * Returns a string representation of the attribute.
     * @return a string representation of the node.
     */
    public String toString() {
    	String s = this.getName()+": "+this.confidentiality.toString();
    	return s;
    }
        
    
    /**
     * Checks if a continuous attribute value is valid.
     * @param value value of attribute to be checked.
     * @return TRUE if the continuous attribute value is valid, and FALSE otherwise.
     */
    private boolean validContinuousValue(String value) {
    	boolean b = true;
    	try {
    		
    		Double.parseDouble(value);
    	} catch (NumberFormatException e) {
    		b = false;
    	}
    	return b;
    }
    
    
    /**
     * Checks if an attribute value is valid.
     * @param value value of attribute to be checked.
     * @return TRUE if the attribute value is valid, and FALSE otherwise.
     */
    public boolean validValue (String value) {    	    	
    	return validContinuousValue(value);
    }
	
	/**
	 * Checks if the value is null (represented with the String "?")
	 * @param value the attribute value to be checked.
	 * @return TRUE if the value is null ("?") and FALSE otherwise.
	 */
	public boolean nullValue(String value) {
		if ((value == null)||(value.equals("?"))) {
			return true;
		}
		else return false;
	}
	
	
	/**
	 * Returns a random value of the attribute.
	 * @param dataset the dataset
	 * @return a random value of the attribute.
	 */
	public Object getRandom(Dataset dataset) {
		Random r = new Random(System.nanoTime());
		
		List<Double> aux = Utilities.listStringToDouble(dataset.getAttributeValues(this.getNum()));
		double maxV = Utilities.max(aux);
		double minV = Utilities.min(aux);
		
		return r.nextDouble()*(maxV-minV)+minV;				
	}

}
