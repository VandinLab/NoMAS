package NoMAS;
import java.io.*;

public class Output {
	public static Output log;
	public PrintStream stream;
	
	static {
		log = new Output("log.txt", true);
	}
	
	public Output(String filename, boolean append) {
		filename = verifyPath(filename);
		stream = printStream(filename, append);
	}
	
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
	
	public static PrintStream printStream(String filename, boolean append) {
		PrintStream stream = null;
		try {
			stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename, append)));
		}catch(Exception e) {
			System.err.println("Failed to open file for writing: "+filename);
		}
		return stream;
	}
	
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