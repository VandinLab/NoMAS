package NoMAS;

import java.util.*;

public class SingleGeneTest {
	public static void run(String data, int samples, int N) {
		String mutationFile = "datasets/"+data+".txt";
		String graphFile = "networks/"+data+".txt";
		
		Model model = new Model();
		Graph.loadGraph(graphFile, model);
		Mutations.loadMutationMatrix(mutationFile, model);
		
		ArrayList<Solution> genes = new ArrayList<Solution>();
		for(Vertex v : model.vertices) {
			if(v.gene.m1 > 0) {
				Solution solution = new Solution(v, model);
				genes.add(solution);
			}
		}
		Output out = new Output("pvalue_"+data+".txt", false);
		out.stream.println("module\tpvalue\tnorm.logrank\tm1");
		for(Solution solution : genes) {
			Statistics.pvalue(model, samples, N, false, solution);
			out.stream.println(solution.vertices.get(0).gene.symbol+"\t"+solution.pv+"\t"+solution.nlr+"\t"+solution.vertices.get(0).gene.m1);
			out.stream.flush();
		}
		out.stream.close();
	}
	
	public static void runAll(String data, int samples, int N) {
		String mutationFile = "datasets/"+data+".txt";
		String graphFile = "networks/"+data+".txt";
		
		Model model = new Model();
		Graph.loadGraph(graphFile, model);
		Mutations.loadMutationMatrix(mutationFile, model);
		
		ArrayList<Integer> bitsets = new ArrayList<Integer>();
		for(int i=1; i<=10; i++) {
			Bitstring.enumerate(bitsets, 0, 10, i, 0);
		}

		Output out = new Output("pvalue_sets_"+data+".txt", false);
		out.stream.println("modules\tpvalue\tnorm.logrank\tm1\tm1/m");
		
		ArrayList<Result> results = new ArrayList<Result>();
		int count = 0;
		for(int bitset : bitsets) {
			Solution solution = new Solution();
			for(int i=0; i<10; i++) {
				if(Bitstring.getBit(bitset, i) == 1) {
					solution.vertices.add(model.vertices[i]);
				}
			}
			solution.computePopulationVector(model);
			solution.computeLogrankStatistic(model);
			
			Statistics.pvalue(model, samples, N, false, solution);
			
			double ratio = Utils.round(((double)solution.m1/model.m), 2);
			out.stream.println(Bitstring.asString(bitset, 10)+"\t"+solution.pv+"\t"+solution.nlr+"\t"+solution.m1+"\t"+ratio);
			out.stream.flush();
			//results.add(new Result(bitset, solution));
			//System.err.println("Counter = "+count++);
		}
		out.stream.close();
	}
	
	private static class Result implements Comparable<Result> {
		int bitset;
		Solution solution;
		
		public Result(int i, Solution s) {
			bitset = i;
			solution = s;
		}
		
		public int compareTo(Result other) {
			if(this.solution.pv < other.solution.pv) return -1;
			if(this.solution.pv > other.solution.pv) return 1;
			return 0;
		}
	}
}
