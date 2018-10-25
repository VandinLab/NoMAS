package NoMAS;
import java.util.*;

public class Statistics {
	/**
	 * Obtains experimental permutation p-values of the given 
	 * solutions by repeatedly permuting the data of the model.
	 *
	 * @param list List of solutions
	 * @param algorithm The algorithm to use on the permuted data
	 * @param permuter The data permutation strategy
	 * @param permutations The number of samples for the p-value test
	 * @param objective The objective function to compare the solutions
	 */
	public static void permutationTest(	Solution[] list,
										Algorithm algorithm,
										DataPermuter permuter,
										int permutations,
										Objective objective) {
		// Reset the permutation p-value of the solutions
		for(Solution s : list) {
			s.ppv = 0.0;
		}
		// Repeatedly permute the data and compare solutions
		Progressbar progress = new Progressbar(50);
		for(int p=0; p<permutations; p++) {
			progress.update((double)p/permutations);
			permuter.permute();
			Solution[] perm_list = algorithm.run();
			for(int i=0; i<perm_list.length; i++) {
				if(perm_list[i] == null) {
					System.out.println("permlist["+i+"] = null");
					System.exit(1);
					break;
				}
				if(objective.compare(perm_list[i], list[i]) >= 0) {
					list[i].ppv += 1.0;
				}
			}
		}
		progress.update(1.0);
		progress.finish();
		// Compute permutation p-values
		for(Solution s : list) {
			s.ppv /= permutations;
			if(s.ppv == 0) {
				s.ppv = -1.0;
			}
		}
	}
	
	/**
	 * Entry (i,j) is the number of times that algorithm j found a solution in the permuted data
	 * that was better or equal to the solution found in real data by algorithm i
	 */
	public static double[][][] permutationTest (Model model,
												DataPermuter permuter,
												int permutations,
												Objective objective,
												Output out,
												Algorithm... algorithms) {
		// Construct empty table
		double[][][] table = new double[algorithms.length][algorithms.length][10];
		
		Progressbar progress = new Progressbar(50);
		
		// Identify real data solutions with each algorithm
		System.err.println("Computing real data solutions...");
		Solution[][] real_solutions = new Solution[algorithms.length][10];
		for(int i=0; i<algorithms.length; i++) {
			progress.update((double)i/algorithms.length);
			real_solutions[i] = algorithms[i].run();
			Output.solutions(out.stream, model, real_solutions[i]);
			out.stream.println("Algorithm: "+algorithms[i].getClass().getName()+"\n");
			out.stream.flush();
		}
		progress.update(1.0);
		progress.finish();
		
		System.err.println("Generating permutations...");
		for(int p=0; p<permutations; p++) {
			progress.update((double)p/permutations);
			
			// Permute data
			permuter.permute();
			
			// Identify permuted data solution with each algorithm
			for(int j=0; j<algorithms.length; j++) {
				Solution[] perm_solutions = algorithms[j].run();
				
				// compare permuted solution to real solution of each algorithm
				for(int i=0; i<algorithms.length; i++) {
					for(int s=0; s<10; s++) {
						if(objective.compare(perm_solutions[s], real_solutions[i][s]) >= 0) {
							table[i][j][s] += 1.0;
						}
					}
				}
			}
		}
		progress.update(1.0);
		progress.finish();
		
		for(int i=0; i<algorithms.length; i++) {
			for(int j=0; j<algorithms.length; j++) {
				for(int s=0; s<10; s++) {
					table[i][j][s] /= permutations;
					if(table[i][j][s] == 0) {
						table[i][j][s] = -1.0;
					}
				}
			}
		}
		
		return table;
	}
	
    /**
	 * For different i and j, finds the number of overlapping vertices between the highest scoring subnetwork of size i
     * and the top-"limit" highest scornig subnetworks of size j
	 */
	public static int[][] overlapMatrix(int limit, Solution[][] lists_row, Solution[][] lists_col) {
		int[][] overlap_matrix = new int[lists_row.length][lists_col.length];
		for(int i=0; i<lists_row.length; i++) {
			ArrayList<Vertex> A = Solution.getVertices(limit, lists_row[i]);
			for(int j=i; j<lists_col.length; j++) {
				ArrayList<Vertex> B = lists_col[j][0].vertices;
				overlap_matrix[i][j] = Graph.overlap(A, B);
			}
		}
		return overlap_matrix;
	}
	
