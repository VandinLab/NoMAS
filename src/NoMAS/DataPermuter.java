package NoMAS;
import java.util.*;

/**
 * Abstract class that represent a permuter for the mutation matrix. The permuter is employed when assessing the statistical significance of solutions and by calculating the experimental permutation p-values.
 * The role of this class is to shuffle the matrix of mutations, breaking all links in data between mutations and censoring information.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public abstract class DataPermuter {
	/**
	 * {@link Model} containing input data.
	 */
	public Model model;
	/**
	 * The {@link Random} object that performs randomization
	 */
	public Random rng;
	
	/**
	 * Initializes the permuter with provided {@link Model} and a seed to create the {@link Random} object that performs randomization.
	 * 
	 * @param model {@link Model} with input data
	 * @param seed {@link Integer} seed to initialize the {@link Random} object
	 */
	public void initialize(Model model, Integer seed) {
		this.model = model;
		rng = (seed != null) ? new Random(seed) : new Random();
	}
	
	/**
	 * The method that performs the permutation: its implementation depends from the null model for mutations used to permute the data 
	 */
	public abstract void permute();
	
	/**
	 * Instantiates a new DataPermuter based on a strategy passed as parameter (GI - Gene Identity model; MS - Marginal Sums model)
	 * 
	 * @param name the acronym of the model
	 * @return an instance of {@link IdentityPermuter} if parameter is "GI", a {@link SwitchPermuter} if parameter is "MS", null elsewhere (and streams an error message on the standard error)  
	 */
	public static DataPermuter permuterFromName(String name) {
		if(name.equals("GI")) {
			return new IdentityPermuter();
		}
		if(name.equals("MS")) {
			return new SwitchPermuter();
		}
		System.err.println("No such mutations null model: "+name);
		System.exit(1);
		return null;
	}
}