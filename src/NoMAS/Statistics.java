package NoMAS;
import java.util.*;

/**
 * Containers of static methods that perform the computations of various statistics.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Statistics {
	/**
	 * Obtains experimental permutation p-values of the given solutions by repeatedly permuting the data of the model.
	 *
	 * @param list List of {@link Solution} instances.
	 * @param algorithm The {@link Algorithm} to use on the permuted data.
	 * @param permuter {@link DataPermuter} instance that implements permutations strategy.
	 * @param permutations The number of samples for the p-value test.
	 * @param objective The objective function to compare the solutions.
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
	 * Estimates the p-value of the given log-rank statistic under the permutational distribution using permutation sampling.
	 *
	 * @param logrank The log-rank statistic.
	 * @param m1 The size of population that determined the logrank.
	 * @param model The {@link Model} instance containing data under which the log-rank statistic was obtained.
	 * @param samples The number of permtuation samples.
	 * @param N The number of threads to use.
	 *
	 * @return The estimated p-value.
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
	 * Estimates the p-value of the log-rank statistic for the given list of {@link Solution} instances.
	 *
	 * @param model The {@link Model} instance containing data under which the solutions was obtained.
	 * @param samples The number of samples for the p-value estimate.
	 * @param N The number of threads to use.
	 * @param crossval If true, the algorithm uses a holdout approach for statistical validation and the {@link Model} instance contains data about patients in the validation set. 
	 * @param solutions The list of {@link Solution} instances.
	 * 
	 */
	public static void pvalue(Model model, int samples, int N, boolean crossval, Solution... solutions) {
		if (crossval) {
			for(Solution solution : solutions) {
				solution.pcv = pvalue(solution.lrcv, solution.m1cv, model, samples, N);
			}
		} else {
			for(Solution solution : solutions) {
				solution.pv = pvalue(solution.lr, solution.m1, model, samples, N);
			}
		}
		
	}
	
	/**
	 * Derives an upper bound on the number of color-coding iterations required to achieve the optimal solution with given error probability.
	 *
	 * @param error An error probability (between 0 and 1).
	 * @param k The size of the optimal subnetwork.
	 *
	 * @return The number of required iterations.
	 */
	public static int iterationsUpperBound(double error, int k) {
		return (int)(Math.log(1.0/error)*Math.pow(Math.E, k)+0.5);
	}
	
	/**
	 * Derives the exact number of color-coding iterations required to achieve the optimal solution with given error probability and colorset size.
	 *
	 * @param error An error probability (between 0 and 1).
	 * @param k The size of the optimal subnetwork.
	 * @param d The number of colors in the colorset.
	 *
	 * @return The number of required iterations.
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
	 * @param rng {@link Random} instance that performs random number generation for the trial.
	 * @param prob_success The probability of success (between 1 and 0).
	 *
	 * @return True if the trial succeeds and False otherwise.
	 */
	public static boolean btrial(Random rng, double prob_success) {
		return (rng.nextDouble() <= prob_success);
	}
}
