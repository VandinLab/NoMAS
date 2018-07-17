To compile the program, run the script
./compile.sh

To execute the program, run the script
./nomas.sh

To run demos
./DEMO1.sh
./DEMO2.sh
./DEMO3.sh


SIMPLE EXECUTION:

Example 1:
./nomas.sh data path/to/dataset.txt network path/to/network.txt k 6

The above line runs NoMAS to find the top 10 (default value) subnetworks of size 6 with default error probability 0.05.
The following command line arguments are used: [data, network, k].
Data: File containing the mutation/survival information of the samples
Network: File containing the network
k: Cardinality of the subnetworks

Example 2:
./nomas.sh data path/to/dataset.txt network path/to/network.txt k 8 algorithm SNoMAS2 solutions 20 error 0.01 permutations 100

The above line first runs SNoMAS2 (heuristic 2) with standard settings, to find the top 20 subnetworks of cardinality 6 with custom error probability 0.01.
Then it assesses the statistical significance of the subnetworks by running NoMAS with the same settings on 100 permuted datasets.
The following command line arguments are used: [data, network, k, algorithm, solutions, error, permutations].
Data: File containing the mutation/survival information of the samples
Network: File containing the network
k: Cardinality of the subnetworks
algorithm: Algorithm to use
solutions: Number of solutions to report
error: Error probability of NoMAS
permutations: Number of permutations to use when estimating the permutation p-value


About the SNoMAS heuristics:
SNoMAS(0,1,2) runs NoMAS on a subnetwork of the complete gene interaction network.
The subnetwork is defined by a set, S, of seed vertices, and consists of all the vertices reachable
by at most <kprime> edges.
SNoMAS is a local search algorithm and all its solutions contain at least one of the seed vertices from S.
SNoMAS comes in three variations:

SNoMAS0:    Is the algorithm described above.
            Can enumerate the same number of solutions (containing at least one seed vertex) as NoMAS.
            Is faster than NoMAS.
            
SNoMAS1:    Constructs solutions by combination of smaller solutions of size at most <kprime> neighboring a seed vertex.
            Is faster than SNoMAS0.
            
SNoMAS2:    Same as SNoMAS1, but can also combine with vertices not neighboring a seed vertex.
            A bit slower than SNoMAS1 but faster than SNoMAS0.
            Can enumerate more solutions than SNoMAS1.


The program can be run with a multitude of arguments, which are listed in the table below:
-----------------------------------------------------------------------------------------
parameter       Type/Range              Deafult         Description
-----------------------------------------------------------------------------------------
network         String                  hint+hi2012     Path of a gene interaction network file

data            String                  ov              Path of a survival information and mutation data file	

output          String                  solution        Path of the solution file to be outputted by the program (no file ending)

import          String                  -               Path of the solution file to import into the program

seeds           String                  -               Path of the file containing gene symbols of seeds to SNoMAS

ignore			String					-				Path of the file containing genes whose mutations should be ignored

N               integer > 0             4               Number of processors

k               integer > 0             5               Maximium subnetnetwork cardinality

kprime          integer > 0, < k        (k + 1) / 2     SNoMAS local search space width

d               integer >= k, < 32      k + 1           Number of colors

L               integer > 1             5               Fat Table modification size

iterations      integer > 0             32              Number of color-coding iterations

seed            integer                 42              Seed for random number generators

solutions       integer > 0             10              Number of high scoring solutions reported

pvalue          integer > 0             -               Number of Monte Carlo iterations for p-value estimation

permutations    integer > 0             -               Number of permutations for statistical significance test

mutinfo         integer >= 0            -               Index of solution from imported solutions of which the survival/mutation information should be generated

error           double > 0, < 1         0.05            Error probability

seederror       double > 0, < 1         0.05            Error probability when generating seed vertices to SNoMAS

threshold       double >= 0.0           3.0             Mutation threshold: Mutations in patients that are mutated in less than <threshold> genes are removed
                                                        from the mutation matrix before the algorithm is run.

retain                                  3               Policy with regards to retaining a specific vertex/gene in the gene interaction network
                1                                       - Keep if gene has mutations
                2                                       - Keep if gene has degree > 1 in network
                3                                       - Keep if either (1) or (2)

mutmodel                                GI              The null model for mutations that should be used to permute the data
                GI                                      - Gene Identity model
                MS                                      - Marginal Sums model

func                                    MAX_NLR         Subnetwork scoring function
                MAX_NLR									- Maximization of normalized log-rank statsitic
                MIN_NLR                                 - Minimization of normalized log-rank statsitic
                SCORE_RED                               - Maximization of single-gene score for reduced survival (can only be used with algorithm Additive)
                SCORE_INC                               - Maximization of single-gene score for increased survival (can only by used with algorithm Additive)
                
