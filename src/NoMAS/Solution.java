package NoMAS;
import java.util.*;

public class Solution{
	public static final String HEADER =
	"Mutation count\tlog-rank\tnormalized log-rank\tp-value\tpermutation p-value\tsingle-gene score";
	
	public int[] x;
	public int m1;
	public double lr, nlr, pv, pcv, ppv, score;
	public ArrayList<Vertex> vertices;
	public Solution next;
	
	public Solution() {
		vertices = new ArrayList<Vertex>();
	}
	
	public Solution(Vertex v, Model model) {
		this();
		vertices.add(v);
		x = Arrays.copyOf(v.gene.x, v.gene.x.length);
		computeLogrankStatistic(model);
	}

	public Solution(Vertex v, double score, Model model) {
		this();
		vertices.add(v);
		this.score = score;
		x = Arrays.copyOf(v.gene.x, v.gene.x.length);
	}
	
	public void computeLogrankStatistic(Model model) {
		Bitstring.logrankAndCount(this, model.w);
		nlr = model.normalizeLogrankStatistic(lr, m1);
	}
	
	public String asString(Model model) {
		String s = m1+"\t"+lr+"\t"+nlr+"\t"+pv+"\t"+ppv+"\t"+score+"\t#";
		for(Vertex v : vertices) {
			s += "\t"+model.genes[v.id].symbol;
		}
		return s;
	}

	public void computePopulationVector(Model model) {
		x = Bitstring.getEmpty(model.m);
		for(Vertex v : vertices) {
			x = Bitstring.logicalOR(x, v.gene.x);
		}
		m1 = Bitstring.numberOfSetBits(x);
	}
	
	public void computePopulationVector(int m) {
		x = Bitstring.getEmpty(m);
		for(Vertex v : vertices) {
			x = Bitstring.logicalOR(x, v.gene.x);
		}
		m1 = Bitstring.numberOfSetBits(x);
	}
	
	public static boolean isEqual(Solution a, Solution b) {
		if(a.vertices.size() != b.vertices.size()) {
			return false;
		}
		for(Vertex v : a.vertices) {
			if(!b.vertices.contains(v)) {
				return false;
			}
		}
		return true;
	}
	
	public static int uniqueness(Solution a, Solution b) {
		if(a.vertices.size() != b.vertices.size()) {
			return Math.abs(a.vertices.size() - b.vertices.size());
		}
		int unique = 0;
		for(Vertex v : a.vertices) {
			if(!b.vertices.contains(v)) {
				unique++;
			}
		}
		return unique;
	}
	
	public static Solution merge(Solution a, Solution b, Model model) {
		Solution s = new Solution();
		s.vertices.addAll(a.vertices);
		s.vertices.addAll(b.vertices);
		s.x = Bitstring.logicalOR(a.x, b.x);
		s.computeLogrankStatistic(model);
		return s;
	}

	public static Solution mergeScore(Solution a, Solution b, Model model) {
		Solution s = new Solution();
		s.vertices.addAll(a.vertices);
		s.vertices.addAll(b.vertices);
		s.score = a.score + b.score;
		return s;
	}
	
	public static Solution merge(Solution solution, Vertex v, Model model) {
		return merge(solution, new Solution(v, model), model);
	}

	public static Solution mergeScore(Solution solution, Vertex v, Model model) {
		return merge(solution, new Solution(v, model), model);
	}
	
	public static ArrayList<Vertex> getVertices(Solution... solutions) {
		ArrayList<Vertex> set = new ArrayList<Vertex>();
		for(Solution solution : solutions) {
			for(Vertex v : solution.vertices) {
				if(!set.contains(v)) {
					set.add(v);
				}
			}
		}
		return set;
	}
	
	public static ArrayList<Vertex> getVertices(int limit, Solution... solutions) {
		ArrayList<Vertex> set = new ArrayList<Vertex>();
		for(int i=0; i<limit; i++) {
			Solution solution = solutions[i];
			for(Vertex v : solution.vertices) {
				if(!set.contains(v)) {
					set.add(v);
				}
			}
		}
		return set;
	}
	
	public static double[] contributions(Model model, Solution solution) {
		double[] contributions = new double[solution.vertices.size()];
		for(int i=0; i<solution.vertices.size(); i++) {
			Vertex v = solution.vertices.get(i);
			int[] x = Bitstring.getEmpty(model.m);
			for(Vertex u : solution.vertices) {
				if(u != v) {
					x = Bitstring.logicalOR(x, u.gene.x);
				}
			}
			int m1 = Bitstring.numberOfSetBits(x);
			double lr = Bitstring.dotProductWithArray(x, model.w);
			double nlr = model.normalizeLogrankStatistic(lr, m1);
			contributions[i] = Utils.round(solution.nlr - nlr, 2);
		}
		return contributions;
	}
}
