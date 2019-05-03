package NoMAS;
import java.util.*;

/**
 * Implementation of SNoMas, heuristic 1. Constructs solutions by combination of smaller solutions of size at most neighboring a seed vertex. 
 * Is faster than {@link SNoMAS1}.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class SNoMAS2 extends AbstractSNoMAS {
	/**
	 * Constructor that receives input data ({@link Model} instance and configuration parameters ({@link Configuration} instance)
	 * 
	 * @param model Instance of {@link Model} containing input data.
	 * @param config Instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public SNoMAS2(Model model, Configuration config) {
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
			}else if(dist[i] == 0) {
				cost[i] = config.k;
			}else {
				cost[i] = config.kprime - dist[i] + 1;
			}	
		}
		return cost;
	}
}