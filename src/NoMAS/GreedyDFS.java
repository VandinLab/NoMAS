package NoMAS;
import java.util.*;

public class GreedyDFS extends AbstractGreedy {
	
	public GreedyDFS(Model model , Configuration config) {
		super(model, config);
	}
	
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
	
	public Solution branch(Vertex v, Solution current, Solution best, int k, int[] dist, boolean[] visited) {
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