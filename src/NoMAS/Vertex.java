package NoMAS;
import java.util.*;

public class Vertex {
	public int id, color, degree;
	public Gene gene;
	public ArrayList<Vertex> neighbors;

	public Vertex() {
		neighbors = new ArrayList<Vertex>();
	}
	
	public void addNeighbor(Vertex v) {
		if(!neighbors.contains(v)) {
			neighbors.add(v);
			degree = neighbors.size();
		}
	}
}
