package NoMAS;
import java.util.*;

public class IdentityPermuter extends DataPermuter {
	public void permute() {
		int[] indices = Utils.indexArray(model.n);
		Utils.shuffle(rng, indices);
		for(int i=0; i<model.n; i++) {
			model.vertices[i].gene = model.genes[indices[i]];
		}
	}
}