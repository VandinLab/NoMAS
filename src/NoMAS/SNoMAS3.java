package NoMAS;
import java.util.*;

public class SNoMAS3 extends SNoMAS2 {
	public boolean[][] included_arrays;
	
	public SNoMAS3(Model model, Configuration config) {
		super(model, config);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		included_arrays = new boolean[config.N][model.n];
	}
	
	@Override
	public Solution computeEntry(Vertex v, int T, int last, int p) {
		// Do simple computation if v is not a seed vertex
		if(vertex_cost[v.id] <= config.kprime) {
			return super.computeEntry(v, T, last, p);
		}
		
		Solution best = null;
		for(int rowQ=last; rowQ>=0; rowQ--) {
			int Q = row_to_colorset[rowQ];
			int R = Bitstring.setDifference(T, Q);
			int rowR = colorset_to_row[R];

			// If Q is not a subset of T or the entry for v is null
			if(R == 0 || W[rowQ][v.id] == null) {
				continue;
			}

			// Build neighborhood of subnetwork W(Q,v)
			ArrayList<Vertex> neighborhood = new ArrayList<Vertex>();
			boolean[] included = included_arrays[p];
			for(Vertex w : W[rowQ][v.id].vertices) {
				for(Vertex u : w.neighbors) {
					if(included[u.id] || vertex_cost[u.id] <= 0) {
						continue;
					}
					included[u.id] = true;
					neighborhood.add(u);
				}
			}

			// Attempt to combine with W(R,u) for each neighbor u
			for(Vertex u : neighborhood) {
				included[u.id] = false;	
				if(W[rowR][u.id] != null) {
					Solution candidate = Solution.merge(W[rowQ][v.id], W[rowR][u.id], model);
					if(best == null || config.objective.compare(candidate, best) >= 0) {
						best = candidate;
					}
				}
			}
		}
		return best;
	}
}
