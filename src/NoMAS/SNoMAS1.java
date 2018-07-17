package NoMAS;
import java.util.*;

public class SNoMAS1 extends AbstractSNoMAS {
	public SNoMAS1(Model model, Configuration config) {
		super(model, config);
	}

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