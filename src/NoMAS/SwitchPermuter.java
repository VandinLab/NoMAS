package NoMAS;
import java.util.*;

/**
 * Implementation of the concept expressed by the {@link DataPermuter} abstract class. It implements the Marginal Sums model permutation strategy.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class SwitchPermuter extends DataPermuter {
	/**
	 * The list of all existing links between patients and genes in form of collection of {@link Wire} instances.
	 */
	public ArrayList<Wire> wires;
	/**
	 * Genes represented as {@link Node} instances.
	 */
	public Node[] nodes;
	/**
	 * iterations to perform
	 */
	public int iterations;
	/**
	 * trigger to determine if the permuter is initialized or not
	 * 
	 */
	public boolean initialized;
	
	/**
	 * Permutes the mutation matrix by columns, displacing each mutation to a random 
	 */
	public void permute() {
		if(!initialized) {
			initialize();
		}
		// Perform random re-wirings
		for(int i=0; i<iterations; i++) {
			Wire a = wires.get(rng.nextInt(wires.size()));
			Wire b = wires.get(rng.nextInt(wires.size()));
			rewire(a, b);
		}
		// Encode new data in mutation matrix of the model
		for(int i=0; i<model.n; i++) {
			int[] x = model.vertices[i].gene.x;
			Bitstring.clear(x);
			for(Wire wire : nodes[i].wires) {
				Bitstring.setBit(x, wire.patient);
			}
		}
	}
	
	/**
	 * initializes the data structures of the permuter, linking each node to each patient who has a mutation in it. It also computes a lower bound of the number of iterations.
	 */
	public void initialize() {
		wires = new ArrayList<Wire>();
		nodes = new Node[model.n];
		for(int i=0; i<model.n; i++) {
			nodes[i] = new Node();
			int[] x = model.vertices[i].gene.x;
			for(int j=0; j<model.m; j++) {
				if(Bitstring.getBit(x, j) == 1) {
					Wire wire = new Wire();
					wire.gene = i;
					wire.patient = j;
					nodes[i].wires.add(wire);
					wires.add(wire);
				}
			}
		}
		// Compute lower bound on number of iterations
		double e = wires.size();
		double d = e/(model.n*model.m);
		double left = e/(2.0*(1.0-d));
		double right = Math.log((1.0-d)*e);
		iterations = (int)(left*right);
		initialized = true;
		System.err.println(iterations);
	}
	
	/**
	 * Switches, if there is no link between the two {@link Node} instances, the links ({@link Wire} instances) between two patients and two genes (i.e. switches the mutations in the two patients).
	 * 
	 * @param a The first {@link Wire} instance to switch.
	 * @param b The second {@link Wire} instance to switch.
	 */
	public void rewire(Wire a, Wire b) {
		if(nodes[a.gene].has(b.patient) || nodes[b.gene].has(a.patient)) {
			return;
		}
		int tmp = a.patient;
		a.patient = b.patient;
		b.patient = tmp;
	}
	
	/**
	 * Class that represents a link between a gene and a patient (i.e. the gene is mutated in patient)
	 * 
	 * @author Federico Altieri
	 * @author Tommy V. Hansen
	 * @author Fabio Vandin
	 *
	 */
	private class Wire {
		/**
		 * the gene index.
		 */
		int gene;
		
		/**
		 * the matient index.
		 */
		int patient;
	}

	/**
	 * Representation of a gene as an element of a network that connects the genes (the nodes) with the patients.
	 * 
	 * @author Federico Altieri
	 * @author Tommy V. Hansen
 	 * @author Fabio Vandin
	 * 
	 */
	private class Node {
		/**
		 * Collection of the {@link Wire} instances relative to the represented gene.
		 */
		ArrayList<Wire> wires;
		
		/**
		 * Default construction.
		 */
		public Node() {
			wires = new ArrayList<Wire>();
		}

		/**
		 * Checks if a node is linked with a particular patient.
		 * 
		 * @param i The patient index.
		 * @return true if the link exists. False elsewhere.
		 */
		public boolean has(int i) {
			for(Wire w : wires) {
				if(w.patient == i) {
					return true;
				}
			}
			return false;
		}
	}
}
