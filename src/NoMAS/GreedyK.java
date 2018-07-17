package NoMAS;
import java.util.*;

public class GreedyK extends AbstractGreedy {
	
	public GreedyK(Model model , Configuration config) {
		super(model, config);
	}
	
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
	
	public Solution fix(Solution solution, Vertex last_added, int[] dist) {
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