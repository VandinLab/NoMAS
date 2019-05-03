package NoMAS;

/**
 * Implementation of the concept expressed by the {@link DataPermuter} abstract class. It implements the Gene Identity model permutation strategy.
 * 
 * @author Federico  Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class IdentityPermuter extends DataPermuter {
	
	/**
	 * Permutes the genes in the network using the Gene Identity model. 
	 */
	public void permute() {
		int[] indices = Utils.indexArray(model.n);
		Utils.shuffle(rng, indices);
		for(int i=0; i<model.n; i++) {
			model.vertices[i].gene = model.genes[indices[i]];
		}
	}
}