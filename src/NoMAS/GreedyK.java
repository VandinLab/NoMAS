package NoMAS;

/**
 * Implementation of a greedy strategy to solve the problem based on nodes
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class GreedyK extends AbstractGreedy {
	
	/**
	 * Constructor with model and configuration.
	 *  
	 * @param model {@link Model} with input data.
	 * @param config {@link Configuration} with algorithm configuration.
	 */
	public GreedyK(Model model , Configuration config) {
		super(model, config);
	}
	
	/**
	 *{@inheritDoc}
	 */
	public Solution expand(Solution solution, int k) {
		if(solution.vertices.size() == k) {
			return solution;
		}
		int max_dist = k - solution.vertices.size();
		int[] dist = Graph.shortestPaths(model, solution.vertices);
		Solution best = solution;
		Vertex last_added = null;
		for(Vertex v : model.vertices) {
			if(dist[v.id] > 0 && dist[v.id] <= max_dist) {
				Solution candidate = Solution.merge(solution, v, model);
				if(config.objective.compare(candidate, best) > 0) {
					best = candidate;
					last_added = v;
				}
			}
		}
		if(last_added == null) {
			return solution;
		}
		Solution fixed_solution = fix(best, last_added, dist);
		return expand(fixed_solution, k);	
	}
	
	/**
	 * Fixes the current solution exploring the neighbors of last added vertex.
	 * 
	 * @param solution The current {@link Solution}instance to process.
	 * @param last_added {@link Vertex} instance of last added node.
	 * @param dist the distances array.
	 * @return the fixed solution
	 */
	private Solution fix(Solution solution, Vertex last_added, int[] dist) {
		if(dist[last_added.id] == 1) {
			return solution;
		}
		Solution best = null;
		Vertex best_added = null;
		for(Vertex u : last_added.neighbors) {
			if(dist[u.id] == dist[last_added.id] - 1) {
				Solution candidate = Solution.merge(solution, u, model);
				if(best == null || config.objective.compare(candidate, best) > 0) {
					best = candidate;
					best_added = u;
				}
			}
		}
		return fix(best, best_added, dist);
	}
}