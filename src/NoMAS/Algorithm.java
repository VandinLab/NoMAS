package NoMAS;


/**
 * Interface that defines the simple concept of algorithm. Parameters are intended to be provided when an instance of the implementation is created (i.e. passed with the constructor invocation and stored as fields).
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public interface Algorithm  {
	
	/**
	 * Executes the algorithm. No parameter is passed here.
	 * 
	 * @return an array of Solutions computed by the algorithm.
	 */
	public Solution[] run();
	
	/**
	 * @return time spent in algorithm execution
	 */
	public double timeElapsed();
}