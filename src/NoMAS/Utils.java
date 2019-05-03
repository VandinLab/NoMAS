package NoMAS;
import java.util.*;
import java.math.*;
import java.util.concurrent.*;

import java.io.*;

/**
 * Container of utilities in form of static methods that perform various operations.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Utils {
	/**
	 * Rounds (in excess) up to a certain decimal.
	 * 
	 * @param value The value to round.
	 * @param places The number of the decimal up to round.
	 * @return The rounded value.
	 */
	public static double round(double value, int places) {
		BigDecimal bd = new BigDecimal(value);
		return bd.setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
	
	/**
	 * Computes the number of all possible choices (without replacement) of r distinct objects from a set of n distinct objects. 
	 * 
	 * @param n Total number of distinct objects.
	 * @param r Objects to pick.
	 * @return The desired value.
	 */
	public static int choose(int n, int r) {
		long num = n;
		long den = 1;
		for(int i=n-1; i>=n-r+1; i--) {
			num *= i;
		}
		for(int i=2; i<=r; i++) {
			den *= i;
		}
		return (int)(num/den);
	}
	
	/**
	 * Executes 10^(exp).
	 * 
	 * @param exp The exponent.
	 * @return The computed value.
	 */
	public static int pow10(int exp) {
		return (int)Math.pow(10, exp);
	}

    /**
	 * Returns an array of length n, in which each entry at index i has value i.
	 *
	 * @param n Length of the array.
	 * @return The array.
	 */
	public static int[] indexArray(int n) {
		int[] A = new int[n];
		for(int i=0; i<n; i++) {
			A[i] = i;
		}
		return A;
	}
	
	/**
	 * Shuffles an array of integers.
	 * 
	 * @param rng {@link Random} instance that performs randomization.
	 * @param A The array to shuffle.
	 */
	public static void shuffle(Random rng, int[] A) {
		int n = A.length;
		for(int i=0; i<n-1; i++) {
			int j = rng.nextInt(n-i);
			int tmp = A[n-i-1];
			A[n-i-1] = A[j];
			A[j] = tmp;
		}
	}

	/**
	 * Shuffles an array of integers.
	 * 
	 * @param A The array to shuffle.
	 */
	public static void shuffle(int[] A) {
		Random rng = new Random();
		shuffle(rng, A);
	}
	
    /**
	 * Synchronizes a number of threads as specified by the barrier.
	 *
	 * @param barrier {@link CyclicBarrier} to use in synchronization.
	 */
	public static void synchronize(CyclicBarrier barrier) {
		try {
			barrier.await();
		}catch(InterruptedException ex) {
			System.err.println("Interruption during thread synchronization.");
			System.exit(1);
		}catch(BrokenBarrierException ex) {
			System.err.println("Broken barrier during thread synchronization.");
			System.exit(1);
		}
	}

     /**
	 * Waits on the given list of {@link Thread} instances to join.
	 *
	 * @param threads The list of {@link Thread} instances to join.
	 */
	public static void join(Thread[] threads) {
		for(int i=0; i<threads.length; i++) {
			try {
				threads[i].join();
			}catch(Exception e) {
				System.err.println("Join failure.");
			}
		}
	}

    /**
	 * Get the number of jobs from the total, to distribute to processor i out of N processors.
	 *
	 * @param N The total number of processors.
	 * @param i The processor to distribute.
	 * @param total Number of jobs to distribute.
	 * @return The desired value.
	 */
	public static int getJobCount(int N, int i, int total) {
		int base = total / N;
		if(i < total % N) {
			return base + 1;
		}else {
			return base;
		}
	}

     /**
	 * Get the first jobs from the total, to distribute to processor i out of N processors
	 *
	 * @param N The total number of processors.
	 * @param i The processor to distribute.
	 * @param total Number of jobs to distribute.
	 * @return The desired value.
	 */
	public static int getBase(int N, int i, int total) {
		int base = i*(total/N);
		int remainder = total % N;
		if(i < remainder) {
			return base + i;
		}else {
			return base + remainder;
		}
	}
	
	/**
	 * Instantiates a {@link BufferedReader} to handle a certain file. Writes an error on the standard error if fails.
	 * 
	 * @param filename Path to the file to handle.
	 * @return {@link BufferedReader} instance that handles the file.
	 */
	public static BufferedReader bufferedReader(String filename) {
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(filename));
		}catch(FileNotFoundException e) {
			System.err.println("Failed to read file: "+filename);
		}
		return reader;
	}
	
	/**
	 * Reads a line from a file handled by an instance of {@link BufferedReader}. Writes an error on the standard error if fails.
	 * 
	 * @param reader {@link BufferedReader} instance that handles the file.
	 * @return The line read.
	 */
	public static String readLine(BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
		}catch(IOException e) {
			System.err.println("Failed to read line from file");
		}
		return line;
	}
	
	/**
	 * Closes a file handled by an instance of {@link BufferedReader}. Writes an error on the standard error if fails.
	 * 
	 * @param reader {@link BufferedReader} instance that handles the file.
	 */
	public static void close(BufferedReader reader) {
		try {
			reader.close();
		}catch(IOException e) {
			System.err.println("Failed to close file");
		}
	}
	
	/**
	 * Gets the nano time of the system.
	 * 
	 * @return The nano time of the system.
	 */
	public static long getTime() {
		return System.nanoTime();
	}
	
	/**
	 * Computes seconds elapsed from a certain instant.
	 * 
	 * @param start Nano time of the starting instant.
	 * @return Elapsed times in seconds.
	 */
	public static double timeElapsed(long start) {
		long elapsed = System.nanoTime() - start;
		return round((double)elapsed/1000000000.0, 4);
	}
	
	/**
	 * Returns, if present, the index where a certain token is placed among an array. Returns -1 if not present.
	 * 
	 * @param tokens The array of tokens to scan.
	 * @param token The token to find.
	 * @return The index of the token in the array if present, -1 elsewhere.
	 */
	public static int index(String[] tokens, String token) {
		for(int i=0; i<tokens.length; i++) {
			if(tokens[i].equals(token)) {
				return i;
			}
		}
		return -1;
	}
}