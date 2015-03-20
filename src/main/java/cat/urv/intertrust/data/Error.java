package cat.urv.intertrust.data;

/**
 * Error class.
 */

public class Error /*extends Exception*/ {
	
	/**
	 * Constructor.
	 * @param msg the message to be shown when the error is thrown.
	 */	
    public Error(String msg) {
    	System.out.println(msg);
        System.exit(-1);
    }
}