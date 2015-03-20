package cat.urv.intertrust.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cat.urv.intertrust.data.Attribute;
import cat.urv.intertrust.data.Dataset;
import cat.urv.intertrust.methods.DataShuffling;
import cat.urv.intertrust.methods.Distortions;
import cat.urv.intertrust.methods.GaussianNoise;

/**
 * @author Crises Research Group - URV 2014.
 * @usage
		-resolution: Add resolution of the position. {pos1 pos2 ... posN res
		-resolution2: Approximates to N decimal digits. {pos1 pos2 ... posN N}
		-noise: Adds gausian noise.
		-shuffle: Data shuffling.
		-in: Database input path.
 *
 */
public class Main {
	public static void main (String args[]) {
		Dataset originalDataset = new Dataset();
		
		try {
			if (args.length <= 0) throw new IllegalArgumentException("Operation not found!"); 
			
			/*if (args[0].equalsIgnoreCase("-shuffle")) {
				originalDataset = loadDataset(args);
				List<String> predictors = new ArrayList<String>();
				for (int i = 0; i < args.length; i++) {
					String arg = args[i];
					if (arg.equalsIgnoreCase("-predictors")) {
						predictors.addAll(Arrays.asList(args[i+1].split(",")));
					}
				}
				
		    	for (int i = 0; i < originalDataset.numAttributes(); i++) {
		    		Attribute a = originalDataset.getAttribute(i);
		    		if (predictors.contains(a.getName())) 
		    			a.setPredictor("predictor");
		    		else 
		    			a.setPredictor("response");
		    	}
				
				DataShuffling.ShuffleData(originalDataset).toFile("shuffle.txt");
			
			} else*/ 
			if (args[0].equalsIgnoreCase("-reslos")) {
				try {
					double res = Double.valueOf(args[args.length-1]);
					PrintWriter pw = new PrintWriter(new FileWriter("resolutions.txt"));
					
					for (int i = 1; i < args.length-1; i++) {
						Double result = Distortions.resolution(res, Double.valueOf(args[i]));
						pw.write(result.toString() + "\t");
					}
					
					pw.flush();
					pw.close();
					
					System.out.println("Resolution applied properly.");
				} catch (IOException io) {}
			} else if (args[0].equalsIgnoreCase("-approx")) {
				try {
					int n = Integer.valueOf(args[args.length-1]);
					PrintWriter pw = new PrintWriter(new FileWriter("approximations.txt"));
					
					for (int i = 1; i < args.length-1; i++) {
						Double result = Distortions.resolution2(n, Double.valueOf(args[i]));
						pw.write(result.toString() + "\t");
					}
					
					pw.flush();
					pw.close();
					
					System.out.println("Decimals approximated properly.");
				} catch (IOException io) {}
			} else if (args[0].equalsIgnoreCase("-noise")) {
				originalDataset = loadDataset(args);
				double mean = 1.0f, variance = 0.0f;
				try {
					mean = Double.valueOf(args[1]);
					variance = Double.valueOf(args[2]);
				} catch (IllegalArgumentException iae) {
					System.out.println("Error loading mean & variance.");
					usage();
				}
				
				GaussianNoise.addGaussianNoise(originalDataset, mean, variance).toFile("noise.txt");
				System.out.println("Noise added properly.");
			} else if (args[0].equalsIgnoreCase("-mdav")) {
				
			} else if (args[0].equalsIgnoreCase("-help")){
				usage();
			} else {
				throw new IllegalArgumentException("Incorrect Operation!");
			}
		} catch (IllegalArgumentException notfoundoption) {
			System.out.println(notfoundoption.getMessage());
			usage();
		}
	}
	
	
	private static void usage() {
		System.out.println("InterTrust Anonimity 1.0.");
		System.out.println("Usage Parameters:");
		System.out.println("-help: Show this usage.");
		System.out.println("-reslos: Modifies resolution of the position. {pos1 pos2 ... posN res}");
		System.out.println("-approx: Approximates to N decimal digits. {pos1 pos2 ... posN N}");
		System.out.println("-noise: Adds gausian noise. It requires as arguments mean and variance.");
		//System.out.println("-shuffle: Data shuffling. It requires the predictors as parameter.");
		//System.out.println("\t-predictors predictor1,predictor2,....,predictorN");
		System.out.println("-in: Database input path. Each attribute must be separated between tabs, with a first line of attribute names.");
	}
	
	private static Dataset loadDataset(String args[]) {
		Dataset dataset = new Dataset();
		boolean loaded = false;
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equalsIgnoreCase("-in")) {
				try {
					dataset.load(args[i+1]);
					loaded = true;
				} catch (Exception ex) {
					System.out.println("ERROR loading database. Please review it.");
				}		
			}
		}
		
		if (loaded)
			return dataset;
		else throw new IllegalArgumentException("Dataset not found. Please review it!");
	}
}
