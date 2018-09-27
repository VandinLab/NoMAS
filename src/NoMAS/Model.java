package NoMAS;
import java.io.*;

public class Model {
	public static final int HASMUTATIONS = 1;
	public static final int INTERNAL = 2;
	public int n, m, reduction_conditions;
	public double norm_coef, mutation_threshold;
	public int[] c;
	public double[] w, scores, times;
	public String[] patient_ids;
	public Gene[] genes;
	public Vertex[] vertices;
	public String graph_file, matrix_file;
	public Output log;
	
	public Model() {
		log = new Output("Log.txt", false);
	}
	
	public Model(String logname) {
		log = new Output(logname+"Log.txt", false);
	}
	
	public double normalizeLogrankStatistic(double lr, int m1) {
		int m2 = m-m1;
		double variance = norm_coef*((double)(m1*m2)/(double)(m*(m-1)));
		return (variance == 0.0) ? Double.MIN_VALUE : lr/Math.sqrt(variance);
	}

	public static void computeSingleGeneScores(Model model, int samples, int N, boolean positive) {
		model.scores = new double[model.n];
		for(int i=0; i<model.n; i++) {
			double lr = Bitstring.dotProductWithArray(model.genes[i].x, model.w);
			if((positive && lr > 0.0) || (!positive && lr < 0.0)) {
				double pv = Statistics.pvalue(lr, model.genes[i].m1, model, samples, N);
				model.scores[i] = -Math.log10(pv);
			}else {
				model.scores[i] = 0.0;
			}
		}
	}
	
    /**
	 * Decides whether to retain the given vertex in the network
	 */
	public static boolean includeVertex(Vertex v, Gene g, int flags) {
		if((flags & HASMUTATIONS) > 0 && g.m1 > 0) {
			return true;
		}
		if((flags & INTERNAL) > 0 && v.degree > 1) {
			return true;
		}
		return false;
	}
	
	public static Model fromSolutionsFile(String filename) {
		Model model = new Model();
		BufferedReader file = Utils.bufferedReader(filename);
		String line = Utils.readLine(file);
		while(!line.startsWith("Graph file")) {
			line = Utils.readLine(file);
		}
		Graph.loadGraph(line.split("\t")[1], model);
		Mutations.loadMutationMatrix(Utils.readLine(file).split("\t")[1], model);
		Mutations.removeMutations(model, Double.parseDouble(Utils.readLine(file).split("\t")[1]));
		Graph.reduce(model, Integer.parseInt(Utils.readLine(file).split("\t")[1]));
		return model;
	}

	public static final Objective MAX_NLR = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.nlr > b.nlr) return 1;
			if(a.nlr < b.nlr) return -1;
			return 0;
		}
		public String getName() {
			return "MAX_NLR";
		}
	};

	public static final Objective MIN_NLR = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.nlr < b.nlr) return 1;
			if(a.nlr > b.nlr) return -1;
			return 0;
		}
		public String getName() {
			return "MIN_NLR";
		}
	};

	public static final Objective SCORE_RED = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.score > b.score) return 1;
			if(a.score < b.score) return -1;
			return 0;
		}
		public String getName() {
			return "SCORE_RED";
		}
	};
    
    public static final Objective SCORE_INC = new Objective() {
		public int compare(Solution a, Solution b) {
			if(a.score > b.score) return 1;
			if(a.score < b.score) return -1;
			return 0;
		}
		public String getName() {
			return "SCORE_INC";
		}
	};
	
	public static Objective objectiveFromName(String name) {
		if(name.equals("MAX_NLR")) {
			return MAX_NLR;
		}else if(name.equals("MIN_NLR")) {
			return MIN_NLR;
		}else if(name.equals("SCORE_RED")) {
			return SCORE_RED;
		}else if(name.equals("SCORE_INC")) {
			return SCORE_INC;
		}
		System.err.println("No such scoring function: "+name);
		System.exit(1);
		return null;
	}
}