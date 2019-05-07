package NoMAS;

/**
 * NoMAS algorithm that implement the "Fat Table" variant. Further details in the supplementary material of <a href="https://doi.org/10.3389/fgene.2019.00265">NoMAS: A Computational Approach to Find Mutated Subnetworks Associated With Survival in Genome-Wide Cancer Studies</a>.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class FatNoMAS extends AbstractNoMAS {
	/**
	 * @param model {@link Model} containing input data.
	 * @param config {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public FatNoMAS(Model model, Configuration config) {
		super(model, config);
	}

	/**
	 *{@inheritDoc}
	 */
	public Solution computeEntry(Vertex v, int T, int last, int p) {
		Solution[] best = new Solution[config.L];
		for(int rowQ=last; rowQ>=0; rowQ--) {
			int Q = row_to_colorset[rowQ];
			int R = Bitstring.setDifference(T, Q);
			int rowR = colorset_to_row[R];	
			if(R == 0 || W[rowQ][v.id] == null) {
				continue;
			}
			for(Vertex u : v.neighbors) {
				if(W[rowR][u.id] != null) {
					crossProduct(W[rowQ][v.id], W[rowR][u.id], best);
				}
			}
		}
		return SolutionList.toLinkedList(best);
	}
	
	/**
	 * Computes an array containing the best scoring subnetworks obtainable by all possible merging of the given subnetworks. All merged subnetworks are explored with an exhaustive approach.
	 * 
	 * @param a instance of {@link Solution} representing the first subnetwork to merge
	 * @param b instance of {@link Solution} representing the second subnetwork to merge
	 * @param list array of {@link Solution} instances of best scoring merged subnetworks
	 */
	public final void crossProduct(Solution a, Solution b, Solution[] list) {
		while(a != null) {
			Solution inner = b;
			while(inner != null) {
				Solution candidate = Solution.merge(a, inner, model);
				SolutionList.insert(list, candidate, config.objective);
				inner = inner.next;
			}
			a = a.next;
		}
	}
}