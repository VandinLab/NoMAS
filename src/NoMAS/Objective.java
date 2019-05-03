package NoMAS;

/**
 * Interface that represent the {@link Solution} score ranking policy, from best to worst.
 *  
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public interface Objective {
	/**
	 * Returns a value that indicates how the two given {@link Solution} instances are ranked. The implementation should reflect the chosen ranking policy and this method should return 1 if {@link Solution} a is better than {@link Solution} b, 0 if {@link Solution} a is equal to {@link Solution} b, -1 if a {@link Solution} is worse than {@link Solution} b.
	 *
	 * @param a The first {@link Solution} instance to compare
	 * @param b The Second {@link Solution} instance to compare
	 * @return 1 if {@link Solution} a is better than {@link Solution} b; 0 if {@link Solution} a is equal to {@link Solution} b; -1 if {@link Solution} a is worse than {@link Solution} b
	 */
	public int compare(Solution a, Solution b);

	/**
	 * Returns the name of the policy
	 * 
	 * @return the name of the policy
	 */
	public String getName();
}