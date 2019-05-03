package NoMAS;
import java.util.*;

/**
 * Implementation of SNoMas, heuristic 0. It can enumerate the same number of solutions (containing at least one seed vertex) as {@link NoMAS}.
 *  Is faster than {@link NoMAS}.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class SNoMAS1 extends AbstractSNoMAS {
	/**
	 * Constructor that receives input data ({@link Model} instance and configuration parameters ({@link Configuration} instance)
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @param config Instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public SNoMAS1(Model model, Configuration config) {
		super(model, config);
	}

	/**
	 *{@inheritDoc}
	 */
	public int[] getVertexCost(int[] dist) {
		int[] cost = new int[model.n];
		for(int i=0; i<model.n; i++) {
			if(dist[i] > config.kprime) {
				cost[i] = 0;
			}else {
				cost[i] = config.k - dist[i];
			}
		}
		return cost;
	}
}