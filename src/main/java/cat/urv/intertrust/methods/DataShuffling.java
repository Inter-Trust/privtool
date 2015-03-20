package cat.urv.intertrust.methods;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

import cat.urv.intertrust.data.Dataset;

import java.util.*;

public class DataShuffling {

	/**
	 * @param Dataset with the values.
	 * @return Returns a shuffled dataset.
	 */
	public static Dataset ShuffleData(Dataset dataset){
		int numAtts = dataset.getAttributeList().size();
		dataset.loadVariances(true);
		
		RealMatrix data = MatrixUtils.createRealMatrix(dataset.numRecords(), numAtts);

		for (int i = 0; i < numAtts; i++) {
			data.setColumn(i, dataset.getAttributeDoubleValues(i));
		}
				
		double[][] ranks = computeRanks(data);
		double[][] perc =  percentileRanks(ranks);
		
		double[][] normInvers = inverseNormal(perc);
		
		SpearmansCorrelation sc = new SpearmansCorrelation();
		RealMatrix corRanks = sc.computeCorrelationMatrix(data);
		
		for (int i = 0; i < corRanks.getRowDimension(); i++) {
			for (int j = 0; j < corRanks.getColumnDimension(); j++) {
				double entry = corRanks.getEntry(i, j);
				
				entry = 2 * Math.sin((Math.PI*entry)/6);
				corRanks.setEntry(i, j, entry);
			}
		}
		
		//Get Predictors Index and Responses.
		List<Integer> indPredictors = dataset.getPredictors();
		List<Integer> indResponses = dataset.getResponses();
		
		RealMatrix responses = MatrixUtils.createRealMatrix(data.getRowDimension(), indResponses.size());
		for (int i = 0; i < indResponses.size(); i++) {
			responses.setColumn(i, data.getColumn(indResponses.get(i)));
		}
		
		RealMatrix pxs = reduceMatrix(indResponses, indPredictors, corRanks);
		RealMatrix pxx = reduceMatrix(indResponses, indResponses, corRanks);
		RealMatrix psx = reduceMatrix(indPredictors, indResponses, corRanks);
		
		RealMatrix pssinv = new LUDecomposition(reduceMatrix(indPredictors, indPredictors, corRanks)).getSolver().getInverse();
		
	    double[][] responses1 = new double[normInvers.length][indResponses.size()];
	    for (int i = 0; i < responses1.length; i++) {
	    	for (int j = 0; j < indResponses.size(); j++) {
	    		responses1[i][j] = normInvers[i][indResponses.get(j)];
	    	}
	    }
	    
	    double[][] predictors1 = new double[normInvers.length][indPredictors.size()];
	    for (int i = 0; i < responses1.length; i++) {
	    	for (int j = 0; j < indPredictors.size(); j++) {
	    		predictors1[i][j] = normInvers[i][indPredictors.get(j)];
	    	}
	    }
	    
	    RealMatrix mPred1 = MatrixUtils.createRealMatrix(predictors1);
	    RealMatrix Ystar1 = mPred1.multiply(pxs.multiply(pssinv).transpose());
	    RealMatrix sigma = pxx.subtract(pxs.multiply(pssinv).multiply(psx));
	    
	    MultivariateNormalDistribution e1 = new MultivariateNormalDistribution(new double[indResponses.size()], sigma.getData());
	    double[][] sample = e1.sample(data.getRowDimension());
	    RealMatrix yStar = Ystar1.add(MatrixUtils.createRealMatrix(sample));    
	    
	    RealMatrix result = MatrixUtils.createRealMatrix(reverseMap(responses , yStar)); 
	    
	    int index = 0;
	    for (int i : indResponses) {
	    	dataset.setAttribute(i, result.getColumn(index));
	    	index++;
	    }
	    
		return dataset;
	}
	
	private static double[][] computeRanks(RealMatrix data) {
		NaturalRanking naturalRanking = new NaturalRanking(NaNStrategy.FIXED, TiesStrategy.AVERAGE);
		
		RealMatrix ranks = MatrixUtils.createRealMatrix(data.getRowDimension(), data.getColumnDimension());
		
		for (int i = 0; i < data.getColumnDimension(); i++) {
			ranks.setColumn(i, naturalRanking.rank(data.getColumn(i)));
		}
			
		return ranks.getData();
	}

