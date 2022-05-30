/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.generalstring;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Genotype;
import freak.core.util.FreakMath;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;
import java.io.Serializable;

/**
 * An implementation of the Ising fitness function on cliques of equal size connected via a specified number of randomly chosen edges.
 * @author  Dirk
 */
public class IsingModelCliques extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable, BatchEventListener {

	public static final int RANDOM = 0;
	public static final int EVENLY = 1;
	public static final int STAIRS = 2;

	private int bridgeSelectionStrategy = RANDOM;

	private int numberOfBridges;
	private int numberOfCliques;
	
	private int corridorSize1 = 0;
	private int corridorSize2 = 0;
	
	/**
	 * @uml.property  name="cliques"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private Clique[] cliques;
	
	// a single bridge is represented by an array of two ints, the indices of
	// the corresponding nodes inside the genotype. 
	private int[][] bridges;
	
	// a redundant array containing adjacency lists for all nodes
	private int[][] adjacencyLists;

	/**
	 * Creates a new <code>IsingModelCliques</code>.
	 */
	public IsingModelCliques(Schedule schedule) {
		super(schedule);
		
		// if the search space's dimension is not even, every node represents
		// a clique.
		int dim = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension(); 
		if (((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() % 2 == 0) {
			setPropertyNumberOfCliques(new Integer(2)); 
		} else {
			setPropertyNumberOfCliques(new Integer(dim)); 
		}
		
		setPropertyNumberOfBridges(new Integer(1));
	}
	
	public void initialize() {
		super.initialize();

		int dim = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension();

		if (cliques != null && dim != cliques.length * cliques[0].getSize()) {
		    // re-compute number of cliques if the search space dimension has changed
			if (((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() % 2 == 0) {
				setPropertyNumberOfCliques(new Integer(2)); 
			} else {
				setPropertyNumberOfCliques(new Integer(dim)); 
			}
			initCliques();
			chooseEdges();
		}
	}

	protected double evaluate(Genotype genotype) {
		double fitness = 0;
		
		int[] gen = ((GeneralStringGenotype)genotype).getIntArray();
		
		// If there are no limited corridors and all possible bridges between
		// the cliques are present, the whole graph consists of one large 
		// clique. This special case is handled separately for performance 
		// reasons.
		if (!(bridges.length == getMaximalNumberOfBridges() && (corridorSize1 >= cliques[0].getSize() || corridorSize1 == 0) && (corridorSize2 >= cliques[0].getSize() || corridorSize2 == 0))) {
			// compute fitness values inside the cliques
			for (int i = 0; i < cliques.length; i++) {
				fitness += getFitnessInsideClique(cliques[i], gen);
			}
		
			// compute fitness values of bridges
			for (int i = 0; i < bridges.length; i++) {
				// add 1 if both nodes of the bridge are monochromatic
				if (gen[bridges[i][0]] == gen[bridges[i][1]]) fitness++;
			}
		} else {
			// the graph is one big clique
			Clique clique = new Clique(0, gen.length);
			fitness += getFitnessInsideClique(clique, gen);
		}
		
		return fitness;
	}
	
	private double getFitnessInsideClique(Clique clique, int[] gen) {
		double result = 0;
		
		int k = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getNumChars(); 

		// an array for counting all nodes with some color 
		int[] colors = new int[k];
			 
		// count colors for all nodes inside the current clique
		for (int j = 0; j < clique.size; j++) {
			// compute the index belonging to the node
			int index = clique.start + j;
			// increase the color value belonging to the node's color
			colors[gen[index]]++;
		}
			
		// Nodes with the same color form a monochromatic subgraph which
		// is a clique itself.
		for (int j = 0; j < k; j++) {
			// add fitness value for all monochromatic sub-cliques inside
			// the current clique
			result += colors[j] * (colors[j] - 1) / 2;
		}
		
		return result;
	}
	
	public String getName() {
		return "Ising Model (Cliques)";
	}

	public String getDescription() {
		return "The Ising Model is a model derived from statistical mechanics " +
			   "describing the behavior of atoms with two spins where neighbored atoms tend to orient in the same direction as their neighbors.\n" +
			   "Another view is an inversion of the colorability problem: an edge contributes a value of 1 to the fitness if and only if its nodes have got the same color. So, all colorings with only one color are optimal.\n" +
			   "Here, the graph is described by a specified number of cliques of " +
			   "equal size connected via a specified amount of bridges. " +
			   "Bridges are randomly chosen edges between two different cliques.";
	}

	public double getLowerBound() throws UnsupportedOperationException {
		int fitness = 0;
		int k = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getNumChars(); 
		
		if (cliques != null) {
			for (int i = 0; i < cliques.length; i++) {
				// Partitioning the clique into subgraphs by the color of 
				// the nodes leads to k subgraphs if k is the number of colors.
				// These subgraphs are themselves monochromatic cliques.
				// The fitness is minimal if all k sub-cliques are of equal size, so
				// we sum up the fitness values of k monochromatic sub-cliques of 
				// average size. 
				int averageSize = cliques[i].size / k;
				int fitnessOfAverageSubClique = (averageSize * (averageSize - 1) / 2); 
				fitness += k * fitnessOfAverageSubClique;	
			}
		}
		
		// Pessimistically, we assume that all bridges are colored in two 
		// different colors. 
		return fitness;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		int fitness = 0;
		
		if (cliques != null) {
			for (int i = 0; i < cliques.length; i++) {
				// if the fitness value is optimal, all cliques are monochromatic 
				fitness += cliques[i].size * (cliques[i].size - 1) / 2;	
			}
		}

		// add the fitness value resulting from monochromatic bridges
		return fitness + numberOfBridges;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	private void initCliques() {
		if (((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() % numberOfCliques != 0) { 
			throw new IllegalArgumentException("The number of cliques is not a factor of the search space dimension.");
		}
			
		cliques = new Clique[numberOfCliques];
		
		int cliqueSize = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() / numberOfCliques;
		for (int i = 0; i < numberOfCliques; i++) {
			cliques[i] = new Clique(i * cliqueSize, cliqueSize);
		}
	}
	
	private void chooseEdges() {
		bridges = new int[numberOfBridges][2];
		
		switch (bridgeSelectionStrategy) {
			case RANDOM : chooseEdgesRandomly(); break;
			case EVENLY : chooseEdgesEvenly(); break;
			case STAIRS : chooseEdgesAsStairs(); break;
		}
		
		createAdjacencyLists();
	}
	
	public void chooseEdgesAsStairs() {
		if (cliques.length == 2) {
			int n = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension();
			
			numberOfBridges = n*n/16 + n/4;
			bridges = new int[numberOfBridges][2];

			int index = 0;
			for (int i = 0; i < n/4; i++) {
				int numberOfEdges = n/2 - 2*i;
				int[] endNodes = FreakMath.getKofN(schedule, numberOfEdges, n/2);
				for (int j = 0; j < endNodes.length; j++) {
					bridges[index][0] = i;
					bridges[index][1] = endNodes[j] + n/2;
					index++;
				}
			}
		}
	}
	
	private void createAdjacencyLists() {
		// fill adjacency lists
		int dim = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension();
		// an array of maximal size is created and then compressed later on
		adjacencyLists = new int[dim][dim-1];
		// the number of fields in the adjacencyLists array filled with data
		int[] sizes = new int[dim];
		
		// add bridges to adjacency lists
		for (int i = 0; i < bridges.length; i++) {
			// write the adjacent node into the adjacency list and increase size 
			// of the current node's adjacency list 
			adjacencyLists[bridges[i][0]][sizes[bridges[i][0]]++] = bridges[i][1];
			adjacencyLists[bridges[i][1]][sizes[bridges[i][1]]++] = bridges[i][0];
		}
		// add nodes inside the clique to the adjacency lists
		for (int i = 0; i < cliques.length; i++) {
			for (int j = cliques[i].getStart(); j < cliques[i].getStart() + cliques[i].getSize(); j++) {
				for (int k = j + 1; k < cliques[i].getStart() + cliques[i].getSize(); k++) {
					// write the adjacent node into the adjacency list and increase size 
					// of the current node's adjacency list 
					adjacencyLists[j][sizes[j]++] = k;
					adjacencyLists[k][sizes[k]++] = j;
				}
			}
		}
		
		// compress adjacency lists
		for (int i = 0; i < adjacencyLists.length; i++) {
			int[] compressed = new int[sizes[i]];
			System.arraycopy(adjacencyLists[i], 0, compressed, 0, sizes[i]);
			adjacencyLists[i] = compressed;
		}
	}
	
	public int[] getAdjacencyList(int node) {
		return adjacencyLists[node];
	}
	
	private void chooseEdgesRandomly() {
		int cliqueSize = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() / numberOfCliques;
		
		// We construct an enumeration of all edges with nodes in different 
		// cliques. Since the graph is undirected, we only take edges into 
		// account where the index of the first node is smaller than the index
		// of the second node. Thus, edges can be seen here as directed edges
		// going from the node with smaller index to the node with higher index.
		 
		// Then, the enumeration is used to choose the desired amount of edges
		// randomly.
		// While enumerating the edges, we create a table that is used to 
		// compute the edge from its index within the enumeration.

		// the size of the enumeration with indices 0, ..., range-1		
		int range = (numberOfCliques * (numberOfCliques - 1) / 2) * (corridorSize1 == 0 ? cliqueSize : corridorSize1) * (corridorSize2 == 0 ? cliqueSize : corridorSize2);
		
		// the first index specifies the index inside the enumeration. 
		// Using the index, one receives an inner array containing a pair of 
		// ints representing the indices of the two nodes inside the genotype.
		int[][] decodingTable = new int[range][2];
		
		int counter = 0;
		
		// process all cliques as sources for edges 
		for (int firstClique = 0; firstClique < cliques.length - 1; firstClique++) {
			// process all nodes inside the corridor for the source
			for (int i = 0; i < (corridorSize1 == 0 ? cliques[firstClique].getSize() : corridorSize1); i++) {
				// process all cliques as destinations for edges
				for (int secondClique = firstClique + 1; secondClique < cliques.length; secondClique++) {
					// process all nodes inside the corridor for the destination
					for (int j = 0; j < (corridorSize2 == 0 ? cliques[secondClique].getSize() : corridorSize2); j++) {
						// store indices of current nodes
						decodingTable[counter][0] = firstClique * cliqueSize + i;
						decodingTable[counter][1] = secondClique * cliqueSize + j;
				
						counter++;
					}
				}
			}
		}
		
		// Choose numberOfBridges different numbers out of the projection's 
		// range
		int[] encodedEdges = FreakMath.getKofN(schedule, numberOfBridges, range);
		
		// now decode the int values back to pairs of ints 
		for (int i = 0; i < encodedEdges.length; i++) {
			bridges[i][0] = decodingTable[encodedEdges[i]][0];
			bridges[i][1] = decodingTable[encodedEdges[i]][1];
		}
	}
	
	private void chooseEdgesEvenly() {
		int cliqueSize = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() / numberOfCliques;
		int numberOfCorridors = cliques.length * (cliques.length - 1) / 2;

		int counter = 0;
		int corridorCounter = 0;
		
		// process all cliques as sources for edges 
		for (int firstClique = 0; firstClique < cliques.length - 1; firstClique++) {
			// process all cliques as destinations for edges
			for (int secondClique = firstClique + 1; secondClique < cliques.length; secondClique++) {
				
				int numberOfEdgesInCurrentCorridor = (int)Math.round((numberOfBridges * (corridorCounter + 1)) / (double)numberOfCorridors) - counter;
				
				// Boolean values specifying which edges within the corridor
				// are chosen.  
				boolean[] isChosen = new boolean[(corridorSize1 == 0 ? cliques[firstClique].getSize() : corridorSize1) * (corridorSize2 == 0 ? cliques[secondClique].getSize() : corridorSize2)];
				int offsetInSecondClique = 0; 
				int indexOfCurrentBridge = 0;
		
				// distribute edges within the current corridor between 
				// firstClique and secondClique				
				for (int i = 0; i < numberOfEdgesInCurrentCorridor; i++) {
					bridges[counter][0] = firstClique * cliqueSize + (i % (corridorSize1 == 0 ? cliques[firstClique].getSize() : corridorSize1));
					
					do {
						// choose provisional second node  
						bridges[counter][1] = secondClique * cliqueSize + ((offsetInSecondClique + i) % (corridorSize2 == 0 ? cliques[secondClique].getSize() : corridorSize2));
						
						// compute the edge's index inside the isChosen array
						indexOfCurrentBridge = (bridges[counter][0] % cliqueSize) * (corridorSize2 == 0 ? cliques[secondClique].getSize() : corridorSize2) + (bridges[counter][1] % cliqueSize);
						
						// if the edge has already been chosen, all further 
						// choices of second nodes are shifted to the right
						// by increasing the offset
						if (isChosen[indexOfCurrentBridge]) offsetInSecondClique++;
						
					} while (isChosen[indexOfCurrentBridge]);

					isChosen[indexOfCurrentBridge] = true;
				
					counter++;
				}
			
				corridorCounter++;
			}
		}
	}
	
	private int getMaximalNumberOfBridges() {
		int dim = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int maxNumberOfBridgesInCorridors = (corridorSize1 == 0 ? dim/numberOfCliques : corridorSize1) * (corridorSize2 == 0 ? dim/numberOfCliques : corridorSize2);
		return (numberOfCliques * (numberOfCliques - 1) / 2) * maxNumberOfBridgesInCorridors;
	}

	public void batchStarted(BatchEvent evt) {
		initCliques();
		chooseEdges();
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	// --- property methods

	/**
	 * @return the number of bridges, i.e., randomly chosen edges connecting the two cliques.
	 */
	public Integer getPropertyNumberOfBridges() {
		return new Integer(numberOfBridges);
	}

	/**
	 * Sets the number of bridges, i.e., randomly chosen edges connecting the two cliques.
	 */
	public void setPropertyNumberOfBridges(Integer i) {
		numberOfBridges = i.intValue();

		if (numberOfBridges < 0) numberOfBridges = 0;

		if (numberOfBridges > getMaximalNumberOfBridges()) numberOfBridges = getMaximalNumberOfBridges();
	}

	public String getShortDescriptionForNumberOfBridges() {
		return "Number of bridges";
	}
	
	public String getLongDescriptionForNumberOfBridges() {
		return "The number of bridges, i.e., the number of edges connecting the two cliques. " +
			"The bridges are chosen randomly at the start of a new batch and every time the number of bridges is altered.";
	}
	
	/**
	 * @return the number of cliques.
	 */
	public Integer getPropertyNumberOfCliques() {
		return new Integer(numberOfCliques);
	}

	/**
	 * Sets the number of cliques. Note that the number of cliques must be a
	 * factor of the search space's dimension to ensure that all cliques are
	 * of equal size.
	 */
	public void setPropertyNumberOfCliques(Integer i) {
		if (i.intValue() >= 2) {
			if (((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() % i.intValue() == 0) {
				numberOfCliques = i.intValue();
			}
			
			// update other properties
			setPropertyNumberOfBridges(new Integer(numberOfBridges));
			setPropertyCorridorSize1(new Integer(corridorSize1));
			setPropertyCorridorSize2(new Integer(corridorSize2));
		}
	}

	public String getShortDescriptionForNumberOfCliques() {
		return "Number of cliques";
	}
	
	public String getLongDescriptionForNumberOfCliques() {
		return "The number of cliques. To ensure that all cliques are of equal " +
			"size, the number of cliques must be a factor of the search space's " +
			"dimension.";
	}
	
	public Integer getPropertyCorridorSize1() {
		return new Integer(corridorSize1);
	}
	
	public void setPropertyCorridorSize1(Integer i) {
		corridorSize1 = i.intValue();

		if (corridorSize1 != 0) {
			int cliqueSize = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() / numberOfCliques;
			if (corridorSize1 < 1) corridorSize1 = 1;
			if (corridorSize1 > cliqueSize) corridorSize1 = cliqueSize; 
		}

		setPropertyNumberOfBridges(new Integer(numberOfBridges));
	}
	
	public String getShortDescriptionForCorridorSize1() {
		return "Corridor size (first nodes)";
	}

	public String getLongDescriptionForCorridorSize1() {
		return "If the corridor size is greater than the 0, only " +
			"the first nodes within the clique can have bridges to nodes in " +
			"other cliques. The corridor size for first nodes only holds for " +
			"first nodes of undirected edges. By convention, these are the " +
			"nodes with smaller indices in the genotype. " +
			"Using different values for the first " +
			"and the second node leads to asymmetric distributions.";
	}

	public Integer getPropertyCorridorSize2() {
		return new Integer(corridorSize2);
	}
	
	public void setPropertyCorridorSize2(Integer i) {
		corridorSize2 = i.intValue();
		
		if (corridorSize2 != 0) {
			int cliqueSize = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension() / numberOfCliques;
			if (corridorSize2 < 1) corridorSize2 = 1;
			if (corridorSize2 > cliqueSize) corridorSize2 = cliqueSize; 
		}
		
		setPropertyNumberOfBridges(new Integer(numberOfBridges));
	}

	public String getShortDescriptionForCorridorSize2() {
		return "Corridor size (second nodes)";
	}

	public String getLongDescriptionForCorridorSize2() {
		return "If the corridor size is greater than 0, only " +
			"the first nodes within the clique can have bridges to nodes in " +
			"other cliques. The corridor size for second nodes only holds for " +
			"second nodes of undirected edges. By convention, these are the " +
			"nodes with larger indices in the genotype. " +
			"Using different values for the first " +
			"and the second node leads to asymmetric distributions.";
	}

	public void setPropertyBridgeSelectionStrategy(StringArrayWrapper saw) {
		bridgeSelectionStrategy = saw.getIndex();
	}
	
	public StringArrayWrapper getPropertyBridgeSelectionStrategy() {
		String[] s = new String[] { "Random", "Evenly distributed", "Stairs" };
		return new StringArrayWrapper(s, bridgeSelectionStrategy);
	}
	
	public String getShortDescriptionForBridgeSelectionStrategy() {
		return "Bridge selection strategy";
	}

	public String getLongDescriptionForBridgeSelectionStrategy() {
		return "The strategy used to select the bridges. Choose random " +
			"distribution or evenly distributed edges. Stairs is a special " +
			"bridge distribution for two cliques only.";
	}

	
	/**
	 * A very simple entity class representing a clique.
	 * @author  Dirk
	 */
	public class Clique implements Serializable {
		
		int size;
		int start;
		
		Clique(int start, int size) {
			this.start = start;
			this.size = size;
		}
		
		/**
		 * @return  the size of the clique.
		 * @uml.property  name="size"
		 */
		public int getSize() {
			return size;
		}

		/**
		 * @return  the start index in the GeneralString genotype.
		 * @uml.property  name="start"
		 */
		public int getStart() {
			return start;
		}

	}
	
	/**
	 * @return  the bridges between the cliques as pairs of the nodes' indices in the genotype.
	 * @uml.property  name="bridges"
	 */
	public int[][] getBridges() {
		return bridges;
	}

	/**
	 * @return  the cliques.
	 * @uml.property  name="cliques"
	 */
	public Clique[] getCliques() {
		return cliques;
	}

}
