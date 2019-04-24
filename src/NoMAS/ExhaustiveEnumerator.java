package NoMAS;
import java.util.*;
import java.util.concurrent.*;

/**
 * Algorithm that implements the brute force approach to the problem resolution, by exhaustive enumeration of all subnetworks of size at most k.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class ExhaustiveEnumerator implements Algorithm {
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
	public Solution[][] solutions;
	/**
	 * {@link Progressbar} instance that provides a visual aid to user to understand percentage of completion of the process.
	 */
	public Progressbar progress;
	/**
	 * Count of the processed solutions
	 */
	private int done;
	/**
	 * Contains the order of processing of the network nodes (it stores the indexes)
	 */
	private int[] timestamps;
	/**
	 * Elapsed time in elaboration since start_time. Similar to wall-clock time.
	 */
	public double time_elapsed;
	
	/**
	 * COnstructor that initializes configuration, input and progress bar.
	 * 
	 * @param model {@link Model} containing input data.
	 * @param config {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public ExhaustiveEnumerator(Model model, Configuration config) {
		this.model = model;
		this.config = config;
		progress = new Progressbar(50);
	}
	
	/**
	 *{@inheritDoc}
	 */
	public Solution[] run() {
		solutions = new Solution[2][10];
		computeTimestamps();
		done = 0;
		progress.update(0.0);
		long start_time = Utils.getTime();
		ExecutorService pool = Executors.newFixedThreadPool(config.N);
		for(final Vertex v : model.vertices) {
			pool.submit(new Callable<Object>() {
				public Object call() {
					Solution[][] lists = new Solution[2][10];
					int[] xn = new int[model.n];
					ArrayList<Node> beta = new ArrayList<Node>();
					Node root = depth(0, v, beta, xn, timestamps[v.id]);
					traverse(root, null, lists);
					submitSolutions(lists);
					return null;
				}
			});
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}catch(Exception e) {}
		progress.update(1.0);
		progress.finish();
		time_elapsed = Utils.timeElapsed(start_time);
		System.err.println("ExhaustiveEnumerator finished in "+time_elapsed+" seconds.");
		Solution[] both_solutions = new Solution[20];
		for(int i=0; i<10; i++) {
			both_solutions[i] = solutions[0][i];
			both_solutions[10+i] = solutions[1][i];
		}
		return both_solutions;
	}
	
	/**
	 *{@inheritDoc}
	 */
	public double timeElapsed() {
		return time_elapsed;
	}
	
	/**
	 * Expands the tree of the explored subnetworks of nodes in depth
	 * 
	 * @param size Current size of explored subnetworks
	 * @param v {@link Vertex} instance to become root of the subtree to process
	 * @param beta {@link ArrayList} of {@link Node} instances to process and eventually include in the tree
	 * @param xn Array of integers, indicating if the vertex corresponding to the index have been already included in an explored subnetwork
	 * @param timestamp Current step of execution
	 * @return The root instance of {@link Node} representing the root of the tree of explored subnetworks starting from v. Returns null if the size of the explored subnetworks exceeds k
	 */
	private Node depth(int size, Vertex v, ArrayList<Node> beta, int[] xn, int timestamp) {
		size++;
		if(size > config.k) {
			return null;
		}	
		xn_add(xn, v);
		Node n = new Node(v);
		ArrayList<Node> beta_prime = new ArrayList<Node>();
		for(int i=0; i<beta.size(); i++) {
			Node n_prime = breadth(size, beta.get(i), v, xn, timestamp);
			if(n_prime != null) {
				n.addChild(n_prime);
				beta_prime.add(n_prime);
			}
		}
		for(Vertex u : v.neighbors) {
			if(timestamps[u.id] < timestamp || !xn_included(xn, u)) {
				continue;
			}
			Node n_prime = depth(size, u, beta_prime, xn, timestamp);
			if(n_prime != null) {
				n.addChild(n_prime);
				beta_prime.add(n_prime);
			}
		}	
		xn_remove(xn, v);
		return n;
	}
	
	/**
	 * Performs an "horizontal" search in the tree, to basically eliminate double entries in subnetworks exploration.
	 * Returns a new instance of {@link Node} and relative sub-subnetworks if it finds new paths
	 * 
	 * @param size Current size of explored subnetworks
	 * @param n {@link Node} instance to process
	 * @param v {@link Vertex} instance to process and check neighbors
	 * @param xn Array of integers, indicating if the vertex corresponding to the index have been already included in an explored subnetwork
	 * @param timestamp Current step of execution
	 * @return The root instance of {@link Node} representing the root of the tree of explored valid subnetworks starting from v. Returns null if the size of the explored subnetworks exceeds k
	 */
	private Node breadth(int size, Node n, Vertex v, int[] xn, int timestamp) {
		size++;
		if(xn_included(xn, n.vertex)) {
			for(Vertex u : v.neighbors) {
				if(timestamps[u.id] < timestamp) {
					continue;
				}
				if(u == n.vertex) {
					return null;
				}
			}
		}
		if(size > config.k) {
			return null;
		}
		Node n_prime = new Node(n.vertex);
		for(Node n_star : n.children) {
			Node n_dprime = breadth(size, n_star, v, xn, timestamp);
			if(n_dprime != null) {
				n_prime.addChild(n_dprime);
			}
		}
		return n_prime;
	}
	
	/**
	 * Walks the tree and fills the {@link Solution} instances table by merging close subnetworks
	 * 
	 * @param root {@link Node} instance root of the subtree to explore
	 * @param solution {@link Solution} instance to merge (parameter for recursion)
	 * @param lists Table of {@link Solution} instances to fill
	 */
	private void traverse(Node root, Solution solution, Solution[][] lists) {
		Solution candidate = new Solution(root.vertex, model);
		if(solution != null) {
			candidate = Solution.merge(solution, candidate, model);
		}
		SolutionList.insert(lists[0], candidate, Model.MIN_NLR);
		SolutionList.insert(lists[1], candidate, Model.MAX_NLR);
		for(Node child : root.children) {
			traverse(child, candidate, lists);
		}
	}
	
	/**
	 * Submits candidate solutions to the table of solutions, one for each objective function (min or max score)
	 * 
	 * @param lists Table of {@link Solution} instances to fill
	 */
	private synchronized void submitSolutions(Solution[][] lists) {
		solutions[0] = SolutionList.merge(10, Model.MIN_NLR, solutions[0], lists[0]);
		solutions[1] = SolutionList.merge(10, Model.MAX_NLR, solutions[1], lists[1]);
		done++;
		progress.update(done/(double)model.n);
	}
	
	/**
	 * Orders nodes on their degree, in ascending order. Order is stored as an array of timestamps: at index i it can be found the time of processing of the corresponding node i of the network 
	 */
	private void computeTimestamps() {
		timestamps = new int[model.n];
		Integer[] mapping = new Integer[model.n];
		for(int i=0; i<model.n; i++) {
			mapping[i] = i;
		}
		Arrays.sort(mapping, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				return model.vertices[a].degree - model.vertices[b].degree;
			}
		});
		for(int i=0; i<model.n; i++) {
			timestamps[mapping[i]] = i; 
		}
	}
	
	/**
	 * Checks if the {@link Vertex} instance have been already included in an explored subnetwork
	 * 
	 * @param xn Array of integers, indicating if the vertex corresponding to the index have been already included in an explored subnetwork
	 * @param v The {@link Vertex} instance to look for
	 * @return true if included, false elsewhere
	 */
	private boolean xn_included(int[] xn, Vertex v) {
		return (v == null) ? false : (xn[v.id] == 1);
	}
	
	/**
	 * Marks the indexes corresponding to the {@link Vertex} v and all its neighbors as included in an explored subnetwork
	 * 
	 * @param xn Array of integers, indicating if the vertex corresponding to an index have been included in an explored subnetwork
	 * @param v The {@link Vertex} instance to mark
	 */
	private void xn_add(int[] xn, Vertex v) {
		xn[v.id]++;
		for(Vertex u : v.neighbors) {
			if(u != null) {
				xn[u.id]++;
			}
		}
	}
	
	/**
	 * Unmarks the indexes corresponding to the {@link Vertex} v and all its neighbors as included in an explored subnetwork
	 * 
	 * @param xn Array of integers, indicating if the vertex corresponding to an index have been included in an explored subnetwork
	 * @param v The {@link Vertex} instance to unmark
	 */
	private void xn_remove(int[] xn, Vertex v) {
		xn[v.id]--;
		for(Vertex u : v.neighbors) {
			if(u != null) {
				xn[u.id]--;
			}
		}
	}	
	
	/**
	 * Node of a tree-like data structure used to order instances od {@link Vertex}
	 * 
	 * @author Federico Altieri
	 * @author Tommy V. Hansen
	 * @author Fabio Vandin
	 *
	 */
	private class Node {
		/**
		 * The {@link Vertex} instance of the node
		 */
		Vertex vertex;
		/**
		 * Reference to the {@link Node} instance parent
		 */
		Node parent;
		/**
		 * {@link ArrayList} of references to the {@link Node} instances of the children of the current tree node
		 */
		ArrayList<Node> children;
		
		/**
		 * Constructor. It builds a Node with an instance of {@link Vertex} and an empty children list
		 * 
		 * @param v The {@link Vertex} instance of the node
		 */
		public Node(Vertex v) {
			vertex = v;
			children = new ArrayList<Node>();
		}
		
		/**
		 * Adds an instance of {@link Node} to the list of children
		 * 
		 * @param child The child instance of {@link Node}
		 */
		public void addChild(Node child) {
			children.add(child);
			child.parent = this;
		}
	}
}
