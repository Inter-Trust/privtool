package cat.urv.intertrust.methods;

public class Distortions {
	/**
	 * @param res: Resolution parameter
	 * @param pos: The position to distorsion through resolution. 
	 * @return Returns a resoluted (slightly perturbed) position.
	 */
	public static double resolution (double res, double pos) {
		return (res*Math.round(pos/res));
	}
	
	/**
	 * @param n: Number of decimal digits to approximate.
	 * @param pos: The position that will be approximated.
	 * @return Returns an approximated resolution value. 
	 */
	public static double resolution2 (int n, double pos) {
		int c = (int) Math.pow(10,n);
		return Math.rint(pos*c)/c;
	}
}
