package NoMAS;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public abstract class AbstractSNoMAS extends AbstractNoMAS {
	public int[] vertex_cost;
	public ArrayList<ArrayList<Vertex>> job_queue;
	
	public AbstractSNoMAS(Model model, Configuration config) {
		super(model, config);
	}
	
	public void initialize() {
		ArrayList<Vertex> seeds = null;
		if(config.seeds == null) {
			seeds = generateSeeds();
		}else {
			seeds = config.seeds;
		}
		super.initialize();
		int[] dist = Graph.shortestPaths(model, seeds);
		vertex_cost = getVertexCost(dist);
		computeWorkloads();
	}
	
	public void computeWorkloads() {
		job_queue = new ArrayList<ArrayList<Vertex>>();
		int[] last_assigned = new int[config.k];
		for(int i=0; i<config.N; i++) {
			job_queue.add(new ArrayList<Vertex>());
		}
		for(int i=0; i<model.n; i++) {
			int cost = vertex_cost[i];
			if(cost > 0) {
				int processor = last_assigned[cost-1]; // zero-indexed
				job_queue.get(processor).add(model.vertices[i]);
				last_assigned[cost-1] = (processor+1)%config.N;
			}
		}
	}
	
	public static ArrayList<Vertex> loadSeedsFromFile(Model model, String filename) {
		ArrayList<Vertex> seeds = new ArrayList<Vertex>();
		BufferedReader file = Utils.bufferedReader(filename);
		String line = null;
		while((line = Utils.readLine(file)) != null) {
			line = line.split("\t")[0];
			Vertex v = Graph.getVertexBySymbol(model, line);
			if(v != null) {
				seeds.add(v);
			}
		}
		if(seeds.size() == 0) {
			return null;
		}
		//DEBUGGING
		System.err.println("Imported seed vertices from file");
		for(Vertex v : seeds) {
			System.err.print(v.gene.symbol+", ");
		}
		System.err.print("\n");
		return seeds;
	}
	
	public ArrayList<Vertex> generateSeeds() {
        System.err.println("Generating seed vertices...");
		Configuration config2 = new Configuration();
		config2.k = (config.k+1)/2 + 1;
		config2.colors = config2.k+1;
		config2.progress = false;
		config2.objective = config.objective;
		config2.uniqueness = true;
		config2.N = config.N;
		config2.iterations = Statistics.iterations(config.seed_error, config2.k, config2.colors);
		NoMAS nomas = new NoMAS(model, config2);
		Solution[] seed_solutions = nomas.run();
        System.err.println("Done.");
		return Solution.getVertices(seed_solutions);
	}
	
	/**
	 * Fills out the entire table using dynamic programming.
	 */
	@Override
	public void fillTable() {
		final CyclicBarrier barrier = new CyclicBarrier(config.N);
		final int n = model.n;

		Thread[] threads = new Thread[config.N];
		for(int i=0; i<config.N; i++) {
			final int index = i;
			threads[i] = new Thread(new Runnable() {
				public void run() {
					ArrayList<Vertex> jobs = job_queue.get(index);				
					for(Vertex v : jobs) {
						if(vertex_cost[v.id] >= 1) {
							W[v.color][v.id] = computeTrivialEntry(v);
						}
					}				
					Utils.synchronize(barrier);				
					for(int group=2; group<=config.k; group++) {
						int first = colorset_groups[group-1];
						int end = colorset_groups[group];
						for(Vertex v : jobs) {
							if(vertex_cost[v.id] >= group) {
								if(v.degree > 0) {
									for(int r=first; r<end; r++) {
										W[r][v.id] = computeEntry(v, row_to_colorset[r], first-1, index);
									}
								}
							}
						}
						Utils.synchronize(barrier);
					}
				}
			});
			threads[i].start();
		}
		Utils.join(threads);
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
				if(vertex_cost[u.id] <= 0) {
					continue;
				}
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

	public abstract int[] getVertexCost(int[] dist);
}
