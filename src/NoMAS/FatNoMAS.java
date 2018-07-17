package NoMAS;

public class FatNoMAS extends AbstractNoMAS {
	public FatNoMAS(Model model, Configuration config) {
		super(model, config);
	}

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