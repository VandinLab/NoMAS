package NoMAS;
import java.io.*;

/**
 * Packages all the input data inside a single object. The Model represents the working data.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Model {
	public static final int HASMUTATIONS = 1;
	public static final int INTERNAL = 2;
	/**
	 * number of genes
	 */
	public int n;
	/**
	 * number of patients
	 */
	public int m;
	/**
	 * reduction conditions of the gene graph
	 */
	public int reduction_conditions;
	/**
	 * Normalizing coefficient for logrank normalization
	 */
	public double norm_coef; 
	/**
	 * minimum number of mutated patients for a gene to be considered in the algorithm
	 */
	public double mutation_threshold;
	/**
	 * Censoring information
	 */
	public int[] c;
	/**
	 * array of patients weights
	 */
	public double[] w;
	/**
	 * Scores of single genes (useful only in NoMas Additive variant)
	 */
	public double[] scores;
	/**
	 * Survival times
	 */
	public double[] times;
	/**
	 * Labels of patients
	 */
	public String[] patient_ids;
	/**
	 * array of all {@link Gene} instances representing all genes 
	 */
	public Gene[] genes;
	/**
	 * array of {@link Vertex} instances representing the gene network
	 */
	public Vertex[] vertices;
	/**
	 * Path to the file of gene network
	 */
	public String graph_file;
	/**
	 * Path to the file of the mutation data.
	 */
	public String matrix_file;
	/**
	 * {@link Output} instance that performs the log output
	 */
	public Output log;
	
	/**
	 * Default constructor that simply initializes the log
	 */
	public Model() {
		log = new Output("Log.txt", false);
	}
	
	/**
	 * Constructor that initializes the log, using a prefix to filename.
	 * 
	 * @param logname The prefix. The log file will be called "logname"Log.txt. 
	 */
	public Model(String logname) {
		log = new Output(logname+"Log.txt", false);
	}
	
	/**
	 * Normalizes the logrank statistic to the variance of the measurements 
	 * 
	 * @param lr The measure to normalize
	 * @param m1 the number of patients that contributed to the statistic
	 * @return the normalized logrank value
	 */
	public double normalizeLogrankStatistic(double lr, int m1) {
		int m2 = m-m1;
		double variance = norm_coef*((double)(m1*m2)/(double)(m*(m-1)));
		return (variance == 0.0) ? Double.MIN_VALUE : lr/Math.sqrt(variance);
	}

	/**
	 * Estimates the p-values for each gene when logrank statistic is computed considering only its mutations.
	 * 
	 * @param model {@link Model} instance with the data
	 * @param samples number of permutations to perform
	 * @param N number of threads to use
	 * @param positive flag that determines if we are considering the positive scores or negative (i.e. the positive or negative tail of the gaussian)
	 */
	public static void computeSingleGeneScores(Model model, int samples, int N, boolean positive) {
		model.scores = new double[model.n];
		for(int i=0; i<model.n; i++) {
			double lr = Bitstring.dotProductWithArray(model.genes[i].x, model.w);
			if((positive && lr > 0.0) || (!positive && lr < 0.0)) {
				double pv = Statistics.pvalue(lr, model.genes[i].m1, model, samples, N);
				model.scores[i] = -Math.log10(pv);
			}else {
				model.scores[i] = 0.0;
			}
		}
	}
	
    /**
	 * Decides whether to retain the given vertex in the network.
	 *
	 * @param v The {@link Vertex} instance to investigate.
	 * @param g The {@link Gene} instance relative to the vertex.
	 * @param flags chosen policy of graph reduction.
	 * @return true if the vertex fulfills the inclusion policy, false if not
	 */
	public static boolean includeVertex(Vertex v, Gene g, int flags) {
		if((flags & HASMUTATIONS) > 0 && g.m1 > 0) {
			return true;
		}
		if((flags & INTERNAL) > 0 && v.degree > 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a new instance of {@link Model} starting from a given instances of {@link Solution} instances written in file.
	 * File must have a line, starting with "Graph file" containing, separated by tabs, the path to the graph file, the path to the mutations file, the threshold for mutations removal and the graph reduced conditions.
	 * All values must be labeled (i.e. "Graph file"-tab-path/to/graphfile-tab-"threshold"-tab-threshold value-tab-...)
	 * 
	 * @param filename path to file to read
	 * @return The composed {@link Model} instance
	 */
	public static Model fromSolutionsFile(String filename) {
		Model model = new Model();
		BufferedReader file = Utils.bufferedReader(filename);
		String line = Utils.readLine(file);
		while(!line.startsWith("Graph file")) {
			line = Utils.readLine(file);
		}
		Graph.loadGraph(line.split("\t")[1], model);
		Mutations.loadMutationMatrix(Utils.readLine(file).split("\t")[1], model);
		Mutations.removeMutations(model, Double.parseDouble(Utils.readLine(file).split("\t")[1]));
		Graph.reduce(model, Integer.parseInt(Utils.readLine(file).split("\t")[1]));
		return model;
	}

	/**
	 * Implementation of the policy of maximization of normalized log-rank statistic.
	 */
	public static final Objective MAX_NLR = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.nlr > b.nlr) return 1;
			if(a.nlr < b.nlr) return -1;
			return 0;
		}
		public String getName() {
			return "MAX_NLR";
		}
	};

	/**
	 * Implementation of the policy of minimization of normalized log-rank statistic.
	 */
	public static final Objective MIN_NLR = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.nlr < b.nlr) return 1;
			if(a.nlr > b.nlr) return -1;
			return 0;
		}
		public String getName() {
			return "MIN_NLR";
		}
	};

	/**
	 * Implementation of the policy of maximization of single-gene score for reduced survival (can only be used with additive algorithm version).
	 */
	public static final Objective SCORE_RED = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.score > b.score) return 1;
			if(a.score < b.score) return -1;
			return 0;
		}
		public String getName() {
			return "SCORE_RED";
		}
	};
    
    /**
     * Implementation of the policy of maximization of single-gene score for increased survival (can only by used with additive algorithm version).
     */
    public static final Objective SCORE_INC = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.score > b.score) return 1;
			if(a.score < b.score) return -1;
			return 0;
		}
		public String getName() {
			return "SCORE_INC";
		}
	};
	
	/**
	 * Instantiates the correct score ranking policy, depending of the name provided.
	 * 
	 * @param name The name of the policy.
	 * @return An instance of {@link Objective} representing the policy, an error if there is no policy with such name.
	 */
	public static Objective objectiveFromName(String name) {
		if(name.equals("MAX_NLR")) {
			return MAX_NLR;
		}else if(name.equals("MIN_NLR")) {
			return MIN_NLR;
		}else if(name.equals("SCORE_RED")) {
			return SCORE_RED;
		}else if(name.equals("SCORE_INC")) {
			return SCORE_INC;
		}
		System.err.println("No such scoring function: "+name);
		System.exit(1);
		return null;
	}
}