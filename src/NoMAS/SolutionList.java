package NoMAS;

import java.io.*;
import java.util.*;

/**
 * Container os static methods to perform various kinds of operation between {@link Solution} instances. 
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class SolutionList {
    /**
	 * Inserts a {@link Solution} instance into the given array of {@link Solution} instances, keeping it sorted (Insertion sort ascending order).
	 * Does nothing if an equivalent solution is already in the array
	 *
	 * @param list Array of {@link Solution} instances to update.
	 * @param s {@link Solution} instance to insert.
	 * @param objective Objective function to determine solution sorting.
	 */
	public static void insert(Solution[] list, Solution s, Objective objective) {
		if(s == null) {
			return;
		}
		int best_fit = list.length;
		for(int i=list.length-1; i>=0; i--) {
			if(list[i] != null) {
				int comparison = objective.compare(list[i], s);
				if(comparison > 0) {
					break;
				}else if(comparison == 0 && Solution.isEqual(list[i], s)) {
					return;
				}
			}
			best_fit = i;
		}
		if(best_fit < list.length) {
			for(int j=list.length-1; j>best_fit; j--) {
				list[j] = list[j-1];
			}
			list[best_fit] = s;
		}
	}
	
    /**
	 * Inserts a {@link Solution} instance into the given array of {@link Solution} instances, keeping it sorted (Insertion sort ascending order).
     * Does nothing if the array already contains a solution that has at most two different vertexes. 
	 *
	 * @param list Array of {@link Solution} instances to update.
	 * @param s {@link Solution} instance to insert.
	 * @param objective Objective function to determine solution sorting.
	 */
	public static void insertUnique(Solution[] list, Solution s, Objective objective) {
		if(s == null) {
			return;
		}
		int best_fit = list.length;
		for(int i=list.length-1; i>=0; i--) {
			if(list[i] != null) {
				int comparison = objective.compare(list[i], s);
				if(comparison > 0) {
					break;
				}else if(comparison == 0 && Solution.uniqueness(list[i], s) <= 1) {
					return;
				}
			}
			best_fit = i;
		}
		if(best_fit < list.length) {
			for(int j=list.length-1; j>best_fit; j--) {
				list[j] = list[j-1];
			}
			list[best_fit] = s;
		}
	}

	/**
	 * For each instance of {@link Solution} in a collection, computes and stores inside it the number of the patients with at least a mutated gene in the respective solution subnetwork and their logrank statistic value.
	 * 
	 * @param model Instance of {@link Model} containing patients data.
	 * @param solutions Collection of {@link Solution} instances to update.
	 */
	public static void computeLogrank(Model model, Solution... solutions) {
		for(Solution solution : solutions) {
			solution.computePopulationVector(model);
			solution.computeLogrankStatistic(model);
		}
	}
	
	/**
	 * For each instance of {@link Solution} in a collection, computes and stores inside it the number of the patients in the validation set with at least a mutated gene in the respective solution subnetwork and their logrank statistic value.
	 * To be invoked when using the holdout approach to perform statistical validation of solutions.
	 * 
	 * @param control Instance of {@link Model} containing data of patients in the validation set.
	 * @param all Instance of {@link Model} containing data of all patients.
	 * @param solutions Collection of {@link Solution} instances to update.
	 */
	public static void computeLogrankCrossval(Model control, Model all, Solution... solutions) {
		for(Solution solution : solutions) {
			solution.computePopulationVectorCrossval(control, all);
			solution.computeLogrankStatisticCrossval(control);
		}
	}
	
	/**
	 * Merges multiple collections of {@link Solution} instances in an unique collection.
	 * 
	 * @param size Size of the merged collection
	 * @param objective Objective function to determine solution sorting.
	 * @param lists Array of arrays of {@link Solution} instances to merge.
	 * @return
	 */
	public static Solution[] merge(int size, Objective objective, Solution[]... lists) {
		Solution[] solutions = new Solution[size];
		for(Solution[] list : lists) {
			for(Solution solution : list) {
				insert(solutions, solution, objective);
			}
		}
		return solutions;
	}
	
	/**
	 * Computes the index where a {@link Solution} instance would be placed in a list of ascending ordered solutions. 
	 * 
	 * @param s {@link Solution} instance to rank.
	 * @param solutions Collection of {@link Solution} instances ordered ascending.
	 * @return The rank expressed as an index of the array.
	 */
	public static int rank(Solution s, Solution... solutions) {
		for(int i=0; i<solutions.length; i++) {
			if(solutions[i].equals(s)) {
				return i+1;
			}
		}
		return solutions.length+1;
	}
	
	/**
	 * Creates a new collection of {@link Solution} instances "parsed" from a properly written text file.
	 * 
	 * @param model Instance of {@link Model} containing patients data.
	 * @param filename Path to the file with the written solutions.
	 * @return The collection of parsed {@link Solution} instances
	 */
	public static Solution[] fromFile(Model model, String filename) {
		ArrayList<Solution> list = new ArrayList<Solution>();
		BufferedReader reader = Utils.bufferedReader(filename);
		String line = Utils.readLine(reader);
		while(!(line = Utils.readLine(reader)).equals("")) {
			String[] tokens = line.split("#")[1].split("\t");
			Solution solution = null;
			for(String token : tokens) {
				for(Vertex v : model.vertices) {
					if(v.gene.symbol.equals(token)) {
						if(solution == null) {
							solution = new Solution(v, model);
						}else {
							solution = Solution.merge(solution, new Solution(v, model), model);
						}
						break;
					}
				}
			}
			tokens = line.split("#")[0].split("\t");
			solution.pv = Double.parseDouble(tokens[3]);
			solution.ppv = Double.parseDouble(tokens[4]);
			list.add(solution);
		}
		Solution[] solutions = new Solution[list.size()];
		for(int i=0; i<solutions.length; i++) {
			solutions[i] = list.get(i);
		}
		return solutions;
	}
	
	/**
	 * Concatenates a collection of {@link Solution} instances by adding references to single instances, creating a "pseudo-List"
	 * 
	 * @param solutions The collection of {@link Solution} instances to concatenate.
	 * @return {@link Solution} instance at the beginning of the list.
	 */
	public static Solution toLinkedList(Solution... solutions) {
		for(int i=solutions.length-1; i>=0; i--) {
			if(solutions[i] != null) {
				solutions[i].next = null;
				break;
			}
		}
		for(int i=solutions.length-2; i>=0; i--) {
			if(solutions[i] != null) {
				solutions[i].next = solutions[i+1];
			}
		}
		return solutions[0];
	}
}
