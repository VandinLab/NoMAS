package NoMAS;
import java.util.*;
import java.io.*;

/**
 * Class that loads the the main network and performs basic operations. All methods are static 
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Graph {
	/**
	 * Constructs a graph in the given {@link Model} instance.
	 *
	 * @param filename {@link String} containing path of the graph data file
	 * @param model The {@link Model} to contain the graph
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
	 * @param model The {@link Model} containing the graph to be reduced
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
	 * @param model The {@link Model} containing the graph to be colored 
	 */
	public static void color(int seed, int k, Model model) {
		Random rng = new Random(seed);
		for(Vertex v : model.vertices) {
			v.color = rng.nextInt(k);
		}
	}
	
	/**
	 * Given a list of {@link Vertex} instances, determines if this set is colorful (i.e. each {@link Vertex} has a different color) 
	 * 
	 * @param vertices {@link ArrayList} of {@link Vertex} instances to analyze
	 * @param d the number of colors
	 * @return true if the set is colorful
	 */
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
	
	/**
	 * Computes the minimum distances of all nodes in the network from a node in the sources set 
	 * 
	 * @param model The {@link Model} that stores information about data
	 * @param sources {@link ArrayList} of {@link Vertex} instances using as sources of the paths
	 * @return an array of integers containing the minimum distances to each node starting from a note in the sources set 
	 */
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
	
	/**
	 * Picks a random connected subgraph from the current network stored in the passed {@link Model} instance.
	 * 
	 * @param model {@link Model} containing input data.
	 * @param rng {@link Random} instance that performs random numbers extraction.
	 * @param k Size of the subgraph to pick.
	 * @return The subgraph extracted in form of an {@link ArrayList} of {@link Vertex} instances.
	 */
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
	
	/**
	 * Determines how many nodes two subgraphs share
	 * 
	 * @param A A subgraph in form of an {@link ArrayList} of {@link Vertex} instances.
	 * @param B Another subgraph in form of an {@link ArrayList} of {@link Vertex} instances. 
	 * @return the number of shared nodes
	 */
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
	
	/**
	 * Prints the information on the graph using the {@link Output} instance contained in the passed {@link Model} instance.
	 * 
	 * @param model {@link Model} instance with data.
	 * @param prefix A fixed prefix to print at the beginning of each line.
	 */
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
	
	/**
	 * Counts the edges of a network.
	 * 
	 * @param model {@link Model} instance with data.
	 * @return the number of edges.
	 */
	public static int numberOfEdges(Model model) {
		int edges = 0;
		for(Vertex v : model.vertices) {
			edges += v.neighbors.size();
		}
		return edges/2;
	}
	
	/**
	 * Counts the isolated nodes of the network (i.e. nodes without neighbors).
	 * 
	 * @param model {@link Model} instance with data.
	 * @return the number of isolated nodes.
	 */
	public static int numberOfIsolated(Model model) {
		int isolated = 0;
		for(Vertex v : model.vertices) {
			if(v.neighbors.size() == 0) {
				isolated++;
			}
		}
		return isolated;
	}
	
	/**
	 * Returns, if present, the instance of {@link Vertex} corresponding to the gene with symbol in input.
	 * 
	 * @param model {@link Model} instance with data.
	 * @param symbol The symbol of the gene to look for.
	 * @return The corresponding instance of {@link Vertex} if present, null elsewhere.
	 */
	public static Vertex getVertexBySymbol(Model model, String symbol) {
		for(Vertex v : model.vertices) {
			if(v.gene.symbol.equals(symbol)) {
				return v;
			}
		}
		return null;
	}
	
	/**
	 * Returns the maximum degree present in the network.
	 * 
	 * @param model {@link Model} instance with data.
	 * @return the maximum degree.
	 */
	public static int largestDegree(Model model) {
		int largest = 0;
		for(Vertex v : model.vertices) {
			if(v.degree > largest) {
				largest = v.degree;
			}
		}
		return largest;
	}
	
	/**
	 * Returns an array with the size of the connected components of the graph. At a certain index, the array stores the number of other nodes reachable from the corresponding node. 
	 * 
	 * @param model {@link Model} instance with data.
	 * @return an array of integers with the size of corresponding connected components.
	 */
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
