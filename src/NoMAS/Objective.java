package NoMAS;

public interface Objective {
	/**
	 * Returns 1 if a is better than b
	 * Returns 0 if a is equal to b
	 * Returns -1 if a is worse than b
	 */
	public int compare(Solution a, Solution b);

	public String getName();
}