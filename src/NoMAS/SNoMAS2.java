package NoMAS;
import java.util.*;

public class SNoMAS2 extends AbstractSNoMAS {
	public SNoMAS2(Model model, Configuration config) {
		super(model, config);
	}

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