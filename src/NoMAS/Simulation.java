package NoMAS;
import java.util.*;

public class Simulation {
    // This class performs the simulations similar to those found in the thesis
    
	public static void simulate(Model model_ref, Configuration config, int simulations) {
		Random rng = new Random();
		double[] fractions = {0.2, 0.25, 0.33};
		double[] probabilities = {0.5, 0.75, 0.85};
		int[] ms = {model_ref.m, 500, 750, 1000};
		for(double fraction : fractions) {
			for(double prob : probabilities) {
				for(int m : ms) {
					System.err.println("Running with p = "+prob+" and m = "+m+" and fraction = "+fraction);
					simulation(rng, model_ref, config, m, simulations, prob, 0.01, fraction);
				}
			}
		}
	}
	
	public static void simulation(Random rng, Model model_ref, Configuration config, int m, int simulations, double prob, double noise_prob, double fraction) {
		config.progress = false;
		SimulationStats stats = new SimulationStats();
		Progressbar bar = new Progressbar(50);
		for(int s=0; s<simulations; s++) {
			bar.update(s/(double)simulations);
			Model model = Mutations.getSimulatedData(rng, model_ref, m);
			Mutations.removeMutations(model, model_ref.mutation_threshold);
			Graph.reduce(model, model_ref.reduction_conditions);
			
			Solution planted = plantSolution(rng, model, config.k, prob, noise_prob, fraction);
			stats.found = false;
			stats.colorful = false;
			Solution[] solutions = new Solution[config.solutions];
			NoMAS nomas = new NoMAS(model, config);
			nomas.initialize();
			for(int i=0; i<config.iterations; i++) {
				nomas.iterate(rng, solutions);
				if(Graph.isColorful(planted.vertices, config.colors)) {
					stats.colorful = true;
				}
				if(nomas.isFound(planted)) {
					stats.found = true;
				}
			}
			stats.postSimulation(planted, solutions);
		}
		bar.update(1.0);
		bar.finish();
		String prefix = "Simulation/"+model_ref.matrix_file+"/f"+fraction+"_p"+prob+"_m"+m;
		Output overview = new Output(prefix+"_overview.txt", false);
		Output distributions = new Output(prefix+"_distributions.txt", false);
		overview.stream.println("Data = "+model_ref.matrix_file);
		overview.stream.println("Simulations = "+simulations);
		overview.stream.println("Iterations = "+config.iterations);
		overview.stream.println("k = "+config.k);
		overview.stream.println("Probability = "+prob);
		overview.stream.println("Noise probability = "+noise_prob);
		overview.stream.println("Fraction = "+fraction+"\n");
		stats.output(overview, distributions);
		overview.stream.close();
		distributions.stream.close();
	}
	
	public static Solution plantSolution(Random rng, Model model, int k, double prob, double noise_prob, double fraction) {
		ArrayList<Vertex> subnetwork = Graph.randomSubgraph(model, rng, k);
		Solution planted = new Solution();
		for(Vertex v : subnetwork) {
			Bitstring.clear(v.gene.x);
			v.gene.m1 = 0;
			planted.vertices.add(v);
		}
		int split = (int)(fraction * model.m);
        for(int i=0; i<split; i++) {
			if(Statistics.btrial(rng, prob)) {
				int vertex = rng.nextInt(k);
				for(int j=0; j<k; j++) {
					if(j == vertex || Statistics.btrial(rng, noise_prob)) {
						Bitstring.setBit(subnetwork.get(j).gene.x, i);
					}
				}
			}else {
				for(Vertex v : subnetwork) {
					if(Statistics.btrial(rng, noise_prob)) {
						Bitstring.setBit(v.gene.x, i);
					}
				}
			}
		}
		for(int i=split; i<model.m; i++) {
			for(Vertex v : subnetwork) {
				if(Statistics.btrial(rng, noise_prob)) {
					Bitstring.setBit(v.gene.x, i);
				}
			}
		}
		planted.computePopulationVector(model.m);
		planted.computeLogrankStatistic(model);
		return planted;
	}
	
	private static class SimulationStats {
		ArrayList<Integer> best_sizes;
		ArrayList<Integer> overlap_sizes;
		ArrayList<Integer> ranks;
		int c0, c1, c2, c3, c4;
		boolean colorful, found;
		
		public SimulationStats() {
			best_sizes = new ArrayList<Integer>();
			overlap_sizes = new ArrayList<Integer>();
			ranks = new ArrayList<Integer>();
		}
		
		public void postSimulation(Solution planted, Solution[] solutions) {
			if(solutions[0].equals(planted)) {
				c0++;
			}else if(solutions[0].nlr >= planted.nlr) {
				if(found) {
					best_sizes.add(solutions[0].vertices.size());
					overlap_sizes.add(Graph.overlap(planted.vertices, solutions[0].vertices));
					ranks.add(SolutionList.rank(planted, solutions));
					c1++;
				}else if(colorful) {
					c2++;
				}else {
					c3++;
				}
			}else {
				c4++;
			}
		}
		
		public void output(Output overview, Output distributions) {
			overview.stream.println("EQUAL: "+c0);
			overview.stream.println("BETTER GOOD: "+c1);
			overview.stream.println("BETTER BAD COLORFUL: "+c2);
			overview.stream.println("BETTER BAD: "+c3);
			overview.stream.println("WORSE: "+c4);
			overview.stream.println("Overlap cardinalities: "+overlap_sizes.toString()+"\n");
			overview.stream.println("Best subgraph sizes:   "+best_sizes.toString()+"\n");
			overview.stream.println("Ranks: "+ranks.toString()+"\n");
			for(int i=0; i<overlap_sizes.size(); i++) {
				distributions.stream.println(overlap_sizes.get(i)+"\t"+best_sizes.get(i)+"\t"+ranks.get(i));
			}
		}
	}
}