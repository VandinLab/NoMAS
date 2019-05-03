package NoMAS;
import java.util.*;
import java.util.concurrent.*;

/**
 * Abstract class with core elements of NoMas algorithm to solve the optimization problem, as described in {@link https://doi.org/10.3389/fgene.2019.00265}  
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public abstract class AbstractNoMAS implements Algorithm {
	/**
	 * {@link Model} containing input data.
	 */
	public Model model;
	/**
	 * {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public Configuration config;
	/**
	 * Table of partial solutions to be filled through dynamic programming.
	 */
	public Solution[][] W;
	/**
	 * Number of rows of the partial solutions table. It coincides with the number of extracted color combinations on the vertexes graph.
	 */
	public int rows;

	private int iterations_performed;
	/**
	 * Array that stores (if filled), at colorset_to_row[i] the index of row_to_colorset relative to the combinations of colors represented by the bitstring i.
	 */
	public int[] colorset_to_row; 
	/**
	 * Array that stores, at row_to_colorset[i], a combination of colors.
	 */
	public int[] row_to_colorset;
	/**
	 * Array that stores, at colorset_groups[i] the number of combinations of at most i colors in a sequence of colors drafted randomly.
	 */
	public int[] colorset_groups;
	/**
	 * Start time of the elaboration of the algorithm, in nanoseconds.
	 */
	public long start_time;
	/**
	 * Elapsed time in elaboration since start_time. Similar to wall-clock time.
	 */
	public double time_elapsed;
	/**
	 * {@link Progressbar} instance that provides a visual aid to user to understand percentage of completion of the process.
	 */
	public Progressbar progress;

	/**
	 * Constructor that receives input data ({@link Model} instance and configuration parameters ({@link Configuration} instance)
	 * 
	 * @param model instance of {@link Model} containing input data.
	 * @param config instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public AbstractNoMAS(Model model, Configuration config) {
		this.model = model;
		this.config = config;
	}
	
	/**
	 *{@inheritDoc}
	 */
	public Solution[] run() {
		Random rng = new Random(config.seed);
		initialize();
		Solution[] list = new Solution[config.solutions];
		start_time = Utils.getTime();
		if(config.progress) {
			progress.update(0.0);
		}
		while(!isDone()) {
			iterate(rng, list);
		}
		if(config.progress) {
			progress.update(1.0);
			progress.finish();
		}
		time_elapsed = Utils.timeElapsed(start_time);
		return list;
	}
	
	/**
	 *{@inheritDoc}
	 */
	public double timeElapsed() {
		return time_elapsed;
	}

	/**
	 * Initializes data structures for the algorithm execution. 
	 */
	public void initialize() {
		rows = 0;
		for(int i=1; i<=config.k; i++) {
			rows += Utils.choose(config.colors, i);
		}
		W = new Solution[rows][model.n];
		colorset_groups = new int[config.k+1];
		colorset_to_row = new int[(int)Math.pow(2, config.colors)];
		row_to_colorset = new int[rows];
		Arrays.fill(colorset_to_row, -1);
		ArrayList<Integer> sets = new ArrayList<Integer>();
		for(int i=1; i<=config.k; i++) {
			Bitstring.enumerate(sets, 0, config.colors, i, 0);
			colorset_groups[i] = sets.size();
		}
		for(int i=0; i<sets.size(); i++) {
			colorset_to_row[sets.get(i)] = i;
			row_to_colorset[i] = sets.get(i);
		}
		iterations_performed = 0;
		progress = new Progressbar(50);
	}
	
	/**
	 * Performs a single color coding iteration and computes the table of solutions relative to the picked configuration, collecting the best solutions.
	 * 
	 * @param rng The {@link Random} random number generator to use
	 * @list The list into which the best solutions are inserted as an array of {@link Solution}
	 */
	public void iterate(Random rng, Solution[] list) {
		Graph.color(rng.nextInt(), config.colors, model);
		clearTable();
		fillTable();
		screenSolutions(list);
	}

	/**
	 * Screens the entire table for the best solutions.
	 *
	 * @param list The list into which the best solutions are inserted as an array of {@link Solution}
	 */
	public void screenSolutions(Solution[] list) {
		for(Solution[] row : W) {
			for(Solution solution : row) {
				if(solution != null) {
					if(config.uniqueness) {
						SolutionList.insertUnique(list, solution, config.objective);
					}else {
						SolutionList.insert(list, solution, config.objective);
					}
				}
			}
		}
	}
	
	/**
	 * Looks for a certain solution inside the two-dimensional table of partial solutions. Returns true if found
	 * 
	 * @param s the instance of {@link Solution} to look for
	 * @return true if found, false elsewhere
	 */
	public boolean isFound(Solution s) {
		for(Solution[] row : W) {
			for(Solution solution : row) {
				if(solution != null && solution.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Initializes all the table of partial solutions to null values 
	 */
	public void clearTable() {
		for(Solution[] row : W) {
			Arrays.fill(row, null);
		}
	}

	/**
	 * Fills out the entire table using dynamic programming and composing the solutions through partial solutions.
	 */
	public void fillTable() {
		final CyclicBarrier barrier = new CyclicBarrier(config.N);
		final int n = model.n;
		final int[] columns = Utils.indexArray(n);
		Utils.shuffle(columns);

		Thread[] threads = new Thread[config.N];
		for(int i=0; i<config.N; i++) {
			final int index = i;
			threads[i] = new Thread(new Runnable() {
				public void run() {
					int base = Utils.getBase(config.N, index, n);
					int limit = base + Utils.getJobCount(config.N, index, n);
					for(int j=base; j<limit; j++) {
						Vertex v = model.vertices[columns[j]];
						W[v.color][v.id] = computeTrivialEntry(v);
					}
					Utils.synchronize(barrier);
					for(int group=2; group<=config.k; group++) {
						int first = colorset_groups[group-1];
						int end = colorset_groups[group];
						for(int j=base; j<limit; j++) {
							Vertex v = model.vertices[columns[j]];
							if(v.degree > 0) {
								for(int r=first; r<end; r++) {
									W[r][v.id] = computeEntry(v, row_to_colorset[r], first-1, index);
								}
							}
						}
						Utils.synchronize(barrier);
					}
				}
			});
			threads[i].start();
		}
		Utils.join(threads);
	}

	/**
	 * Instantiates a new {@link Solution} with a vertex of the network and based on the passed parameters
	 * 
	 * @param v the base instance of {@link Vertex} to use to initialize the new instance of {@link Solution}
	 * @return the instantiated {@link Solution}
	 */
	public Solution computeTrivialEntry(Vertex v) {
		return new Solution(v, model);
	}
	
	/**
	 * Method that implements the strategy of the computation of the table of the partial solutions.
	 * The process computes the solutions by pivoting a vertex and computing new candidate solutions by merging the partial solutions relative to the vertex passed as parameter and its neighbors.
	 * The table is scanned backwards from the row index passed as parameter and the best among computed solutions is computed.
	 * 
	 * @param v the instance of {@link Vertex} representing the node of the network to be examined as pivot
	 * @param T bitstring (see {@link Bitstring}) representing the set of vertexes collected from the selected color coding configuration
	 * @param last index to consider in the computation of the solutions
	 * @param p implemented only in NoMas mod 2 (see https://doi.org/10.3389/fgene.2019.00265 for details about the second modified version of the algorithm)
	 * @return best solution computed among the candidates
	 */
	public abstract Solution computeEntry(Vertex v, int T, int last, int p);
	
	/**
	 * Checks if iterations limit is hit. Returns true in case.
	 * 
	 * @return true if all the iterations are performed
	 */
	public boolean isDone() {
		if(!config.timing && iterations_performed < config.iterations) {
			iterations_performed++;
			if(config.progress) {
				progress.update(iterations_performed/(double)config.iterations);
			}
			return false;
		}
		if(config.timing && Utils.timeElapsed(start_time) < config.time) {
			iterations_performed++;
			if(config.progress) {
				progress.update(Utils.timeElapsed(start_time)/config.time);
			}
			return false;
		}
		return true;
	}
}
