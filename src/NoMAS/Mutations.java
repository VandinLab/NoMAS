package NoMAS;
import java.util.*;
import java.io.*;

/**
 * A container of static methods for mutation matrixes creation and manipulation.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Mutations {
	
	/**
	 * Constructs mutation matrix and censoring information array.
	 *
	 * @param filename Path to the file containing the data to load.
	 * @param model {@link Model} instance to fill with the loaded data.
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
		printCtrlInformation(model, "\t");
	}
	
	/**
	 * Constructs mutation matrixes and censoring informations, splitting patients into two groups: train and control.
	 * It is invoked when using the holdolut approach for statistical validation.
	 *
	 * @param filename Path to the file containing the data to load.
	 * @param train {@link Model} instance of the data used to extract the candidate solutions.
	 * @param control {@link Model} instance of the data used to statistically validate solutions.
	 * @param timesplits Flag that determines if patients have to be partitioned in groups depending of their survival time.
	 * @param splits Number of groups to split the patients into.
	 * @param proportion For each group of patients, determines the proportion (0 = none, 1 = all) of patients to use in training group.
	 * @param seed Seed to initialize {@link Random} objects that perform randomization.
	 */
	public static void loadMutationMatrixes(String filename, Model train, Model control, boolean timesplits, int splits, double proportion, long seed) {
		BufferedReader reader = Utils.bufferedReader(filename);
		String line = Utils.readLine(reader);
		int m = Integer.parseInt(line);
		double[] times = new double[m];
		
		for(int i=0; i<m; i++) {
			String[] parts = Utils.readLine(reader).split("\t");
			times[i] = Double.parseDouble(parts[2]);	
		}
		
		int[] indexes = sorter(times);
		
		double tempDouble;
				
		for (int i = 0; i < m; i++) {
			int index = indexes[i];
			if (times[index] != times[i]) {
				tempDouble = times[i];
				times[i] = times[index];
				times[index] = tempDouble;
			}
		}
		
		Utils.close(reader);
		
		reader = Utils.bufferedReader(filename);
		line = Utils.readLine(reader); //reset reader
		
		//code for determing the split of datasets
		
		int[] sizes = null;
		
		if (!timesplits) {
			
			//the distribution of times could be reasonably associated with an uniformly distributed variable
			
			sizes = new int[splits];
			sizes[0] = m/splits;
			for (int i = 1; i < sizes.length; i++) {
				sizes[i] = sizes[0];
			}
			if (m%splits != 0) {
				for (int i = 0; i < m%splits; i++) {
					sizes[i]++;
				}
			}			
		}
		else {
			
			double distance = ((times[m-1] - times[0])/splits);
			double threshold = distance + times[0];
			// I try to split the data into time-contiguous parts 
			
			int[] markers = new int[m];
			markers[0] = 1;
			int ct = 1;
			for (int i = 1; i < m; i++) {
				// if the actual subgroup has samples within the threshold
				if (times[i] <= threshold) {
					//add the current element to the subgroup
					markers[ct] = markers[ct-1];
					ct++;
				}
				else {
					threshold += distance;
					//the element will be part of a new group
					markers[ct] = markers[ct-1]+1;
					ct++;
				}
				
			}
			//check markers and compose the sizes array
			sizes = new int[markers[m-1]];
			for (int i = 0; i < markers.length; i++) {
				sizes[markers[i]-1]++;
			}
			
			//check for spurious groups of size 1 and merges it with others close
			ct = 0; // counter of spurious groups
			for (int i = 0; i < sizes.length-1; i++) {
				if (sizes[i] == 1) {
					sizes[i] = 0;
					sizes[i+1]++;
				}
			}
			if (sizes[sizes.length-1] == 1) {
				sizes[sizes.length-1] = 0;
				sizes[sizes.length-2]++;
			}
			
		}		
		
		//data split through indexes: define an array of 0/1 to define either if an entry belongs to training or control group
		int[] group = new int[m];
		
		for (int i = 0; i < group.length; i++) {
			group[i] = 1; //initiation: mark all elements as control
		}
		
		Random generator = new Random(seed);
		long[] seeds = new long[sizes.length];
		
		for (int i = 0; i < sizes.length; i++) {
			seeds[i] = generator.nextLong();
		}
		
		int ct = 0;
		
		//for each subgroup
		for (int i = 0; i < sizes.length; i++) {
			
			// split it in two parts
			int tr = (int) (sizes[i]*proportion);
			int ctrl = sizes[i]-tr;
			
			Stack<Integer> s = new Stack<>();
			
			for (int j = 0; j<sizes[i]; j++) {
				s.push(new Integer(j));
			}
			
			Collections.shuffle(s, new Random(seeds[i]));
			
			//mark training
			for (int j = 0; j < tr; j++) {
				group[ct+((Integer)s.pop()).intValue()] = 0; //training group with random pickup
			}
//			
//			//mark control
//			for (int j = 0; j < ctrl; j++) {
//				group[ct++] = 1; //control group
//			}
			
			train.m = train.m+tr;
			control.m = control.m+ctrl;
			
			ct = ct + sizes[i];
		}
		
		//split the indexes array to keep both dataset ordered
		int[] finalIndexes = new int[m];
		
		//expedient for code reuse
		double[] tempInd = new double[m];
		for (int i = 0; i < tempInd.length; i++) {
			tempInd[i] = (double)indexes[i];
		}
		
		int ctTr = 0;
		int ctCtrl = 0;
		int[] temp = sorter(tempInd);
		for (int i = 0; i < group.length; i++) {
			if (group[temp[i]] == 0) {
				finalIndexes[indexes[temp[i]]] = ctTr;
				ctTr++;
			}
			else {
				finalIndexes[indexes[temp[i]]] = ctCtrl;
				ctCtrl++;
			}
		}
		
		train.c = new int[train.m];
		train.times = new double[train.m];
		train.patient_ids = new String[train.m];
		
		control.c = new int[control.m];
		control.times = new double[control.m];
		control.patient_ids = new String[control.m];
		
				
		HashMap<String, ArrayList<Gene>> map = new HashMap<String, ArrayList<Gene>>();
		for(int i=0; i<train.n; i++) {
			train.genes[i].x = Bitstring.getEmpty(train.m);
			
			ArrayList<Gene> list = map.get(train.genes[i].symbol);
			if(list == null) {
				list = new ArrayList<Gene>();
				list.add(train.genes[i]);
				map.put(train.genes[i].symbol, list);
			}else {
				list.add(train.genes[i]);
			}
		}
		
		HashMap<String, ArrayList<Gene>> mapCtrl = new HashMap<String, ArrayList<Gene>>();
		for(int i=0; i<control.n; i++) {
			control.genes[i].x = Bitstring.getEmpty(control.m);
			
			ArrayList<Gene> list = mapCtrl.get(control.genes[i].symbol);
			if(list == null) {
				list = new ArrayList<Gene>();
				list.add(control.genes[i]);
				mapCtrl.put(control.genes[i].symbol, list);
			}else {
				list.add(control.genes[i]);
			}
		}
//		
		// I now need to organize the indexes to insert all the data ordered by time
		
		
		for(int i=0; i<m; i++) {
			int index = indexes[i];
			if (group[index] == 0) {
				//to the training group
				String[] parts = Utils.readLine(reader).split("\t");
				train.patient_ids[finalIndexes[i]] = parts[0];
				train.c[finalIndexes[i]] = Integer.parseInt(parts[1]);
				train.times[finalIndexes[i]] = Double.parseDouble(parts[2]);
				for(int j=3; j<parts.length; j++) {
					ArrayList<Gene> list = map.get(parts[j]);
					if(list != null) {
						for(Gene gene : list) {
							Bitstring.setBit(gene.x, finalIndexes[i]);
						}
					}
				}
			}
			else
			{
				//to the control group
				String[] parts = Utils.readLine(reader).split("\t");
				control.patient_ids[finalIndexes[i]] = parts[0];
				control.c[finalIndexes[i]] = Integer.parseInt(parts[1]);
				control.times[finalIndexes[i]] = Double.parseDouble(parts[2]);
				for(int j=3; j<parts.length; j++) {
					ArrayList<Gene> list = mapCtrl.get(parts[j]);
					if(list != null) {
						for(Gene gene : list) {
							Bitstring.setBit(gene.x, finalIndexes[i]);
						}
					}
				}				
			}
		}
		
		for(int i=0; i<train.n; i++) {
			train.genes[i].m1 = Bitstring.numberOfSetBits(train.genes[i].x);
		}
		
		for(int i=0; i<control.n; i++) {
			control.genes[i].m1 = Bitstring.numberOfSetBits(control.genes[i].x);
		}
		
		Utils.close(reader);
		train.w = Censoring.computeWeights(train.c);
		control.w = Censoring.computeWeights(control.c);
		
		train.norm_coef = Censoring.computeNormCoef(train.c);
		control.norm_coef = Censoring.computeNormCoef(control.c);
		
		train.matrix_file = filename;
		control.matrix_file = filename;
		
		train.log.stream.println("[Mutation matrix] File name = "+train.matrix_file);
		printInformation(train, "\t");

		control.log.stream.println("[Mutation matrix] File name = "+control.matrix_file);
		printCtrlInformation(control, "\t");
	}
	
    /**
	 * Reads a {@link Model} instance passed as parameter and constructs a random mutation matrix and a random censoring information array that maintain the same network, the same reduction conditions and the same censoring ratio.
	 * The number of patients is then scaled to maintain all proportions.
	 *
	 * @param rng {@link Random} instance that performs the random extractions.
	 * @param model_ref {@link Model} instance to use as source of the information to maintain 
	 * @param m The number of patients that the new {@link Model} instance will have.
	 * @return The new {@link Model} instance created. 
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
	
	/**
	 * Clears mutations of genes to be ignored for some reason. Genes to ignore are written in a file. 
	 * 
	 * @param model {@link Model} instance to clean.
	 * @param filename Path to the file containing the genes to be cleared
	 */
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
	 * Removes mutations found in genes that are mutated in a less than the threshold patients quantity.
	 *
	 * @param model {@link Model} instance to clean.
	 * @param threshold Threshold, expressed as number of patients.
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
	
	/**
	 * Counts the number of mutations in patients data.
	 * 
	 * @param model {@link Model} instance with data.
	 * @return the number of mutations
	 */
	public static int numberOfMutations(Model model) {
		int count = 0;
		for(Gene g : model.genes) {
			count += g.m1;
		}
		return count;
	}
	
	/**
	 * Counts the number of uncensored patients.
	 * 
	 * @param model {@link Model} instance with data.
	 * @return the number of uncensored patients
	 */
	public static int numberOfUncensored(Model model) {
		int uncensored = 0;
		for(int i : model.c) {
			uncensored += i;
		}
		return uncensored;
	}
	
	/**
	 * Counts the genes that present at least a mutation in patients data.
	 * 
	 * @param model {@link Model} instance with data.
	 * @return the number of genes with at least a mutation in patients.
	 */
	public static int numberOfMutatedGenes(Model model) {
		int mutated = 0;
		for(Gene g : model.genes) {
			if(g.m1 > 0) {
				mutated++;
			}
		}
		return mutated;
	}
	
	/**
	 * Prints some informations about the data in the log file: number of patients, uncensored ratio, number of mutations and number of mutated genes.
	 * 
	 * @param model {@link Model} instance with data.
	 * @param prefix a prefix to place in front of the printed lines 
	 */
	public static void printInformation(Model model, String prefix) {
		model.log.stream.println(prefix+"Number of patients: "+model.m);
		model.log.stream.println(prefix+"Uncensored ratio: "+Utils.round(numberOfUncensored(model)/(double)model.m, 2));
		model.log.stream.println(prefix+"Number of mutations: "+numberOfMutations(model));
		model.log.stream.println(prefix+"Number of mutated genes: "+numberOfMutatedGenes(model));
		model.log.stream.flush();
	}
	
	/**
	 * Prints some informations about the data in the log file about the validation group of patients: number of patients, uncensored ratio, number of mutations and number of mutated genes.
	 * To be invoked when using the holdout approach for statistical validation
	 * 
	 * @param model {@link Model} instance with data about the validation set of patients.
	 * @param prefix a prefix to place in front of the printed lines 
	 */
	public static void printCtrlInformation(Model model, String prefix) {
		model.log.stream.println(prefix+"Number of control patients: "+model.m);
		model.log.stream.println(prefix+"Number of mutations in control patients: "+numberOfMutations(model));
		model.log.stream.println(prefix+"Number of mutated genes in control patients: "+numberOfMutatedGenes(model));
		model.log.stream.flush();
	}
	
	/**
	 * Writes the informations about the mutations in patients for the genes of a specific {@link Solution} instance.
	 * 
	 * @param model {@link Model} instance with data.
	 * @param solution {@link Solution} instance to print.
	 * @param out {@link Output} instance that performs the file writing.
	 */
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
	
	/**
	 * Sorts the patients and re-organizes the mutation matrix and survival data based on survival time of patients (from shortest to longest) by creating an array of indexes.
	 *
	 * @param times Array of doubles containing the times to sort.
	 * @return An array of indexes representing the new sorting of the patients. 
	 */
	private static int[] sorter (double[] times) {
		
		int[] indexes = new int[times.length];
		
		//need to convert to Double objs to quickly exploit standard library's sorting method 
		Double [][] temp = new Double[times.length][2];
		for(int i=0; i<times.length; i++) {
			temp[i][1] = new Double(times[i]);
			temp[i][0] = new Double((double)i);
		}
		mysort(temp);
		
		// re-conversion to int with truncation
		
		for(int i=0; i<times.length; i++) {
			indexes[i] = (int) temp[i][0].doubleValue();
		}
		
		return indexes;
			
	}
	
	
	
	/**
	 * Private method to sort data and track indexes.
	 *
     * @param ar The array to sort
     * @return The sorted array
     */
    private static void mysort(Double[][] ar) {
        Arrays.sort(ar, new Comparator<Double[]>() {
            @Override
            public int compare(Double[] int1, Double[] int2) {
            	Double numOfKeys1 = int1[1];
            	Double numOfKeys2 = int2[1];
                return numOfKeys1.compareTo(numOfKeys2);
            }
        });
    }
}