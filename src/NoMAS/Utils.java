package NoMAS;
import java.util.*;
import java.math.*;
import java.util.concurrent.*;
import java.io.*;

public class Utils {
	public static double round(double value, int places) {
		BigDecimal bd = new BigDecimal(value);
		return bd.setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
	
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
	
	public static int pow10(int exp) {
		return (int)Math.pow(10, exp);
	}

    /**
	 * Returns an array of length n, in which entry i has value i
	 */
	public static int[] indexArray(int n) {
		int[] A = new int[n];
		for(int i=0; i<n; i++) {
			A[i] = i;
		}
		return A;
	}
	
	public static void shuffle(Random rng, int[] A) {
		int n = A.length;
		for(int i=0; i<n-1; i++) {
			int j = rng.nextInt(n-i);
			int tmp = A[n-i-1];
			A[n-i-1] = A[j];
			A[j] = tmp;
		}
	}

	public static void shuffle(int[] A) {
		Random rng = new Random();
		shuffle(rng, A);
	}
	
    /**
	 * Synchronizes a number of threads as specified by the barrier
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
	 * Waits on the given list of threads to join
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
	 * Get the number of jobs from the total, to distribute to processor i out of N processors
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
	
	public static BufferedReader bufferedReader(String filename) {
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(filename));
		}catch(FileNotFoundException e) {
			System.err.println("Failed to read file: "+filename);
		}
		return reader;
	}
	
	public static String readLine(BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
		}catch(IOException e) {
			System.err.println("Failed to read line from file");
		}
		return line;
	}
	
	public static void close(BufferedReader reader) {
		try {
			reader.close();
		}catch(IOException e) {
			System.err.println("Failed to close file");
		}
	}
	
	public static long getTime() {
		return System.nanoTime();
	}
	
	public static double timeElapsed(long start) {
		long elapsed = System.nanoTime() - start;
		return round((double)elapsed/1000000000.0, 4);
	}
	
	public static int index(String[] tokens, String token) {
		for(int i=0; i<tokens.length; i++) {
			if(tokens[i].equals(token)) {
				return i;
			}
		}
		return -1;
	}
}