package NoMAS;
import java.util.*;

/**
 * Representation of a solution (a subnetwork) to the problem of the log-rank score maximization (or minimization) presented in details in {@link https://doi.org/10.3389/fgene.2019.00265}.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 * 
 */
public class Solution{
	/**
	 * Fixed header used to print summary statistic about the represented subnetwork.
	 */
	public static final String HEADER =
	"Mutation count\tlog-rank\tnormalized log-rank\tp-value\tpermutation p-value\tsingle-gene score";
	
	/**
	 * Array of bitsets (see {@link Bitstring}) representing the presence of the mutations in patients' genes that are part of the subnetwork relative to the solution represented by this instance.
	 */
	public int[] x;
	/**
	 * Array of bitsets (see {@link Bitstring}) representing the presence of the mutations in the validation set of patients' genes that are part of the subnetwork relative to the solution represented by this instance when using a holdout approach for statistical validation.
	 */
	public int[] xcv;
	/**
	 * Array of bitsets (see {@link Bitstring}) representing the presence of the mutations in patients' genes that are part of the subnetwork relative to the solution represented by this instance when using a holdout approach for statistical validation.
	 */
	public int[] xall;
	/**
	 * Number of patients with at least a mutation in genes part of the subnetwork represented by this instance.
	 */
	public int m1;
	/**
	 * Number of patients of the training set with at least a mutation in genes part of the subnetwork represented by this instance when using a holdout approach for statistical validation.
	 */
	public int m1cv;
	/**
	 * Number of patients with at least a mutation in genes part of the subnetwork represented by this instance when using a holdout approach for statistical validation.
	 */
	public int m1all;
	/**
	 * Logrank statistic value of the subnetwork represented by this instance.
	 */
	public double lr;
	/**
	 * Logrank statistic value of the subnetwork represented by this instance when using a holdout approach for statistical validation.
	 */
	public double lrcv;
	/**
	 * Normalized logrank statistic value of the subnetwork represented by this instance.
	 */
	public double nlr;
	/**
	 * p-value of the given log-rank statistic of the solution under the permutational distribution using permutation sampling.
	 */
	public double pv;
	/**
	 * P-value of the given log-rank statistic of the solution under the permutational distribution using permutation sampling.
	 * It is computed using the validation group of patients when using the holdout approach for statistical validation.
	 */
	public double pcv;
	/**
	 * Experimental permutation p-value of the solution obtained by repeatedly permuting the data.
	 */
	public double ppv;
	/**
	 * Score of the solution represented by this instance.
	 */
	public double score;
	/**
	 * {@link ArrayList} of the instances of {@link Vertex} representing the subnetwork solution of the computational problem.
	 */
	public ArrayList<Vertex> vertices;
	/**
	 * Next instance of {@link Solution} in the list of solutions (check also {@link SolutionList}).
	 */
	public Solution next;
	
	/**
	 * Default constructor that simply initializes an empty {@link ArrayList} of {@link Vertex} instances.
	 */
	public Solution() {
		vertices = new ArrayList<Vertex>();
	}
	
	/**
	 * Constructs a solution made of a single vertex. 
	 * 
	 * @param v Instance of {@link Vertex} representing the vertex of the network. 
	 * @param model Instance of {@link Model} containing input data.
	 */
	public Solution(Vertex v, Model model) {
		this();
		vertices.add(v);
		x = Arrays.copyOf(v.gene.x, v.gene.x.length);
		computeLogrankStatistic(model);
	}

	
	/**
	 * Constructs a solution made of a single vertex and assigns a score.
	 * 
	 * @param v Instance of {@link Model} containing input data.
	 * @param score The score to assign.
	 * @param model Instance of {@link Model} containing input data.
	 */
	public Solution(Vertex v, double score, Model model) {
		this();
		vertices.add(v);
		this.score = score;
		x = Arrays.copyOf(v.gene.x, v.gene.x.length);
	}
	
	/**
	 * Computes the logrank statistic value of the current subnetwork.
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 */
	public void computeLogrankStatistic(Model model) {
		Bitstring.logrankAndCount(this, model.w);
		nlr = model.normalizeLogrankStatistic(lr, m1);
	}
	
