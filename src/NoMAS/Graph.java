package NoMAS;
import java.util.*;
import java.io.*;

public class Graph {
	/**
	 * Constructs a graph in the given model.
	 *
	 * @param filename File containing the graph data
	 * @param model The model to contain the graph
	 */
	public static void loadGraph(String filename, Model model) {		
		BufferedReader reader = Utils.bufferedReader(filename);
		String line = Utils.readLine(reader);
		
		model.n = Integer.parseInt(line);
		model.vertices = new Vertex[model.n];
		model.genes = new Gene[model.n];	
		
		for(int i=0; i<model.n; i++) {
			model.vertices[i] = new Vertex();
			model.genes[i] = new Gene();
			
			model.vertices[i].id = i;
			model.vertices[i].gene = model.genes[i];
		}	
		
		for(int i=0; i<model.n; i++) {
			String[] parts = Utils.readLine(reader).split("\t");
			model.genes[i].symbol = parts[1];
			for(int j=2; j<parts.length; j++) {
				Vertex neighbor = model.vertices[Integer.parseInt(parts[j]) - 1];
				model.vertices[i].addNeighbor(neighbor);
			}
		}
		
		Utils.close(reader);
		model.graph_file = filename;
		model.log.stream.println("[Graph] File name: "+model.graph_file);
		printInformation(model, "\t");
	}

	/**
	 * Reduces the size of the given graph by removing vertices.
	 *
	 * @param model The model containing the graph to be reduced
	 * @param conditions Conditions for a vertex to remain in the graph
	 */
	public static void reduce(Model model, int conditions) {
		model.reduction_conditions = conditions;
		ArrayList<Vertex> good_vertices = new ArrayList<Vertex>();
		ArrayList<Gene> good_genes = new ArrayList<Gene>();
		
		for(int i=0; i<model.n; i++) {
			Vertex v = model.vertices[i];
			if(Model.includeVertex(v, model.genes[i], conditions)) {
				// Add vertex and assosiated gene to include lists
				good_vertices.add(v);
				good_genes.add(model.genes[i]);
			}else {
				// Remove edges to vertex from its neighbors
				for(Vertex u : v.neighbors) {
					u.neighbors.remove(v);
				}
			}
		}

		if(good_vertices.size() != model.n) {
			// Update vertex and gene lists, and recurse
			model.n = good_vertices.size();	
			model.vertices = new Vertex[model.n];
			model.genes = new Gene[model.n];
			for(int i=0; i<model.n; i++) {
				model.vertices[i] = good_vertices.get(i);
				model.vertices[i].id = i; // update vertex id
				model.genes[i] = good_genes.get(i);
			}
			
			model.log.stream.println("[Graph] Reduction performed");
			printInformation(model, "\t");
			
			reduce(model, conditions);
		}
	}

	/**
	 * Assigns a random color to each vertex of the given graph
	 *
	 * @param seed The seed to use for the random selection of colors
	 * @param k The number of different colors to choose from
	 * @param model The model containing the graph to be colored 
	 */
	public static void color(int seed, int k, Model model) {
		Random rng = new Random(seed);
		for(Vertex v : model.vertices) {
			v.color = rng.nextInt(k);
		}
	}
	
	public static boolean isColorful(ArrayList<Vertex> vertices, int d) {
		boolean[] seen = new boolean[d];
		for(Vertex v : vertices) {
			if(seen[v.color]) {
				return false;
			}
			seen[v.color] = true;
		}
		return true;
		
	}
	
	public static int[] shortestPaths(Model model, ArrayList<Vertex> sources) {
		boolean[] visited = new boolean[model.n];
		int[] dist = new int[model.n];
		Arrays.fill(dist, Integer.MAX_VALUE);
		LinkedList<Vertex> fifo = new LinkedList<Vertex>();
		for(Vertex v : sources) {
			fifo.add(v);
			dist[v.id] = 0;
			visited[v.id] = true;
		}
		while(fifo.size() > 0) {
			Vertex v = fifo.removeFirst();
			for(Vertex u : v.neighbors) {
				if(!visited[u.id]) {					
					fifo.add(u);
					dist[u.id] = dist[v.id]+1;
					visited[u.id] = true;
				}
			}
		}
		return dist;
	}
	
	public static ArrayList<Vertex> randomSubgraph(Model model, Random rng, int k) {
		int iterations = 0;
		ArrayList<Vertex> S = new ArrayList<Vertex>();
		Vertex v = model.vertices[rng.nextInt(model.n)];
		if(v.neighbors.size() == 0) {
			return randomSubgraph(model, rng, k);
		}
		while(S.size() < k) {
			if(!S.contains(v)) {
				S.add(v);
			}
			v = v.neighbors.get(rng.nextInt(v.neighbors.size()));
			iterations++;
			if(iterations >= 100000) {
				return randomSubgraph(model, rng, k);
			}
		}
		return S;
	}
	
	public static int overlap(ArrayList<Vertex> A, ArrayList<Vertex> B) {
		int overlap = 0;
		for(Vertex a : A) {
			for(Vertex b : B) {
				if(a.id == b.id) {
					overlap++;
					break;
				}
			}
		}
		return overlap;
	}
	
	public static void printInformation(Model model, String prefix) {
		model.log.stream.println(prefix+"Condition conditions: "+model.reduction_conditions);
		model.log.stream.println(prefix+"Number of vertices: "+model.n);
		model.log.stream.println(prefix+"Number of edges: "+numberOfEdges(model));
		int[] cc_sizes = connectedComponents(model);
		model.log.stream.println(prefix+"Numer of connected components: "+cc_sizes.length);
		model.log.stream.println(prefix+"Largest connected component: "+cc_sizes[cc_sizes.length-1]);
		model.log.stream.println(prefix+"Number of isolated vertices: "+numberOfIsolated(model));
		model.log.stream.flush();
	}
	
	public static int numberOfEdges(Model model) {
		int edges = 0;
		for(Vertex v : model.vertices) {
			edges += v.neighbors.size();
		}
		return edges/2;
	}
	
	public static int numberOfIsolated(Model model) {
		int isolated = 0;
		for(Vertex v : model.vertices) {
			if(v.neighbors.size() == 0) {
				isolated++;
			}
		}
		return isolated;
	}
	
	public static Vertex getVertexBySymbol(Model model, String symbol) {
		for(Vertex v : model.vertices) {
			if(v.gene.symbol.equals(symbol)) {
				return v;
			}
		}
		return null;
	}
	
	public static int largestDegree(Model model) {
		int largest = 0;
		for(Vertex v : model.vertices) {
			if(v.degree > largest) {
				largest = v.degree;
			}
		}
		return largest;
	}
	
	public static int[] connectedComponents(Model model) {
		boolean[] added = new boolean[model.n];
		int[] component = new int[model.n];
		int current_component = 0;
		for(Vertex w : model.vertices) {
			
			if(!added[w.id]) {
				LinkedList<Vertex> queue = new LinkedList<Vertex>();
				queue.add(w);
				added[w.id] = true;
				while(queue.size() > 0) {
					Vertex v = queue.removeFirst();
					component[v.id] = current_component;
					
					for(Vertex u : v.neighbors) {
						
						if(!added[u.id]) {
							added[u.id] = true;
							queue.add(u);
						}
					}
				}
				current_component++;
			}
		}
		
		int[] component_sizes = new int[current_component];
		for(Vertex v : model.vertices) {
			component_sizes[component[v.id]]++;
		}
		Arrays.sort(component_sizes);
		return component_sizes;
	}
}
