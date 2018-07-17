package NoMAS;

public class NoMASAdditive extends AbstractNoMAS {
	public NoMASAdditive(Model model, Configuration config) {
		super(model, config);
	}
    
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
    
    @Override
    public void screenSolutions(Solution[] list) {
		super.screenSolutions(list);
        SolutionList.computeLogrank(model, list);
	}
	
	@Override
	public Solution computeTrivialEntry(Vertex v) {
		return new Solution(v, model.scores[v.id], model);
	}
}