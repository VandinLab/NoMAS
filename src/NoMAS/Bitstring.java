package NoMAS;
import java.util.*;

/**
 * Class of utilities for bitwise based operations. Information are passed to method as sequences of bits represented as integers (the bitstrings)
 * and matrixes as arrays of integers (the bitsets).
 * All methods are static.
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Bitstring {
	/**
	 * the number of significant bits in integer variables (32 - 1 bit for sign)
	 */
	public static final int BITS = 31;
	
	/**
	 * Static look-up table used by the dotProductWithArray-routine
	 */
	private static final int[] Mod37BitPosition = {
		32, 0, 1, 26, 2, 23, 27, 0, 3, 16, 24, 30, 28, 11, 0, 13, 4, 7, 17, 
		0, 25, 22, 31, 15, 29, 10, 12, 6, 0, 21, 14, 9, 5, 20, 8, 19, 18};

	
	/**
	 * Creates a new list of bitsets, by performing a 
	 * logical OR between the two given ones.
	 * 
	 * @param A an operand
	 * @param B an operand
	 * @return the result of the operation
	 */
	public static int[] logicalOR(int[] A, int[] B) {
		int[] C = new int[A.length];
		for(int i=0; i<C.length; i++) {
			C[i] = A[i] | B[i];
		}
		return C;
	}
	
	/**
	 * Returns the number of bits set to 1 in the given bitsets.
	 * It uses Brian Kernighan's bits counting algorithm.
	 * 
	 * @param X the bitset to analize
	 * @return the number of set bits in the given bitset
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
	 * Computes the dot product between the given bitstring
	 * and the given array of real values.
	 * 
	 * @param X the bitstring in form of array
	 * @param w the array of real values
	 * @return the product
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
	
	/**
	 * Computes the logrank of the passed instance of Solution based on the passed weights array when splitting input data for cross validation.
	 * This method writes the output into the variables dedicated to crossval strategy, but does not perform any split of input data.
	 * 
	 * @param solution instance of {@link Solution} whose logrank has to be computed
	 * @param w the weights array
	 */
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
	
	/**
	 * Computes the logrank of the passed instance of Solution based on the passed weights array.
	 * 
	 * @param solution instance of {@link Solution} whose logrank has to be computed
	 * @param w the weights array
	 */
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
	 * Returns bitwise a - b if b subset of a, and 0 otherwise.
	 * 
	 * @param a operand a
	 * @param b operand b
	 * @return the result of the operation
	 */
	public static int setDifference(int a, int b) {
		return ((a&b) != b) ? 0 : a^b;
	}
	
	/**
	 * Sets the i-th bit of a bitstring to 1.
	 * 
	 * @param n the bitstring
	 * @param i the position to set to 1 (first index is 0)
	 * @return the bitstring with the desired transformation
	 */
	public static int setBit(int n, int i) {
		return n | (1<<i);
	}
	
	/**
	 * Sets the i-th bit of a bitstring to 0. 
	 * 
	 * @param n the bitstring
	 * @param i the position to set to 0 (first index is 0)
	 * @return the bitstring with the desired transformation
	 */
	public static int clearBit(int n, int i) {
		return n & ~(1<<i);
	}
	
	/**
	 * returns the i-th bit of a bitstring 
	 * 
	 * @param n the bitstring
	 * @param i the position to retrieve (first index is 0)
	 * @return the desired bit
	 */
	public static int getBit(int n, int i) {
		return (n>>i)&1;
	}
	
	/**
	 * Sets the i-th bit of a bitstring to 1. The array is considered as an unique long sequence of bits obtained through the bitwise concatenation of its elements.
	 * 
	 * @param X the array of integers
	 * @param i the position to set to 1 (first index is 0)
	 */
	public static void setBit(int[] X, int i) {
		X[i/BITS] = setBit(X[i/BITS], i%BITS);
	}
	
	/**
	 * Sets the i-th bit of a bitstring to 0. The array is considered as an unique long sequence of bits obtained through the bitwise concatenation of its elements.
	 * 
	 * @param X the array of integers
	 * @param i the position to set to 0 (first index is 0)
	 */
	public static void clearBit(int[] X, int i) {
		X[i/BITS] = clearBit(X[i/BITS], i%BITS);
	}
	
	/**
	 * Gets the i-th bit of a bitstring. The array is considered as an unique long sequence of bits obtained through the bitwise concatenation of its elements.
	 * 
	 * @param X the array of integers
	 * @param i the position to retrieve (first index is 0)
	 * @return the desired bit
	 */
	public static int getBit(int[] X, int i) {
		return getBit(X[i/BITS], i%BITS);
	}
	
	/**
	 * Creates a new bitset. The size of the obtained sequence is equal to the closest multiple of 31 greater than the length passed as parameter
	 * All bits are set to 0 
	 * 
	 * @param length lower bound of the sequence length
	 * @return the bitset
	 */
	public static int[] getEmpty(int length) {
		return new int[(length+BITS-1)/BITS];
	}
	
	/**
	 * Sets all the bits of a bitset to 0
	 * 
	 * @param x the bitset to clear
	 */
	public static void clear(int[] x) {
		for(int i=0; i<x.length; i++) {
			x[i] = 0;
		}
	}
	
	/**
	 * Creates a new bitset. The size of the obtained sequence is equal to the closest multiple of 31 greater than the length passed as parameter
	 * All bits are set to a random value, with only k bits set to 1
	 * 
	 * @param rng the {@link Random} instance that performs randomization
	 * @param length lower bound of the sequence length
	 * @param k the number of bits to set to 1
	 * @return the created bitset
	 */
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
	 * Enumerates all bitstrings of length k that contains m set bits in a sequence and stores the result in a collection 
	 * 
	 * @param A the collection where the result is stored
	 * @param S parameter for recursion. Invoke using 0
	 * @param k length of the researched sequences
	 * @param m number of bits set to 1 in each sequence
	 * @param pos parameter for recursion. Invoke using 0
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

	/**
	 * Returns a String with the first k bits of A
	 * 
	 * @param A the bitset
	 * @param k number of bits to print
	 * @return the resulting instance of {@link String}
	 */
	public static String asString(int A, int k) {
		String s = "";
		for(int i=0; i<k; i++) {
			s += getBit(A, k-i-1);
		}
		return s;
	}
	
	/**
	 * Returns a String with the first k columns of a bitset
	 * 
	 * @param A the bitset
	 * @param k number of bits to print for each integer
	 * @return the resulting instance of {@link String}
	 */
	public static String asString(int[] A, int k) {
		String s = asString(A[A.length-1], k%BITS);
		for(int i=A.length-2; i>=0; i--) {
			s += asString(A[i], BITS);
		}
		return s;
	}
}