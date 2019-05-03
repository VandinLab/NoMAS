package NoMAS;
import java.util.*;

/**
 * Implementation of SNoMas, heuristic 2. Same as heuristic 1, implemented in {@link SNoMAS2}, but can also combine with vertices not neighboring a seed vertex. 
 * A bit slower than {@link SNoMAS2} but faster than {@link SNoMAS1}. Can enumerate more solutions than {@link SNoMAS2}.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class SNoMAS3 extends SNoMAS2 {
	/**
	 * array of booleans for neighbors tracking
	 */
	private boolean[][] included_arrays;
	
	/**
	 * Constructor that receives input data ({@link Model} instance and configuration parameters ({@link Configuration} instance)
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @param config Instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public SNoMAS3(Model model, Configuration config) {
		super(model, config);
	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void initialize() {
		super.initialize();
		included_arrays = new boolean[config.N][model.n];
	}
	
	/**
	 *{@inheritDoc}
	 */
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
