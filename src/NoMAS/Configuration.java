package NoMAS;
import java.util.*;

public class Configuration {
	public boolean timing = false;
	public boolean progress = false;
	public boolean uniqueness = false;
	public boolean crossval = false;
	public int N = 4;
	public int k = 5;
	public int kprime = 3;
	public int colors = 6;
	public int L = 5;
	public int iterations = 32;
	public int seed = 42;
	public int solutions = 10;
	public double time = 0.0;
	public double seed_error = 0.05;
	public ArrayList<Vertex> seeds = null;
	public Objective objective = Model.MAX_NLR;
	public Output output;
	
	public Configuration() {}
	
	public Configuration(String[] tokens) {
		int index = -1;
		if((index = Utils.index(tokens, "k")) != -1) {
			k = Integer.parseInt(tokens[index+1]);
			colors = k+1;
			kprime = (k+1)/2;
		}
		if((index = Utils.index(tokens, "d")) != -1) {
			colors = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "kprime")) != -1) {
			kprime = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "seed")) != -1) {
			seed = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "solutions")) != -1) {
			solutions = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "iterations")) != -1) {
			iterations = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "N")) != -1) {
			N = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "L")) != -1) {
			L = Integer.parseInt(tokens[index+1]);
		}
		if((index = Utils.index(tokens, "error")) != -1) {
			double error = Double.parseDouble(tokens[index+1]);
			iterations = Statistics.iterations(error, k, colors);
            if(iterations < 1) {
                iterations = 1;
            }
		}
        if((index = Utils.index(tokens, "seederror")) != -1) {
			seed_error = Double.parseDouble(tokens[index+1]);
		}
	}
}