algorithm                               NoMAS           Algorithm to use
                NoMAS                                   -
                FatTable                                - Fat Table modification of NoMAS
                Neighborhood                            - Neighborhood modification of NoMAS
                Additive                                - Modified version of NoMAS that uses the additive score based on single gene scores
                SNoMAS0                                 -
                SNoMAS1                                 -
                SNoMAS2                                 -
                Greedy1                                 -
                GreedyK                                 -
                GreedyDFS                               -
                Exhaustive                              - Exhaustive enumeration of all subnetworks of size <= k
-----------------------------------------------------------------------------------------
In order to specify the value of one of the parameters above, type the name of the parameter followed by the desired value (separated by space)
Example: ./nomas data datasets/lusc.txt k 5 func MIN_NLR

The general flow of the program is as follows.
1) Construct model based on network and data files
2) Find solutions in the model with the specified algorithm
3) If specified, perform p-value estimates
4) If specified, perform permutation test using the same algorithm
5) Output solutions to file

The program outputs two files with the given output filename.
1) filename.txt     - A file containing information about the solutions identified.
2) filename.png     - An image containing graphical representations of all the identified solutions.

If the parameter "permutations" is not specified, then the program will not perform a permutation test on the identified solutions.
Similarly, if the parameter "pvalue" is not specified, the program will not estimate the p-values.

However, these tests can be performed at a later time by importing the solution file filename.txt into the program, via the "import" command.
Example: ./nomas import solutions.txt pvalue 1000000
The imported file will be overwritten by the the results of permutation tests and pvalue estimates.
The image file with the same name as the imported solution file will be overwritten as well.
When a solution file is imported, any specified algorithm will be used ONLY for a permutation test (if such is specified).
That is, points 1) and 2) in the general program flow are skipped.
Network and data files are ignored when a solution file is imported. The relevant network and dataset is deduced from the solution file.

---------------------
SOLUTION FILE FORMAT
---------------------
The solution file contains several information about the reported subnetworks. The columns are:

Mutation count          - Number of patiens mutated in the subnetwork, i.e. the value m_1
log-rank                - Log-rank statistic
normalized log-rank     - Normalized log-rank statistic
p-value                 - The p-value estimated by the Monte Carlo method. This value is 0.0 if not estimations have been performed.
permutation p-value     - The p-value of the permutation test. This value is 0.0 if no test has been made, and -1.0 if NO better or equally good solution was found in the permuted data.
single-gene score       - The score of the subnetwork, by use of the additive scoring function. The score of each gene is estimated by the Monte Carlo method using 10^7 samples.

After these size columns follows a delimiter column with the symbol #, and then follows the list of gene symbols of the subnetwork.

The last four lines of a solution file, contains data used to reconstruct the subnetwork by the program.

NOTES:
The algorithm Exhaustive outputs solutions to both MAX_NLR and MIN_NLR in the same file, by essentially concatenating the lists of identified solutions from both optimization problems.

--------------------
NETWORK FILE FORMAT
--------------------
First line is the number <n> of genes in the network
The remaining lines have the format:
<gene symbl> <id> <id of neighbor 1> <id of neighbor 2> <id of neighbor 3> ...
For an arbitrary number of neighbors < n
All tokens are tab-separated

<gene_symbol>		- string
<id>				- integer
<id of neighbor *>	- integer

Example:
---------------------
3
gene_a	1	2
gene_b	2	1	3
gene_c	3	2
---------------------

--------------------
DATASET FILE FORMAT
--------------------
First line is the number <m> of patiens in the dataset
The remaining lines have the format
<patient id> <censoring> <survival time> <gene symbol> <gene symbol> <gene symbol> ...
For an arbitrary number of mutated genes <gene symbol>.
All tokens are tab-separated
All lines MUST be sorted by increasing survival times
All lines MUST contain at least one gene symbol, that is all patient sin the file should be mutated in some gene

<patient id>		- string
<censoring>			- 1 if dead (uncensored), 0 if alive (censored)
<surival time>		- double
<gene symbol>		- string

Example:
-----------------------------------------
4
patient_1	1	2.0		gene_a
patient_2	0	7.0		gene_a	gene_b
patient_3	1	12.0	gene_b
patient_4	1	42.0	gene_c
-----------------------------------------

--------------------
REFERENCES:
--------------------
if you use NoMAS, please cite: 
Tommy Hansen and Fabio Vandin. Finding Mutated Subnetworks Associated with Survival Time in Cancer. RECOMB 2016
