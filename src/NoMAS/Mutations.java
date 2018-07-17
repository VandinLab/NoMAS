package NoMAS;
import java.util.*;
import java.io.*;

public class Mutations {
	/**
	 * Constructs mutation matrix and censoring information.
	 */
	public static void loadMutationMatrix(String filename, Model model) {
		BufferedReader reader = Utils.bufferedReader(filename);
		String line = Utils.readLine(reader);
		model.m = Integer.parseInt(line);
		model.c = new int[model.m];
		model.times = new double[model.m];
		model.patient_ids = new String[model.m];
		
		HashMap<String, ArrayList<Gene>> map = new HashMap<String, ArrayList<Gene>>();
		for(int i=0; i<model.n; i++) {
			model.genes[i].x = Bitstring.getEmpty(model.m);
			
			ArrayList<Gene> list = map.get(model.genes[i].symbol);
			if(list == null) {
				list = new ArrayList<Gene>();
				list.add(model.genes[i]);
				map.put(model.genes[i].symbol, list);
			}else {
				list.add(model.genes[i]);
			}
		}
		int count = 0;
		for(int i=0; i<model.m; i++) {
			String[] parts = Utils.readLine(reader).split("\t");
			model.patient_ids[i] = parts[0];
			model.c[i] = Integer.parseInt(parts[1]);
			model.times[i] = Double.parseDouble(parts[2]);
			for(int j=3; j<parts.length; j++) {
				ArrayList<Gene> list = map.get(parts[j]);
				if(list != null) {
					for(Gene gene : list) {
						Bitstring.setBit(gene.x, i);
						count++;
					}
				}
			}
		}
		
		for(int i=0; i<model.n; i++) {
			model.genes[i].m1 = Bitstring.numberOfSetBits(model.genes[i].x);
		}
		
		Utils.close(reader);
		model.w = Censoring.computeWeights(model.c);
		model.norm_coef = Censoring.computeNormCoef(model.c);
		model.matrix_file = filename;
		
		model.log.stream.println("[Mutation matrix] File name = "+model.matrix_file);
		printInformation(model, "\t");
	}
	
    /**
	 * Constructs mutation matrix and censoring information based on an existing one, and scales the number of patiens
	 */
	public static Model getSimulatedData(Random rng, Model model_ref, int m) {
		Model model = new Model();
		Graph.loadGraph(model_ref.graph_file, model);
		model.reduction_conditions = model_ref.reduction_conditions;
		model.mutation_threshold = model_ref.mutation_threshold;
		model.m = m;
		double cr = Censoring.censoringRatio(model_ref.c);
		model.c = Censoring.randomCensoring(rng, cr, m);
		
		for(int i=0; i<model.n; i++) {
			int j = rng.nextInt(model_ref.n);
			int m1 = (int)((double)m*(model_ref.genes[j].m1/(double)model_ref.m));
			model.genes[i].x = Bitstring.randomBitstring(rng, model.m, m1);
			model.genes[i].m1 = Bitstring.numberOfSetBits(model.genes[i].x);
		}
		
		model.w = Censoring.computeWeights(model.c);
		model.norm_coef = Censoring.computeNormCoef(model.c);
		return model;
	}
	
	public static void removeMutationsInGenes(Model model, String filename) {
		BufferedReader file = Utils.bufferedReader(filename);
		String line = null;
		while((line = Utils.readLine(file)) != null) {
			Vertex v = Graph.getVertexBySymbol(model, line);
			if(v != null) {
				v.gene.m1 = 0;
				Bitstring.clear(v.gene.x);
				System.err.println("Mutations removed from gene: "+v.gene.symbol);
			}
		}
		Utils.close(file);
	}
	
    /**
	 * Removes mutations found in genes that are mutated in < threshold patients
	 */
	public static void removeMutations(Model model, double threshold) {
		for(Gene g : model.genes) {
			if(g.m1 < threshold) {
				Bitstring.clear(g.x);
				g.m1 = 0;
			}
		}
		model.mutation_threshold = threshold;
		model.log.stream.println("[Mutation matrix] Mutations removed with threshold = "+Utils.round(threshold, 2));
		printInformation(model, "\t");
	}
	
	public static int numberOfMutations(Model model) {
		int count = 0;
		for(Gene g : model.genes) {
			count += g.m1;
		}
		return count;
	}
	
	public static int numberOfUncensored(Model model) {
		int uncensored = 0;
		for(int i : model.c) {
			uncensored += i;
		}
		return uncensored;
	}
	
	public static int numberOfMutatedGenes(Model model) {
		int mutated = 0;
		for(Gene g : model.genes) {
			if(g.m1 > 0) {
				mutated++;
			}
		}
		return mutated;
	}
	
	public static void printInformation(Model model, String prefix) {
		model.log.stream.println(prefix+"Number of patients: "+model.m);
		model.log.stream.println(prefix+"Uncensored ratio: "+Utils.round(numberOfUncensored(model)/(double)model.m, 2));
		model.log.stream.println(prefix+"Number of mutations: "+numberOfMutations(model));
		model.log.stream.println(prefix+"Number of mutated genes: "+numberOfMutatedGenes(model));
		model.log.stream.flush();
	}
	
	public static void writeMutationInfo(Model model, Solution solution, Output out) {
		out.stream.println("patient_id\tsurvival_time\tcensoring\tmutation_status");
		for(int i=0; i<model.m; i++) {
			int mutated = 0;
			for(Vertex v : solution.vertices) {
				if(Bitstring.getBit(v.gene.x, i) == 1) {
					mutated = 1;
					break;
				}
			}
			out.stream.println(model.patient_ids[i]+"\t"+model.times[i]+"\t"+model.c[i]+"\t"+mutated);
		}
	}
}