	/**
	 * Computes the logrank statistic value of the current subnetwork using the validation set of patients when using the holdout approach for statistical validation.
	 * 
	 * @param control Instance of {@link Model} containing data about the validation group of patients.
	 */
	public void computeLogrankStatisticCrossval(Model control) {
		Bitstring.logrankAndCountCrossval(this, control.w);		
	}
	
	/**
	 * Prints the summary statistics of the current solution.
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @return
	 */
	public String asString(Model model) {
		String s = m1+"\t"+lr+"\t"+nlr+"\t"+pv+"\t"+ppv+"\t"+score+"\t#";
		for(Vertex v : vertices) {
			s += "\t"+model.genes[v.id].symbol;
		}
		return s;
	}

	/**
	 * Counts the number of patients with at least a mutated gene among the current solution subnetwork.
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 */
	public void computePopulationVector(Model model) {
		x = Bitstring.getEmpty(model.m);
		for(Vertex v : vertices) {
			x = Bitstring.logicalOR(x, v.gene.x);
		}
		m1 = Bitstring.numberOfSetBits(x);
	}
	
	/**
	 * Counts the number of patients in the validation set with at least a mutated gene among the current solution subnetwork when using a holdout approach for statistical validation.
	 * 
	 * @param control Instance of {@link Model} containing data about the validation group of patients.
	 * @param all Instance of {@link Model} containing data about all patients.
	 */
	public void computePopulationVectorCrossval(Model control, Model all) {
		xcv = Bitstring.getEmpty(control.m);
		for(Vertex v : vertices) {
			Vertex vc = Graph.getVertexBySymbol(control, v.gene.symbol);
			xcv = Bitstring.logicalOR(xcv, vc.gene.x);
		}
		m1cv = Bitstring.numberOfSetBits(xcv);
		
		xall = Bitstring.getEmpty(all.m);
		for(Vertex v : vertices) {
			Vertex vc = Graph.getVertexBySymbol(all, v.gene.symbol);
			xall = Bitstring.logicalOR(xall, vc.gene.x);
		}
		m1all = Bitstring.numberOfSetBits(xall);
	}
	
	/**
	 * Counts the number of patients with at least a mutated gene among the current solution subnetwork.
	 * 
	 * @param m Total number of patients analyzed.
	 */
	public void computePopulationVector(int m) {
		x = Bitstring.getEmpty(m);
		for(Vertex v : vertices) {
			x = Bitstring.logicalOR(x, v.gene.x);
		}
		m1 = Bitstring.numberOfSetBits(x);
	}
	
