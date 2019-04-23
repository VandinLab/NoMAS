package NoMAS;
import java.util.*;

/**
 * Test class
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Experiments {
    public static Model getThesisModel(String graph, String matrix) {
		Model model = new Model();
		Graph.loadGraph(graph, model);
		Mutations.loadMutationMatrix(matrix, model);
		Mutations.removeMutations(model, 3);
		Graph.reduce(model, Model.HASMUTATIONS | Model.INTERNAL);
		return model;
	}
    
	public static void Thesis_Exhaustive(String graph, String matrix) {
		Model model = getThesisModel(graph, matrix);
		
		Configuration config = new Configuration();
		config.N = 50;		

		Output out = new Output("Thesis_Exhaustive/"+matrix+"/results_k6.txt", false);
		for(int k=6; k<=6; k++) {
			config.k = k;
			ExhaustiveEnumerator en = new ExhaustiveEnumerator(model, config);
			Solution[] solutions = en.run();
			Output.solutions(out.stream, model, solutions);
			out.stream.println("time\t"+en.timeElapsed());
			out.stream.println("\n");
			out.stream.flush();
		}
		out.stream.close();
	}
	
	public static void Thesis_MoreColors(String graph, String matrix) {
		Configuration config = new Configuration();
		config.N = 50;
		config.objective = Model.MAX_NLR;
		
		Model model = getThesisModel(graph, matrix);
		
		Output out = new Output("Thesis_MoreColors/"+matrix+"/k5_k10.txt", false);
		Output meta = new Output("Thesis_MoreColors/"+matrix+"/k5_k10_meta.txt", false);
		
		for(int k=5; k<=10; k++) {
			config.k = k;
			config.kprime = (k+1)/2;
			for(int i=k; i<=15; i++) {
				config.colors = i;
				config.iterations = Statistics.iterations(0.1, config.k, config.colors);
			
				Algorithm snomas = new SNoMAS3(model, config);
				Solution[] solutions = snomas.run();
				Output.solutions(out.stream, model, solutions);
				out.stream.println("\n");
                out.stream.flush();
				meta.stream.println("0.1\t"+k+"\t"+i+"\t"+config.iterations+"\t"+snomas.timeElapsed());
				meta.stream.flush();
			}
		}
		
		meta.stream.close();
		out.stream.close();
	}
	
	public static void Thesis_PermutationTest(String graph, String matrix, Objective objective) {
		Configuration config = new Configuration();
		config.N = 50;
		config.k = 6;
		config.colors = 7;
		config.kprime = 7/2;
		config.iterations = Statistics.iterations(0.05, config.k, config.colors);
		config.objective = objective;
		
		Model model = getThesisModel(graph, matrix);
		
		Algorithm[] algorithms = {
			new NoMAS(model, config),
			new SNoMAS3(model, config),
			new Greedy1(model, config),
			new GreedyK(model, config),
			new GreedyDFS(model, config)
		};	
	
        //DataPermuter permuter = new SwitchPermuter();
        DataPermuter permuter = new IdentityPermuter();
        permuter.initialize(model, 42);
        
		Output out = new Output("Thesis_PermutationTest/"+matrix+"/identity_"+objective.getName()+"k"+config.k+"_matrix.txt", false);
		Output results = new Output("Thesis_PermutationTest/"+matrix+"/identity_"+objective.getName()+"k"+config.k+"_results.txt", false);
		
		double[][][] table = Statistics.permutationTest(model, permuter, 100, objective, results, algorithms);
		results.stream.close();
		
		for(int s=0; s<10; s++) {
			for(int i=0; i<algorithms.length; i++) {
				for(int j=0; j<algorithms.length; j++) {	
					out.stream.print(table[i][j][s]+"\t");	
				}
				out.stream.print("\n");
			}
			out.stream.print("\n");
		}
		out.stream.flush();
		out.stream.close();
	}
	
	public static void Thesis_AlgorithmTest(String graph, String matrix, Objective objective) {
		Model model = getThesisModel(graph, matrix);
		
		Configuration config = new Configuration();
		config.N = 50;
		config.objective = objective;
		
		Algorithm[] algorithms = {
			new NoMAS(model, config),
			new SNoMAS1(model, config),
			new SNoMAS2(model, config),
            new SNoMAS3(model, config),
			new FatNoMAS(model, config),
			new NoMAS2(model, config),
            new Greedy1(model, config),
            new GreedyK(model, config),
            new GreedyDFS(model, config)
		};
		
		for(int k=5; k<=8; k++) {
			for(Algorithm alg : algorithms) {
				config.k = k;
				config.colors = config.k;
				config.iterations = Statistics.iterations(0.05, config.k, config.colors);
				config.kprime = (k+1)/2;
				Output out = new Output("Thesis_AlgorithmTest/"+alg.getClass().getName()+"/"+matrix+"/"+objective.getName()+"k"+k+".txt", false);
					
				Solution[] solutions = alg.run();
				Output.solutions(out.stream, model, solutions);
				out.stream.flush();
				out.stream.close();
			}
		}
	}
	
	public static void Thesis_KprimeTest(String graph, String matrix, Objective objective) {
		Model model = getThesisModel(graph, matrix);
		
		Configuration config = new Configuration();
		config.N = 50;
		config.objective = objective;
		
		Algorithm algorithm = new SNoMAS3(model, config);
		for(int k=5; k<=8; k++) {
			config.k = k;
			config.colors = k+1;
			config.iterations = Statistics.iterations(0.05, config.k, config.colors);
			for(int kp=1; kp<k; kp++) {
				config.kprime = kp;
				Solution[] solutions = algorithm.run();
				Output out = new Output("Thesis_KprimeTest/"+matrix+"/"+objective.getName()+"/k"+k+"_kp"+kp+".txt", false);
				Output.solutions(out.stream, model, solutions);
				out.stream.close();
			}
		}
	}
	
	public static void Thesis_OverlapTest(String graph, String matrix, Objective objective) {
		Model model = getThesisModel(graph, matrix);
		
		Configuration config = new Configuration();
		config.N = 50;
		config.progress = true;
		config.objective = objective;
		config.solutions = 50;
		config.uniqueness = true;
		
		int mink = 2;
		int maxk = 8;
		
		Algorithm algorithm = new NoMAS(model, config);
		Solution[][] solution_lists = new Solution[maxk-mink+1][config.solutions];
		
		Output results = new Output("Thesis_OverlapTest/"+matrix+"/"+objective.getName()+"k"+mink+"_k"+maxk+"_results.txt", false);
		
		for(int k=mink; k<=maxk; k++) {
			config.k = k;
			config.colors = k+1;
			config.iterations = Statistics.iterations(0.05, config.k, config.colors);
			solution_lists[k-mink] = algorithm.run();
			Output.solutions(results.stream, model, solution_lists[k-mink]);
			results.stream.print("\n");
			results.stream.flush();
		}
		
		results.stream.close();
		
		int[] limits = {1, 10, 20, 30, 40, 50};
		for(int limit : limits) {
			int[][] table =  Statistics.overlapMatrix(limit, solution_lists, solution_lists);
			Output out = new Output("Thesis_OverlapTest/"+matrix+"/"+objective.getName()+"k"+mink+"_k"+maxk+"_l"+limit+".txt", false);
			for(int i=0; i<table.length; i++) {
				for(int j=0; j<table[i].length; j++) {
					out.stream.print(table[i][j]+"\t");
				}
				out.stream.print("\n");
			}
			out.stream.close();
		}
	}
    
	public static void Thesis_AdditiveScoreTest(String graph, String matrix, Objective objective) {
		Model model = getThesisModel(graph, matrix);

		Configuration config = new Configuration();
		config.N = 50;
		config.progress = true;
		config.objective = objective;
        
		Algorithm alg = new NoMASAdditive(model, config);
		
		for(int k=5; k<=9; k++) {
			config.k = k;
			config.colors = config.k+1;
			config.iterations = Statistics.iterations(0.05, config.k, config.colors);
			Output out = new Output("Thesis_AdditiveScoreTest/"+matrix+"/"+objective.getName()+"_k"+k+".txt", false);
            Solution[] solutions = alg.run();
            SolutionList.computeLogrank(model, solutions);
			Output.solutions(out.stream, model, solutions);
			out.stream.close();
		}
	}
	
	public static void Thesis_RunningTimeTest(String graph, String matrix) {
		Model model = getThesisModel(graph, matrix);
		
		Configuration config = new Configuration();
		config.iterations = 10;
		
		Algorithm[] algorithms = {
			new NoMAS(model, config),
			new FatNoMAS(model, config),
			new NoMAS2(model, config),
			new SNoMAS1(model, config),
			new SNoMAS2(model, config),
			new SNoMAS3(model, config),
			new Greedy1(model, config),
			new GreedyK(model, config),
			new GreedyDFS(model, config)
		};
		
		for(Algorithm alg : algorithms) {
			String algname = alg.getClass().getName();
			Output out = new Output("Thesis_RunningTimeTest/"+algname+"/"+matrix+"/"+config.objective.getName()+".txt", true);
			for(int k=5; k<=8; k++) {
				config.k = k;
				config.colors = k;
				config.kprime = (k+1)/2;
				for(int N=0; N<=6; N++) {
					config.N = (int)Math.pow(2,N);
					alg.run();
					out.stream.println((alg.timeElapsed()/10.0)+"\t"+k+"\t"+N);
					out.stream.flush();
				}
			}
			out.stream.close();
		}
	}
	
	public static void Thesis_SNoMASRunningTime(String graph, String matrix) {
		Model model = getThesisModel(graph, matrix);
		
		Configuration config = new Configuration();
		config.iterations = 10;
		config.N = 50;
		config.k = 8;
		config.colors = 8;
		
		
		Algorithm[] algorithms = {
			new SNoMAS3(model, config),
			new SNoMAS2(model, config),
			new SNoMAS1(model, config)
		};
		
		for(Algorithm alg : algorithms) {
			Output out = new Output("Thesis_SNoMASRunningTime/"+alg.getClass().getName()+"/"+matrix+"/times.txt", false);
			Output results = new Output("Thesis_SNoMASRunningTime/"+alg.getClass().getName()+"/"+matrix+"/results.txt", false);
			for(int kp=1; kp<config.k; kp++) {
				config.kprime = kp;
				Solution[] solutions = alg.run();
				out.stream.println(alg.timeElapsed()+"\t"+kp);
				out.stream.flush();
				Output.solutions(results.stream, model, solutions);
				results.stream.println("\n");
				results.stream.flush();
			}
			results.stream.close();
			out.stream.close();
		}
	}
}