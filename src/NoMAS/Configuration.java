package NoMAS;
import java.util.*;

/**
 * Class containing all the information about the configuration of the algorithm.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Configuration {
	/**
	 * When set to true, a time limit for NoMas execution is provided.
	 */
	public boolean timing = false;
	/**
	 * When set to true, displays a progress bar with the tracking of percentage of execution of the algorithm.
	 */
	public boolean progress = false;
	/**
	 * When set to true, returned solutions must be unique. Uniqueness is intended as  
	 */
	public boolean uniqueness = false;
	/**
	 * When set to true, it triggers the holdout approach variant (cross-evaluation) of NoMas.
	 */
	public boolean crossval = false;
	/**
	 * Number of processors. Default value is 4 when not provided.
	 */
	public int N = 4;
	/**
	 * Maximum subnetwork cardinality. Default value is 5 when not provided.
	 */
	public int k = 5;
	/**
	 * SNoMAS local search space width. It must be greater than 0 and lower than k (k + 1) / 2. Default value when 3 if not provided.
	 */
	public int kprime = 3;
	/**
	 * Number of colors to be used in color coding algorithm. Default class value is k+1 when not provided. 
	 */
	public int colors = 6;
	/**
	 * Fat Table modification size. Default value is 5 when not provided.
	 */
	public int L = 5;
	/**
	 * Number of color coding iterations. Default value is 32 when not provided.
	 */
	public int iterations = 32;
	/**
	 * Seed for random number generators. Default value is 42 when not provided.
	 */
	public int seed = 42;
	/**
	 * Number of high scoring solutions to output. Default value is 10 when not provided.
	 */
	public int solutions = 10;
	/**
	 * Time limit for algorithm execution. Default value is 0 when not provided.
	 */
	public double time = 0.0;
	/**
	 * Error probability when generating seed vertices to SNoMAS. Default value is 0.05 when not provided.
	 */
	public double seed_error = 0.05;
	/**
	 * {@link ArrayList} instance of vertex seeds to SNoMAS.
	 */
	public ArrayList<Vertex> seeds = null;
	/**
	 * Subnetwork scoring function. Default is maximization of normalized log-rank statistic if not provided. 
	 */
	public Objective objective = Model.MAX_NLR;
	/**
	 * Instance of {@link Output} where the output of the algorithm is redirected to.
	 */
	public Output output;
	
	/**
	 * Empty constructor
	 */
	public Configuration() {}
	
	/**
	 * Constructor with parameters passed as array of Strings intended as consequent couples "tag,value".
	 * Example: tokens[0] = "N", tokens[1] = number of processors, tokens[2] = "L"...
	 * Throws exception if the schema is not followed.
	 * 
	 * @param tokens as an array of {@link String} instances
	 */
	public Configuration(String[] tokens) {
		int index = -1;
		if((index = Utils.index(tokens, "k")) != -1) {
			k = Integer.parseInt(tokens[index+1]);
			colors = k+1;
			kprime = (k+1)/2;
		}
		if((index = Utils.index(tokens, "d")) != -1) {
			colors = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "kprime")) != -1) {
			kprime = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "seed")) != -1) {
			seed = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "solutions")) != -1) {
			solutions = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "iterations")) != -1) {
			iterations = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "N")) != -1) {
			N = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "L")) != -1) {
			L = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "error")) != -1) {
			double error = Double.parseDouble(tokens[index+1]);
			iterations = Statistics.iterations(error, k, colors);
            if(iterations < 1) {
                iterations = 1;
            }
		}
        if((index = Utils.index(tokens, "seederror")) != -1) {
			seed_error = Double.parseDouble(tokens[index+1]);
		}
	}
}