	/**
	 * Compares two {@link Solution} instances, determining if the two solutions are identical (i.e. they representi the same subnetwork)
	 * 
	 * @param a The first {@link Solution} instance to compare.
	 * @param b The second {@link Solution} instance to compare.
	 * @return
	 */
	public static boolean isEqual(Solution a, Solution b) {
		if(a.vertices.size() != b.vertices.size()) {
			return false;
		}
		for(Vertex v : a.vertices) {
			if(!b.vertices.contains(v)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Counts the number of vertexes that differ between two solutions (example: it returns 1 if both subnetworks share the same vertexes expect of one)
	 * 
	 * @param a The first {@link Solution} instance to check.
	 * @param b The second {@link Solution} instance to check.
	 * @return the number of vertexes that differ between two solutions.
	 */
	public static int uniqueness(Solution a, Solution b) {
		if(a.vertices.size() != b.vertices.size()) {
			return Math.abs(a.vertices.size() - b.vertices.size());
		}
		int unique = 0;
		for(Vertex v : a.vertices) {
			if(!b.vertices.contains(v)) {
				unique++;
			}
		}
		return unique;
	}
	
	/**
	 * Merges two existing {@link Solution} instances in a new {@link Solution} instance. The original {@link Solution} instances are kept.
	 * 
	 * @param a The first {@link Solution} instance to merge.
	 * @param b The second {@link Solution} instance to merge.
	 * @param model Instance of {@link Model} containing input data.
	 * @return The resulting {@link Solution} instance.
	 */
	public static Solution merge(Solution a, Solution b, Model model) {
		Solution s = new Solution();
		s.vertices.addAll(a.vertices);
		s.vertices.addAll(b.vertices);
		s.x = Bitstring.logicalOR(a.x, b.x);
		s.computeLogrankStatistic(model);
		return s;
	}

	/**
	 * Merges two existing {@link Solution} instances in a new {@link Solution} instance, assigning as score the sum of the scores (additive score variant). 
	 * The original {@link Solution} instances are kept.
	 * 
	 * @param a The first {@link Solution} instance to merge.
	 * @param b The first {@link Solution} instance to merge.
	 * @param model Instance of {@link Model} containing input data.
	 * @return The resulting {@link Solution} instance.
	 */
	public static Solution mergeScore(Solution a, Solution b, Model model) {
		Solution s = new Solution();
		s.vertices.addAll(a.vertices);
		s.vertices.addAll(b.vertices);
		s.score = a.score + b.score;
		return s;
	}
	
	/**
	 * Adds a vertex to an existing {@link Solution} instance and stores the result in a new {@link Solution} instance.
	 * The original {@link Solution} instance is kept.
	 * 
	 * @param solution The {@link Solution} instance to merge.
	 * @param v {@link Vertex} instance representing the vertex to add.
	 * @param model Instance of {@link Model} containing input data.
	 * @return The resulting {@link Solution} instance.
	 */
	public static Solution merge(Solution solution, Vertex v, Model model) {
		return merge(solution, new Solution(v, model), model);
	}

	/**
	 * Adds a vertex to an existing {@link Solution} instance and stores the result in a new {@link Solution} instance, assigning as score the sum of the score of the solution and the score of the single vertex (additive score variant).. 
	 * The original {@link Solution} instance is kept.
	 * 
	 * @param solution The {@link Solution} instance to merge.
	 * @param v {@link Vertex} instance representing the vertex to add.
	 * @param model Instance of {@link Model} containing input data.
	 * @return The resulting {@link Solution} instance.
	 */
	public static Solution mergeScore(Solution solution, Vertex v, Model model) {
		return merge(solution, new Solution(v, model), model);
	}
	
	/**
	 * Creates an {@link ArrayList} of {@link Vertex} instances representing all the vertexes that appears at least once in a collection of solutions. 
	 * 
	 * @param solutions The list of {@link Solution} instances to analyze.
	 * @return The {@link ArrayList} of vertexes.
	 */
	public static ArrayList<Vertex> getVertices(Solution... solutions) {
		ArrayList<Vertex> set = new ArrayList<Vertex>();
		for(Solution solution : solutions) {
			for(Vertex v : solution.vertices) {
				if(!set.contains(v)) {
					set.add(v);
				}
			}
		}
		return set;
	}
	
	/**
	 * Creates an {@link ArrayList} of {@link Vertex} instances representing all the vertexes that appears at least once in a collection of solutions (up to a certain limit).
	 * 
	 * @param limit Maximum number of {@link Solution} instances to process.
	 * @param solutions The list of {@link Solution} instances to analyze.
	 * @return The {@link ArrayList} of vertexes.
	 */
	public static ArrayList<Vertex> getVertices(int limit, Solution... solutions) {
		ArrayList<Vertex> set = new ArrayList<Vertex>();
		for(int i=0; i<limit; i++) {
			Solution solution = solutions[i];
			for(Vertex v : solution.vertices) {
				if(!set.contains(v)) {
					set.add(v);
				}
			}
		}
		return set;
	}
	
	/**
	 * Computes the contributions of each vertex to the total score of the solution subnetwork.
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @param solution The {@link Solution} instance to analyze.
	 * @return The array of contributions
	 */
	public static double[] contributions(Model model, Solution solution) {
		double[] contributions = new double[solution.vertices.size()];
		for(int i=0; i<solution.vertices.size(); i++) {
			Vertex v = solution.vertices.get(i);
			int[] x = Bitstring.getEmpty(model.m);
			for(Vertex u : solution.vertices) {
				if(u != v) {
					x = Bitstring.logicalOR(x, u.gene.x);
				}
			}
			int m1 = Bitstring.numberOfSetBits(x);
			double lr = Bitstring.dotProductWithArray(x, model.w);
			double nlr = model.normalizeLogrankStatistic(lr, m1);
			contributions[i] = Utils.round(solution.nlr - nlr, 2);
		}
		return contributions;
	}
}
