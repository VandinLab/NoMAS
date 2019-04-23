package NoMAS;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class ExhaustiveEnumerator implements Algorithm {
	/**
	 * 
	 */
	public Model model;
	/**
	 * 
	 */
	public Configuration config;
	/**
	 * 
	 */
	public Solution[][] solutions;
	/**
	 * 
	 */
	public Progressbar progress;
	/**
	 * 
	 */
	public int done;
	/**
	 * 
	 */
	public int[] timestamps;
	/**
	 * 
	 */
	public double time_elapsed;
	
	/**
	 * @param model
	 * @param config
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
	 * @param size
	 * @param v
	 * @param beta
	 * @param xn
	 * @param timestamp
	 * @return
	 */
	public Node depth(int size, Vertex v, ArrayList<Node> beta, int[] xn, int timestamp) {
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
	 * @param size
	 * @param n
	 * @param v
	 * @param xn
	 * @param timestamp
	 * @return
	 */
	public Node breadth(int size, Node n, Vertex v, int[] xn, int timestamp) {
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
	 * @param root
	 * @param solution
	 * @param lists
	 */
	public void traverse(Node root, Solution solution, Solution[][] lists) {
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
	 * @param lists
	 */
	public synchronized void submitSolutions(Solution[][] lists) {
		solutions[0] = SolutionList.merge(10, Model.MIN_NLR, solutions[0], lists[0]);
		solutions[1] = SolutionList.merge(10, Model.MAX_NLR, solutions[1], lists[1]);
		done++;
		progress.update(done/(double)model.n);
	}
	
	/**
	 * 
	 */
	public void computeTimestamps() {
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
	 * @param xn
	 * @param v
	 * @return
	 */
	public boolean xn_included(int[] xn, Vertex v) {
		return (v == null) ? false : (xn[v.id] == 1);
	}
	
	/**
	 * @param xn
	 * @param v
	 */
	public void xn_add(int[] xn, Vertex v) {
		xn[v.id]++;
		for(Vertex u : v.neighbors) {
			if(u != null) {
				xn[u.id]++;
			}
		}
	}
	
	/**
	 * @param xn
	 * @param v
	 */
	public void xn_remove(int[] xn, Vertex v) {
		xn[v.id]--;
		for(Vertex u : v.neighbors) {
			if(u != null) {
				xn[u.id]--;
			}
		}
	}	
	
	/**
	 * @author Federico Altieri
	 * @author Tommy V. Hansen
	 * @author Fabio Vandin
	 *
	 */
	private class Node {
		/**
		 * 
		 */
		Vertex vertex;
		/**
		 * 
		 */
		Node parent;
		/**
		 * 
		 */
		ArrayList<Node> children;
		
		/**
		 * @param v
		 */
		public Node(Vertex v) {
			vertex = v;
			children = new ArrayList<Node>();
		}
		
		/**
		 * @param child
		 */
		public void addChild(Node child) {
			children.add(child);
			child.parent = this;
		}
	}
}
