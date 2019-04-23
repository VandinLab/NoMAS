package NoMAS;

/**
 * Basic representation of a single gene's information retrieved from the input data
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Gene {
	/**
	 * {@link String} with the name of the gene.
	 */
	public String symbol;
	/**
	 * bitset (see {@link Bitstring}) representing the distribution of the mutations in the gene represented by this instance in the patients' data.
	 */
	public int[] x;
	/**
	 * number of patients with a mutation in the gene represented by this instance. 
	 */
	public int m1;
}
