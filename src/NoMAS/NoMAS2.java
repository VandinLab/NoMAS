package NoMAS;
import java.util.*;

/**
 * Implementation of neighborhood modification of NoMAS Additive (check <a href="https://doi.org/10.3389/fgene.2019.00265">NoMAS: A Computational Approach to Find Mutated Subnetworks Associated With Survival in Genome-Wide Cancer Studies</a> for description)
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class NoMAS2 extends AbstractNoMAS {
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
	public NoMAS2(Model model, Configuration config) {
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
	public Solution computeEntry(Vertex v, int T, int last, int p) {
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
					if(!included[u.id]) {
						included[u.id] = true;
						neighborhood.add(u);
					}
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