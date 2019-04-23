package NoMAS;
import java.util.*;

/**
 * The class representing the censoring information. Censoring information is basically represented as a 0/1 integers array.
 * Methods contained here perform elaborations on a given censoring array, all methods are static. This is basically a class of auxiliary methods  
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Censoring {
	/**
	 * Derives the weight vector from the given censoring information.
	 *
	 * @param c A binary censoring vector.
	 * @return A vector of real valued weights
	 */
	public static double[] computeWeights(int[] c) {
		int m = c.length;
		double[] w = new double[m];
		for(int i=1; i<=m; i++) {
			double sum = 0.0;
			for(int j=1; j<=i; j++) {
				sum += (double)(c[j-1])/(m-j+1);
			}
			w[i-1] = ((double)c[i-1]) - sum;
		}
		return w;
	}

	/**
	 * Computes the coefficient depending on the given censoring information
	 * which is used in the normalization of the log-rank statistic
	 *
	 * @param c A binary censoring vector
	 * @return The normalization coefficient
	 */
	public static double computeNormCoef(int[] c) {
		double sum = 0.0;
		int k = 0;
		for(int i=0; i<c.length; i++) {
			if(c[i] == 1) {
				sum += 1.0/(double)(c.length-i);
				k++;
			}
		}
		return (double)k - sum;
	}
	
    /**
	 * Compute the ratio of censored events in the given censoring vector
	 *
	 * @param c a binary censoring vector
	 * @return the ratio
	 */
	public static double censoringRatio(int[] c) {
		int censored = c.length;
		for(int i=0; i<c.length; i++) {
			censored -= c[i];
		}
		return censored/(double)c.length;
	}
	
    /**
	 * Generate a random censoring vector of the given length and censoring ratio
	 *
	 * @param rng the java.util.Random object that performs randomization
	 * @param cr the desired ratio
	 * @param m the length
	 * @return the randomized censoring vector
	 */
	public static int[] randomCensoring(Random rng, double cr, int m) {
		int[] c = new int[m];
		int uncensored = m-(int)(m*cr);
		for(int i=0; i<uncensored; i++) {
			c[i] = 1;
		}
		Utils.shuffle(rng, c);
		return c;
	}
}
