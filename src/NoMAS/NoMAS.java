package NoMAS;

/**
 * Standard implementation of NoMas Algorithm (check {@link https://doi.org/10.3389/fgene.2019.00265} for description)
 * 
 * @author Federico  Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class NoMAS extends AbstractNoMAS {
	
	/**
	 * Constructor that receives input data ({@link Model} instance and configuration parameters ({@link Configuration} instance)
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @param config Instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public NoMAS(Model model, Configuration config) {
		super(model, config);
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
			
			if(R == 0 || W[rowQ][v.id] == null) {
				continue;
			}
		
			for(Vertex u : v.neighbors) {
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
