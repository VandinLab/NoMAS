package NoMAS;
import java.util.*;

public class SwitchPermuter extends DataPermuter {
	public ArrayList<Wire> wires;
	public Node[] nodes;
	public int iterations;
	public boolean initialized;
	
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
	
	public void initialize() {
		wires = new ArrayList<Wire>();
		nodes = new Node[model.n];
		for(int i=0; i<model.n; i++) {
			nodes[i] = new Node(i);
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
	
	public void rewire(Wire a, Wire b) {
		if(nodes[a.gene].has(b.patient) || nodes[b.gene].has(a.patient)) {
			return;
		}
		int tmp = a.patient;
		a.patient = b.patient;
		b.patient = tmp;
	}
	
	private class Wire {
		int gene, patient;
	}

	private class Node {
		ArrayList<Wire> wires;

		public Node(int i) {
			wires = new ArrayList<Wire>();
		}

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
