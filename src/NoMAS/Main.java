package NoMAS;
public class Main {
	public static void main(String[] args) {
		int index = -1;
        String import_file = null;
        
        boolean cValMode = false;
        
        if((index = Utils.index(args, "import")) != -1) {
			import_file = args[index+1];
        }
        
        if((index = Utils.index(args, "crossval")) != -1) {
			cValMode = true;
        }
        
     // CONSTRUCT MODEL
        if (!cValMode) {
	 		Model model = null;
	         if(import_file != null) {
	             // Load models from file
	             model = Model.fromSolutionsFile(import_file);
	         }else {
	        	 if((index = Utils.index(args, "output")) != -1) {
	        		 model = new Model(args[index+1]);
		            }
	        	 else {
	        		 model = new Model();
	        	 	}
	             // Load network file
	             String graph_file = "networks/hint+hi2012.txt";
	             if((index = Utils.index(args, "network")) != -1) {
	                 graph_file = args[index+1];
	             }
	             Graph.loadGraph(graph_file, model);
	             // Load data file
	             String dataset_file = "datasets/ov.txt";
	             if((index = Utils.index(args, "data")) != -1) {
	                 dataset_file = args[index+1];
	             }
	             Mutations.loadMutationMatrix(dataset_file, model);
	             // Remove mutations
	             double threshold = 3.0;
	             if((index = Utils.index(args, "threshold")) != -1) {
	                 threshold = Double.parseDouble(args[index+1]);
	             }
	             Mutations.removeMutations(model, threshold);
	 			// Remove mutations in genes to be ignored
	 			if((index = Utils.index(args, "ignore")) != -1) {
	 				Mutations.removeMutationsInGenes(model, args[index+1]);
	 			}
	             // Reduce graph
	             int flags = Model.HASMUTATIONS | Model.INTERNAL;
	             if((index = Utils.index(args, "retain")) != -1) {
	                 flags = Integer.parseInt(args[index+1]);
	             }
	             Graph.reduce(model, flags);
	         }
	 		
	 		// CONSTRUCT CONFIGURATION
	 		Configuration config = new Configuration(args);	
	 		if((index = Utils.index(args, "func")) != -1) {
	 			config.objective = Model.objectiveFromName(args[index+1]);
	 		}
	 		
	 		// CONTRUCT ALGORITHM
	 		Algorithm algorithm = null;
	 		if((index = Utils.index(args, "algorithm")) != -1) {
	 			String alg_name = args[index+1];
	 			if(alg_name.equals("NoMAS")) {
	 				algorithm = new NoMAS(model, config);
	 			}else if(alg_name.equals("FatTable")) {
	 				algorithm = new FatNoMAS(model, config);
	 			}else if(alg_name.equals("Neighborhood")) {
	 				algorithm = new NoMAS2(model, config);
	 			}else if(alg_name.equals("SNoMAS0")) {
	 				algorithm = new SNoMAS1(model, config);
	 			}else if(alg_name.equals("SNoMAS1")) {
	 				algorithm = new SNoMAS2(model, config);
	 			}else if(alg_name.equals("SNoMAS2")) {
	 				algorithm = new SNoMAS3(model, config);
	 			}else if(alg_name.equals("Greedy1")) {
	 				algorithm = new Greedy1(model, config);
	 			}else if(alg_name.equals("GreedyK")) {
	 				algorithm = new GreedyK(model, config);
	 			}else if(alg_name.equals("GreedyDFS")) {
	 				algorithm = new GreedyDFS(model, config);
	 			}else if(alg_name.equals("Additive")) {
	 				algorithm = new NoMASAdditive(model, config);
	 			}else if(alg_name.equals("Exhaustive")) {
	                 algorithm = new ExhaustiveEnumerator(model, config);
	             }
	             else {
	                 System.err.println("No such algorithm: "+alg_name);
	                 System.exit(1);
	             }
	 		}else {
	 			algorithm = new NoMAS(model, config);
	 		}
	 		
	 		// LOAD SEED VERTICES FROM FILE
	 		if((index = Utils.index(args, "seeds")) != -1) {
	 			String seeds_file = args[index+1];
	 			config.seeds = AbstractSNoMAS.loadSeedsFromFile(model, seeds_file);
	 		}
	 		
	 		// FIND SOLUTIONS
	         Solution[] solutions = null;
	         if(import_file != null) {
	             solutions = SolutionList.fromFile(model, import_file);
	         }else {
	             config.progress = true;
	             System.err.println("Solving Max k-set Log-rank.");
	             solutions = algorithm.run();
	             System.err.println("Done. Time elapsed: "+algorithm.timeElapsed());
	         }
	 		
	 		// If p-value estimate
	 		if((index = Utils.index(args, "pvalue")) != -1) {
	 			config.progress = false;
	 			int samples = Integer.parseInt(args[index+1]);
	 			System.err.println("Estimating p-values, using Monte Carlo method. Samples = "+samples);
	 			Statistics.pvalue(model, samples, config.N, solutions);
	 			System.err.println("Done.");
	 		}
	 		
	 		// Permutation test
	 		if((index = Utils.index(args, "permutations")) != -1) {
	 			int permutations = Integer.parseInt(args[index+1]);
	 			DataPermuter permuter;
	 			if((index = Utils.index(args, "mutmodel")) != -1) {
	 				permuter = DataPermuter.permuterFromName(args[index+1]);
	 			}else {
	 				permuter = new IdentityPermuter();
	 			}
	 			permuter.initialize(model, config.seed);
	 			config.progress = false;
	 			System.err.println("Running permutation test for "+permutations+" permutations.");
	 			Statistics.permutationTest(solutions, algorithm, permuter, permutations, config.objective);
	 			System.err.println("Done.");
	 		}
	 		
	         // Output
	         String outname = null;
	         if(import_file != null) {
	             outname = import_file.substring(0, import_file.length()-4);
	         }else {
	             outname = "./solution";
	             if((index = Utils.index(args, "output")) != -1) {
	                 outname = args[index+1];
	             }
	         }
	         Output out = new Output(outname+".txt", false);
	 		 Output.solutions(out.stream, model, solutions);
	         out.stream.flush();
	         out.stream.close();
	         Graphic.render(outname, model, solutions);
	         
	         // Output mutation/survival information
	 		if((index = Utils.index(args, "mutinfo")) != -1) {
	 			int i = Integer.parseInt(args[index+1]);
	             out = new Output(outname+"_mutinfo.txt", false);
	             Mutations.writeMutationInfo(model, solutions[i], out);
	             out.stream.flush();
	             out.stream.close();
	 		}
        }
        else {
			
			// the train set to execute NoMas
			Model train = null;
			
			// the control set to perform validation
			Model control = null;
	        if(import_file != null) {
	        	
	        System.out.println("TO BE IMPLEMENTED");
	        	
	        }else {
	        	if((index = Utils.index(args, "output")) != -1) {
	        		train = new Model(args[index+1]);
	        		control = new Model(args[index+1]);
		            }
	        	 else {
	        		train = new Model();
	 	        	control = new Model();
	        	 	}	        		        	
	            // Load network file
	            String graph_file = "networks/hint+hi2012.txt";
	            if((index = Utils.index(args, "network")) != -1) {
	                graph_file = args[index+1];
	            }
	            Graph.loadGraph(graph_file, train);
	            Graph.loadGraph(graph_file, control);
	            
	            // Load data file
	            String dataset_file = "datasets/demoDataset.txt";
	            if((index = Utils.index(args, "data")) != -1) {
	                dataset_file = args[index+1];
	            }
	            
	            boolean timesplits = false; //choose the kind of splitting, if based on time distribution or not 
	            
	            if((index = Utils.index(args, "timeSplits")) != -1) {
	            	timesplits = true;
	            }else{
	            	timesplits = false;
	            }	            
	            
	            int splits = 10; //default value
	            if((index = Utils.index(args, "splits")) != -1) {
	            	splits = Integer.parseInt(args[index+1]);
	            }
	            
	            double proportion = 0.5; //default value
	            if((index = Utils.index(args, "proportion")) != -1) {
	            	proportion = Double.parseDouble(args[index+1]);
	            }
	            Mutations.loadMutationMatrixes(dataset_file, train, control, timesplits, splits, proportion);
	            	            
	            // Remove mutations
	            double threshold = 3.0;
	            if((index = Utils.index(args, "threshold")) != -1) {
	                threshold = Double.parseDouble(args[index+1]);
	            }
	            Mutations.removeMutations(train, threshold);
	            //Mutations.removeMutations(control, threshold);
				// Remove mutations in genes to be ignored
				if((index = Utils.index(args, "ignore")) != -1) {
					Mutations.removeMutationsInGenes(train, args[index+1]);
					Mutations.removeMutationsInGenes(control, args[index+1]);
				}
	            // Reduce graph
	            int flags = Model.HASMUTATIONS | Model.INTERNAL;
	            if((index = Utils.index(args, "retain")) != -1) {
	                flags = Integer.parseInt(args[index+1]);
	            }
	            Graph.reduce(train, flags);
	            Graph.reduce(control, flags);
	        }        
			
			// CONSTRUCT CONFIGURATION
			Configuration config = new Configuration(args);	
			if((index = Utils.index(args, "func")) != -1) {
				config.objective = Model.objectiveFromName(args[index+1]);
			}
			
			// CONTRUCT ALGORITHM
			Algorithm algorithm = null;
			algorithm = new NoMAS(train, config);
			
			// LOAD SEED VERTICES FROM FILE
			if((index = Utils.index(args, "seeds")) != -1) {
				String seeds_file = args[index+1];
				config.seeds = AbstractSNoMAS.loadSeedsFromFile(train, seeds_file);
			}
			
			// FIND SOLUTIONS
	        Solution[] solutions = null;
	        if(import_file != null) {
	            solutions = SolutionList.fromFile(train, import_file);
	        }else {
	            config.progress = true;
	            System.err.println("Solving Max k-set Log-rank.");
	            solutions = algorithm.run();
	            System.err.println("Done. Time elapsed: "+algorithm.timeElapsed());
	        }
			
			//p-value estimate
	
			config.progress = false;
			int samples = 10000; //default
			if((index = Utils.index(args, "pvalue")) != -1) {
				samples = Integer.parseInt(args[index+1]);
			}
			System.err.println("Estimating p-values, using Monte Carlo method. Samples = "+samples);
			Statistics.pvalue(control, samples, config.N, solutions);
			System.err.println("Done.");
	
	        // Output
	        String outname = null;
	        if(import_file != null) {
	            outname = import_file.substring(0, import_file.length()-4);
	        }else {
	            outname = "./solution";
	            if((index = Utils.index(args, "output")) != -1) {
	                outname = args[index+1];
	            }
	        }
	        Output out = new Output(outname+".txt", false);
			Output.solutions(out.stream, train, solutions);
	        out.stream.flush();
	        out.stream.close();
	        Graphic.render(outname, train, solutions);
	        
	        // Output mutation/survival information
			if((index = Utils.index(args, "mutinfo")) != -1) {
				int i = Integer.parseInt(args[index+1]);
	            out = new Output(outname+"_mutinfo.txt", false);
	            Mutations.writeMutationInfo(train, solutions[i], out);
	            out.stream.flush();
	            out.stream.close();
			}
        }
		
	}
}
