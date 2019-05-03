package NoMAS;
import java.util.*;

/**
 * Implementation of a greedy strategy to solve the problem based on nodes
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Greedy1 extends AbstractGreedy {
	
	/**
	 * Constructor with model and configuration.
	 *  
	 * @param model {@link Model} with input data.
	 * @param config {@link Configuration} with algorithm configuration.
	 */
	public Greedy1(Model model , Configuration config) {
		super(model, config);
	}
	
	/**
	 *{@inheritDoc}
	 */
	public Solution expand(Solution solution, int k) {
		boolean[] in_solution = new boolean[model.n];
		boolean[] visited = new boolean[model.n];
		for(Vertex v : solution.vertices) {
			in_solution[v.id] = true;
		}
		while(solution.vertices.size() < k) {
			Solution best = solution;
			Vertex last_added = null;
			Arrays.fill(visited, false);
			for(Vertex v : solution.vertices) {
				for(Vertex u : v.neighbors) {
					if(!in_solution[u.id] && !visited[u.id]) {
						visited[u.id] = true;
						Solution candidate = Solution.merge(solution, u, model);
						if(config.objective.compare(candidate, best) > 0) {
							best = candidate;
							last_added = u;
						}
					}
				}
			}
			if(last_added == null) {
				return solution;
			}
			in_solution[last_added.id] = true;
			solution = best;
		}
		return solution;
	}
}