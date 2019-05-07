package NoMAS;

/**
 * NoMas implementation with the additive score variant. The score of a subnetwork is considered as the sum of the scores of itsnodes (check <a href="https://doi.org/10.3389/fgene.2019.00265">NoMAS: A Computational Approach to Find Mutated Subnetworks Associated With Survival in Genome-Wide Cancer Studies</a> for further details)
 * 
 * @author Federico  Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class NoMASAdditive extends AbstractNoMAS {
	
	/**
	 * Constructor that receives input data ({@link Model} instance and configuration parameters ({@link Configuration} instance)
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @param config Instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public NoMASAdditive(Model model, Configuration config) {
		super(model, config);
	}
    
    /**
     *{@inheritDoc}
     */
    @Override
    public void initialize() {
           super.initialize();
           if(model.scores == null) {
               System.err.println("Computing single gene scores...");
               boolean red = (config.objective.getName().equals("SCORE_RED")) ? true : false;
               Model.computeSingleGeneScores(model, (int)Math.pow(10, 5), config.N, red);
               System.err.println("Done.");
           }
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
					Solution candidate = Solution.mergeScore(W[rowQ][v.id], W[rowR][u.id], model);
					if(best == null || config.objective.compare(candidate, best) >= 0) {
						best = candidate;
					}
				}
			}
		}
		return best;
	}
    
    /**
     *{@inheritDoc}
     */
    @Override
    public void screenSolutions(Solution[] list) {
		super.screenSolutions(list);
        SolutionList.computeLogrank(model, list);
	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public Solution computeTrivialEntry(Vertex v) {
		return new Solution(v, model.scores[v.id], model);
	}
}