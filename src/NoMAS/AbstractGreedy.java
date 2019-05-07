package NoMAS;

/**
 * 
 * Abstract class with core elements of a Greedy strategy to solve the optimization problem presented in 
 * <a href="https://doi.org/10.3389/fgene.2019.00265">NoMAS: A Computational Approach to Find Mutated Subnetworks Associated With Survival in Genome-Wide Cancer Studies</a>.

 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public abstract class AbstractGreedy implements Algorithm {
	/**
	 * {@link Model} containing input data.
	 */
	public Model model;
	/**
	 * {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public Configuration config;
	/**
	 * variable that tracks elapsed time.
	 */
	public double time_elapsed;
	
	/**
	 * Class constructor with base parameters
	 * 
	 * @param model instance of {@link Model} containing the input data
	 * @param config instance of {@link Configuration} containing the parameters of the algorithm
	 */
	public AbstractGreedy(Model model, Configuration config) {
		this.model = model;
		this.config = config;
	}
	
	/**
	 *{@inheritDoc}
	 */
	public double timeElapsed() {
		return time_elapsed;
	}
	
	/**
	 *{@inheritDoc}
	 */
	public Solution[] run() {
		long start_time = Utils.getTime();
		Thread[] threads = new Thread[config.N];
		final Solution[][] solutions = new Solution[config.N][10];
		for(int i=0; i<config.N; i++) {
			final int index = i;
			threads[i] = new Thread(new Runnable() {
				public void run() {
					int base = Utils.getBase(config.N, index, model.n);
					int limit = base + Utils.getJobCount(config.N, index, model.n);
					for(int j=base; j<limit; j++) {
						Solution solution = expand(model.vertices[j], config.k);
						SolutionList.insert(solutions[index], solution, config.objective);
					}	
				}
			});
			threads[i].start();
		}
		Utils.join(threads);
		time_elapsed = Utils.timeElapsed(start_time);
		return SolutionList.merge(10, config.objective, solutions);
	}
	
	/**
	 * Method that creates and expands a new solution starting from a vertex of the network using a greedy strategy.
	 * 
	 * @param v {@link Vertex} base for the solution
	 * @param k the size of the solution
	 * @return the expanded {@link Solution}
	 */
	public Solution expand(Vertex v, int k) {
		Solution solution = new Solution(v, model);
		return expand(solution, k);
	}
	
	
	/**
	 * Method that expands a {@link Solution} starting from a smaller one, using a greedy strategy.
	 * 
	 * @param s the base {@link Solution}
	 * @param k the size of the {@link Solution}
	 * @return the expanded {@link Solution}
	 */
	public abstract Solution expand(Solution s, int k);
}
