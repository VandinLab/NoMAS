package NoMAS;
import java.util.*;

public class Bitstring {
	public static final int BITS = 31;
	/**
	 * Creates a new list of bitsets, by performing a 
	 * logical OR between the two given ones.
	 */
	public static int[] logicalOR(int[] A, int[] B) {
		int[] C = new int[A.length];
		for(int i=0; i<C.length; i++) {
			C[i] = A[i] | B[i];
		}
		return C;
	}
	
	/**
	 * Returns the number of set bits in the given bitsets.
	 * Uses Brian Kernighan's bits counting algorithm.
	 */
	public static int numberOfSetBits(int[] X) {
		int m1 = 0;
		for(int n : X) {
			for(; n != 0; m1++) {
				n &= n - 1;
			}
		}
		return m1;
	}
	
	/**
	 * Static look-up table used by the dotProductWithArray-routine
	 */
	private static final int[] Mod37BitPosition = {
		32, 0, 1, 26, 2, 23, 27, 0, 3, 16, 24, 30, 28, 11, 0, 13, 4, 7, 17, 
		0, 25, 22, 31, 15, 29, 10, 12, 6, 0, 21, 14, 9, 5, 20, 8, 19, 18};

	/**
	 * Computes the dot product between the given bitstring
	 * and the given array of real values.
	 */
	public static final double dotProductWithArray(int[] X, double[] w) {
		double dp = 0.0;
		for(int i=0; i<X.length; i++) {
			int x = X[i];
			int index = i*31;
			while(x != 0) {
				int pos = Mod37BitPosition[(x&-x)%37]; // Isolate rightmost and look up
				x = x & (x-1); // Clear rightmost
				dp += w[index+pos];
			}
		}
		return dp;
	}
	
	public static final void logrankAndCountCrossval(Solution solution, double[] w) {
		solution.m1cv = 0;
		int[] X = solution.xcv;
		double dp = 0.0;
		for(int i=0; i<X.length; i++) {
			int x = X[i];
			int index = i*31;
			while(x != 0) {
				int pos = Mod37BitPosition[(x&-x)%37]; // Isolate rightmost and look up
				x = x & (x-1); // Clear rightmost
				dp += w[index+pos];
				solution.m1cv++;
			}
		}
		solution.lrcv = dp;
	}
	
	public static final void logrankAndCount(Solution solution, double[] w) {
		solution.m1 = 0;
		int[] X = solution.x;
		double dp = 0.0;
		for(int i=0; i<X.length; i++) {
			int x = X[i];
			int index = i*31;
			while(x != 0) {
				int pos = Mod37BitPosition[(x&-x)%37]; // Isolate rightmost and look up
				x = x & (x-1); // Clear rightmost
				dp += w[index+pos];
				solution.m1++;
			}
		}
		solution.lr = dp;
	}
	
	/**
	 * Returns a - b if b subset of a, and 0 otherwise
	 */
	public static int setDifference(int a, int b) {
		return ((a&b) != b) ? 0 : a^b;
	}
	
	public static int setBit(int n, int i) {
		return n | (1<<i);
	}
	
	public static int clearBit(int n, int i) {
		return n & ~(1<<i);
	}
	
	public static int getBit(int n, int i) {
		return (n>>i)&1;
	}
	
	public static void setBit(int[] X, int i) {
		X[i/BITS] = setBit(X[i/BITS], i%BITS);
	}
	
	public static void clearBit(int[] X, int i) {
		X[i/BITS] = clearBit(X[i/BITS], i%BITS);
	}
	
	public static int getBit(int[] X, int i) {
		return getBit(X[i/BITS], i%BITS);
	}
	
	public static int[] getEmpty(int length) {
		return new int[(length+BITS-1)/BITS];
	}
	
	public static void clear(int[] x) {
		for(int i=0; i<x.length; i++) {
			x[i] = 0;
		}
	}
	
	public static int[] randomBitstring(Random rng, int length, int k) {
		int[] A = new int[length];
		for(int i=0; i<k; i++) {
			A[i] = 1;
		}
		Utils.shuffle(rng, A);
		int[] x = new int[(length+BITS-1)/BITS];
		for(int i=0; i<length; i++) {
			if(A[i] == 1) {
				setBit(x, i);
			}
		}
		return x;
	}
	
	/**
	 * Enumerates all bitstrings of length k that contains m set bits.
	 */
	public static void enumerate(ArrayList<Integer> A, int S, int k, int m, int pos) {
		if(m == 0) {
			A.add(S);
			return;
		}
		for(int i=pos; i<=k-m; i++) {
			enumerate(A, S | (1 << i), k, m-1, i+1);
		}
	}

	public static String asString(int A, int k) {
		String s = "";
		for(int i=0; i<k; i++) {
			s += getBit(A, k-i-1);
		}
		return s;
	}
	
	public static String asString(int[] A, int k) {
		String s = asString(A[A.length-1], k%BITS);
		for(int i=A.length-2; i>=0; i--) {
			s += asString(A[i], BITS);
		}
		return s;
	}
}