	/**
	 * Estimates the p-value of the given log-rank statistic 
	 * under the permutational distribution using permutation sampling.
	 *
	 * @param logrank The log-rank statistic
	 * @param m1 The size of population 1
	 * @param model The model under which the log-rank statistic was obtained
	 * @param samples The number of permtuation samples
	 * @param N The number of threads to use
	 *
	 * @return The estimated p-value
	 */
	public static double pvalue(	final double logrank,
									final int m1,
									final Model model,
									final int samples,
									final int N) {
		if (model.m == 0) {
			return 1;
		}
		final int[] counts = new int[N];
		Thread[] threads = new Thread[N];
		for(int i=0; i<N; i++) {
			final int index = i;
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rng = new Random();
					int local_samples = Utils.getJobCount(N, index, samples);
					int m = model.m;
					double[] w = Arrays.copyOf(model.w, m);
					int count = 0;
					for(int t=0; t<local_samples; t++) {
						double sample_logrank = 0.0;
						for(int j=0; j<m1; j++) {
							int ri = rng.nextInt(m-j);
							sample_logrank += w[ri];
							double tmp = w[m-j-1];
							w[m-j-1] = w[ri];
							w[ri] = tmp;
						}
						if(logrank > 0) {
							if(sample_logrank >= logrank) {
								count++;
							}
						}else if(sample_logrank <= logrank) {
							count++;
						}
					}
					counts[index] = count;
				}
			});
			threads[i].start();
		}
		Utils.join(threads);
		int total_count = 0;
		for(int count : counts) {
			total_count += count;
		}
		return Math.max((double)total_count/samples, 1.0/samples);
	}
	
	/**
	 * Estimates the p-value of the log-rank statistic for the given
	 * list of solutions.
	 *
	 * @param model The model under which the solutions was obtained
	 * @param samples The number of samples for the p-value estimate
	 * @param solutions The list of solutions
	 * @param N The number of threads to use
	 * @param crossval Flag to determine if in crossval mode
	 */
	public static void pvalue(Model model, int samples, int N, boolean crossval, Solution... solutions) {
		if (crossval) {
			for(Solution solution : solutions) {
				solution.pv = pvalue(solution.lr, solution.m1, model, samples, N);
			}
		} else {
			for(Solution solution : solutions) {
				solution.pcv = pvalue(solution.lr, solution.m1, model, samples, N);
			}
		}
		
	}
	
	/**
	 * Derives the upperbound on the number of color-coding iterations required
	 * to achieve the optimal solution with given error probability.
	 *
	 * @param error An error probability (between 0 and 1)
	 * @param k The size of the optimal subnetwork
	 *
	 * @return The number of required iterations
	 */
	public static int iterationsUpperBound(double error, int k) {
		return (int)(Math.log(1.0/error)*Math.pow(Math.E, k)+0.5);
	}
	
	/**
	 * Derives the exact number of color-coding iterations required
	 * to achieve the optimal solution with given error probability.
	 *
	 * @param error An error probability (between 0 and 1)
	 * @param k The size of the optimal subnetwork
	 * @param d The number of colors
	 *
	 * @return The number of required iterations
	 */
	public static int iterations(double error, int k, int d) {
		double pc = 1.0;
		for(int i=1; i<=k; i++) {
			pc *= ((double)(i+(d-k))/(double)(d));
		}
		return (int)((1.0/pc)*Math.log(1.0/error));
	}
	
	
	/**
	 * Performs a binomial trial with the given success probability.
	 *
	 * @param rng The random number generator to use for the trial
	 * @param prob_success The probability of success (between 1 and 0)
	 *
	 * @return True if the trial succeeds and False otherwise
	 */
	public static boolean btrial(Random rng, double prob_success) {
		return (rng.nextDouble() <= prob_success);
	}
}
