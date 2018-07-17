package NoMAS;

public class NoMAS extends AbstractNoMAS {
	public NoMAS(Model model, Configuration config) {
		super(model, config);
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
