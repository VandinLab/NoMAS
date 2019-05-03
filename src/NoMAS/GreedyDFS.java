package NoMAS;

/**
 * Implementation of a greedy strategy to solve the problem. This greedy strategy has an approach based on paths rather than vertexes.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class GreedyDFS extends AbstractGreedy {
	
	/**
	 * Constructor with model and configuration.
	 *  
	 * @param model {@link Model} with input data. 
	 * @param config {@link Configuration} with algorithm configuration.
	 */
	public GreedyDFS(Model model , Configuration config) {
		super(model, config);
	}
	
	/**
	 *{@inheritDoc}
	 */
	public Solution expand(Solution solution, int k) {
		if(solution.vertices.size() == k) {
			return solution;
		}
		int previous_size = solution.vertices.size();
		int[] dist = Graph.shortestPaths(model, solution.vertices);
		boolean[] visited = new boolean[model.n];
		Solution best = solution;
		for(Vertex v : solution.vertices) {
			best = branch(v, solution, best, k, dist, visited);
		}
		if(best.vertices.size() == previous_size) {
			return best;
		}
		return expand(best, k);
	}
	
	/**
	 * Explores if a node improves the current or the best solution found up to the moment and calls a recursion over its neighbors.
	 * 
	 * @param v The node to investigate.
	 * @param current The current {@link Solution} to explore.
	 * @param best The actual highest scoring solution found.
	 * @param k The size of the subnetwork to find
	 * @param dist array with paths to other nodes 
	 * @param visited array of boolean that tells if the nodes have already been explored or not
	 * @return the best solution among the explored
	 */
	private Solution branch(Vertex v, Solution current, Solution best, int k, int[] dist, boolean[] visited) {
		visited[v.id] = true;
		if(v.degree == 0 || current.vertices.size() == k) {
			return best;
		}
		for(Vertex u : v.neighbors) {
			if(!visited[u.id] && dist[u.id] == dist[v.id]+1) {
				Solution candidate = Solution.merge(current, u, model);
				if(config.objective.compare(candidate, best) > 0) {
					best = candidate;
				}
				best = branch(u, candidate, best, k, dist, visited);
			}
		}
		return best;
	}
}