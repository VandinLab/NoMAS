package NoMAS;

public abstract class AbstractGreedy implements Algorithm {
	public Model model;
	public Configuration config;
	public double time_elapsed;
	
	public AbstractGreedy(Model model, Configuration config) {
		this.model = model;
		this.config = config;
	}
	
	public double timeElapsed() {
		return time_elapsed;
	}
	
	public Solution[] run() {
		long start_time = Utils.getTime();
		Thread[] threads = new Thread[config.N];
		final Solution[][] solutions = new Solution[config.N][10];
		for(int i=0; i<config.N; i++) {
			final int index = i;
			threads[i] = new Thread(new Runnable() {
				public void run() {
					int base = Utils.getBase(config.N, index, model.n);
					int limit = base + Utils.getJobCount(config.N, index, model.n);
					for(int j=base; j<limit; j++) {
						Solution solution = expand(model.vertices[j], config.k);
						SolutionList.insert(solutions[index], solution, config.objective);
					}	
				}
			});
			threads[i].start();
		}
		Utils.join(threads);
		time_elapsed = Utils.timeElapsed(start_time);
		return SolutionList.merge(10, config.objective, solutions);
	}
	
	public Solution expand(Vertex v, int k) {
		Solution solution = new Solution(v, model);
		return expand(solution, k);
	}
	
	public abstract Solution expand(Solution s, int k);
}