	public static Dataset FishYatesShuffling(Dataset dst, boolean confidentiality) {
	    Random rnd = new Random();
	    
	    for (int att = 0; att < dst.numAttributes(); att++) {
	    	if ((!confidentiality && dst.getAttribute(att).isQuasiIdentifier()) || (confidentiality && dst.getAttribute(att).isConfidential())) {
			    String[] data = dst.getAttributeValues(att).toArray(new String[dst.numRecords()]);
			    
			    for (int i = data.length - 1; i > 0; i--)
			    {
			      int index = rnd.nextInt(i + 1);
			      // Simple swap
			      String a = data[index];
			      data[index] = data[i];
			      data[i] = a;
			    }
			    
			    dst.setAttribute(att, data);
	    	}
	    }
	    
	    return dst;
	}
	
	private static double[][] percentileRanks (double[][] ranks) {
		double[][] percentile = new double[ranks.length][ranks[0].length];
		
		for (int i = 0; i < ranks.length; i++) {
			for (int j = 0; j < ranks[i].length; j++) {
				percentile[i][j] = (ranks[i][j] - 0.5)/ranks.length;
			}
		}
		
		return percentile;
	}
	
	
	private static double[][] inverseNormal (double[][] data) {
		double[][] result = new double[data.length][data[0].length];
		
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				result[i][j] = qnorm(data[i][j]);
			}
		}
		
		return result;
	}

	private static RealMatrix reduceMatrix (List<Integer> initList, List<Integer> endList, RealMatrix matrix) {
		RealMatrix rm = MatrixUtils.createRealMatrix(initList.size(), endList.size());
		int numRow = 0;
		for (int row : initList) {
			double[] entireRow = matrix.getRow(row);
			int i = 0;
			double[] defRow = new double[endList.size()];
			
			for (int col : endList) {
				defRow[i] = entireRow[col];
				i++;
			}
			
			rm.setRow(numRow, defRow);
			numRow++;
		}
		
		return rm;
	}
	
	private static double[][] reverseMap (RealMatrix x, RealMatrix y) {
		NaturalRanking naturalRanking = new NaturalRanking(NaNStrategy.FIXED, TiesStrategy.AVERAGE);
		
		RealMatrix defData = MatrixUtils.createRealMatrix(x.getRowDimension(), x.getColumnDimension());
		
		for (int i = 0; i < x.getColumnDimension(); i++) {
			double[] column = x.getColumn(i);
			double[] reversedColumn = y.getColumn(i); 
			
			double[] orderY = naturalRanking.rank(reversedColumn); 
			orderY = order(orderY);
			
			double[] orderX = naturalRanking.rank(column); 
			orderX = order(orderX);
						
			for (int j = 0; j < orderX.length; j++) {
				column[(int)(orderX[j])] = column[(int)(orderY[j])]; 
			}
						
			for (int j = 0; j < column.length; j++) {
				reversedColumn[(int)(orderY[j])] = column[(int)(orderX[j])]; 
			}
			
			defData.setColumn(i, reversedColumn);
		}
			
		return defData.getData();
	}	  
	  
	
	private static double[] order(double[] orderX) {
		double[] order = new double[orderX.length];
		List<Double> values = new ArrayList<Double>(); 
		
		for (double d : orderX) values.add(d);
		
		int i = 0;
		while (i < order.length) { 
			order[i] = values.indexOf(Collections.min(values));
			values.set((int)order[i], Double.MAX_VALUE);
			i++;
		}
		return order;
	}

	private static double qnorm(double p) {
		double a0 = 2.515517;
		double a1 = 0.802853;
		double a2 = 0.010328;

		double b1 = 1.432788;
		double b2 = 0.189269;
		double b3 = 0.001308;

		double t = Math.pow(-2 * Math.log(1 - p), 0.5);

		return t - (a0 + a1 * t + a2 * t * t)
				/ (1 + b1 * t + b2 * t * t + b3 * t * t * t);
	}
}
