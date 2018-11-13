package NoMAS;

import java.io.*;
import java.util.*;

public class SolutionList {
    /**
	 * Insert a solution into the given list of solutions. Insert-sort-style.
     * Only contains distinct solutions.
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
	 * Insert a solution into the given list of solutions. Insert-sort-style
     * Only contains solutions that differ by at least 2 genes
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

	public static void computeLogrank(Model model, Solution... solutions) {
		for(Solution solution : solutions) {
			solution.computePopulationVector(model);
			solution.computeLogrankStatistic(model);
		}
	}
	
	public static void computeLogrankCrossval(Model control, Model all, Solution... solutions) {
		for(Solution solution : solutions) {
			solution.computePopulationVectorCrossval(control, all);
			solution.computeLogrankStatisticCrossval(control);
		}
	}
	
	public static Solution[] merge(int size, Objective objective, Solution[]... lists) {
		Solution[] solutions = new Solution[size];
		for(Solution[] list : lists) {
			for(Solution solution : list) {
				insert(solutions, solution, objective);
			}
		}
		return solutions;
	}
	
	public static int rank(Solution s, Solution... solutions) {
		for(int i=0; i<solutions.length; i++) {
			if(solutions[i].equals(s)) {
				return i+1;
			}
		}
		return solutions.length+1;
	}
	
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
