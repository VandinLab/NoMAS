package NoMAS;
import java.io.*;

/**
 * Class that manages the output to a text file and incapsulates the file handling.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Output {
	/**
	 * {@link Output} instance that handes log file.
	 */
	public static Output log;
	/**
	 * {@link PrintStream} instance that handles file.
	 */
	public PrintStream stream;
	
	static {
		log = new Output("log.txt", true);
	}
	
	/**
	 * Constructor that initializes the file handler.
	 * 
	 * @param filename Path to file.
	 * @param append If true, it does not create a new file, but tries to append the new text to an existing file.
	 */
	public Output(String filename, boolean append) {
		filename = verifyPath(filename);
		stream = printStream(filename, append);
	}
	
	/**
	 * Validates a relative path from the current position. If possible, the method creates folders and files to present a file structure coherent with the provided path. 
	 * 
	 * @param filename The relative path to the file to create 
	 * @return The effective relative path to the created file
	 */
	public static String verifyPath(String filename) {
		String[] levels = filename.split("/");
		String path = "";
		for(int i=0; i<levels.length-1; i++) {
			String folder = levels[i];
			if(folder.endsWith(".txt")) {
				folder = folder.substring(0, folder.length()-4);
			}
			path += folder+"/";
			File file = new File(path);
			if(!file.exists()) {
				file.mkdir();
			}
		}
		path += levels[levels.length-1];
		return path;
	}
	
	/**
	 * Instantiates a new {@link PrintStream} object that handles the file whose path is passed as parameter.
	 * 
	 * @param filename The path to the file to handle.
	 * @param append If true, it does not create a new file, but tries to append the new text to an existing file.
	 * @return The {@link PrintStream} instance that handles the file.
	 */
	public static PrintStream printStream(String filename, boolean append) {
		PrintStream stream = null;
		try {
			stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename, append)));
		}catch(Exception e) {
			System.err.println("Failed to open file for writing: "+filename);
		}
		return stream;
	}
	
	/**
	 * Prints a summary of {@link Solution} instances to a file. 
	 * 
	 * @param stream The {@link PrintStream} instance that handles the output file.
	 * @param model Instance of {@link Model} containing input data.
	 * @param solutions Collection of {@link Solution} instances to print.
	 */
	public static void solutions(PrintStream stream, Model model, Solution... solutions) {
		stream.println(Solution.HEADER);
		for(Solution solution : solutions) {
			if(solution != null) {
				stream.println(solution.asString(model));
			}
		}
		stream.println("");
		stream.println("Graph file\t"+model.graph_file);
		stream.println("Mutation matrix file\t"+model.matrix_file);
		stream.println("Mutation removal threshold\t"+model.mutation_threshold);
		stream.println("Reduction conditions\t"+model.reduction_conditions);
	}
	
	/**
	 * Prints a summary of {@link Solution} instances to a file when using the holdout approach for statistical validation.
	 * 
	 * @param stream The {@link PrintStream} instance that handles the output file.
	 * @param train Instance of {@link Model} containing input data.
	 * @param control Instance of {@link Model} containing input data.
	 * @param solutions Collection of {@link Solution} instances to print.
	 */
	public static void solutionsCrossVal(PrintStream stream, Model train, Model control, Solution... solutions) {
		stream.println(Solution.HEADER);
		for(Solution solution : solutions) {
			if(solution != null) {
				stream.println(solution.asString(train));
			}
		}
		stream.println("");
		stream.println("Graph file\t"+train.graph_file);
		stream.println("Mutation matrix file\t"+train.matrix_file);
		stream.println("Mutation removal threshold\t"+train.mutation_threshold);
		stream.println("Reduction conditions\t"+train.reduction_conditions);
	}
	
	/**
	 * Prints a summary of algorithm parameters and system configuration.
	 * 
	 * @param stream The {@link PrintStream} instance that handles the output file.
	 * @param config Instance of {@link Configuration} containing algorithm parameters and system configuration.
	 */
	public static void configuration(PrintStream stream, Configuration config) {
		stream.println("N\t"+config.N);
		stream.println("k\t"+config.k);
		stream.println("kprime\t"+config.kprime);
		stream.println("colors\t"+config.colors);
		if(config.timing) {
			stream.println("time\t"+config.time);
		}else {
			stream.println("iterations\t"+config.iterations);
		}
		stream.println("objective\t"+config.objective.getName());
	}
}