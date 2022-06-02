/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.graphedgeselection;

import freak.core.control.*;
import freak.core.population.*;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;

/**
 * This class implements the fitness function for the minimal spanning tree
 * problem which takes into account the number of connected components, the
 * number of edges and the weights of the selected edges in the graph.
 */
public class MSTWithComponentAndEdgeCount extends AbstractMSTFitnessFunction {
	
	/**
	 * Creates a new instance of MSTWithComponentAndEdgeCount
	 */
	public MSTWithComponentAndEdgeCount(Schedule schedule) {
		super(schedule);
	}
	
	/**
	 * The value returned is calculated as follows: The Genotype passed
	 * represents a subset E' of the set of all edges in a graph.<br>
	 * The value is the sum of<br>
	 * - the number of connected components in the graph w.r.t. E'<br>
	 * - the number of edges in E'<br>
	 * - the sum of the weight of all edges in E'
	 */
	public double evaluate(Genotype gen) {
		
		GraphEdgeSelectionGenotype genotype = (GraphEdgeSelectionGenotype)gen;
		GraphEdgeSelection.Graph graph = genotype.getGraph();
		
		
		// Now we can evaluate the Individual using the methods of Graph
		int numOfComponents = graph.numOfConnectedComponentsForSelectedEdges(genotype);
		int numOfEdges = graph.getNumberOfSelectedEdges(genotype);
		int edgeWeightSum = graph.getSumOfWeightsOfAllSelectedEdges(genotype);
		
		int n = graph.getNumberOfNodes();
		
		int wmax = graph.getMaxEdgeWeight();
		int wub = wmax * n * n;
		
		return (-((numOfComponents - 1) * wub * wub + (numOfEdges - (n - 1)) * wub + edgeWeightSum));
	}
	
	public String getDescription() {
		return
		" The Individual passed represents a subset E' of the set " +
		" of all edges in a graph. " +
		" The value returned is the sum of the number of connected components in the graph wrt E', " +
		" the number of edges in E', and the sum of the weight of all edges in E'."
		;
	}
	
	public String getName() {
		return ("MST with components and edge count");
	}
	
	/**
	 * Compares the two genotypes following the convention in interface
	 * ComparingFitnessFunction.
	 */
	public int compareIndividuals(Individual ind1, Individual ind2) {
		if (ind1 == ind2) return 0;
		
		GraphEdgeSelectionGenotype gen1 = (GraphEdgeSelectionGenotype)ind1.getPhenotype();
		GraphEdgeSelectionGenotype gen2 = (GraphEdgeSelectionGenotype)ind2.getPhenotype();
		GraphEdgeSelection.Graph graph = gen1.getGraph();
		
		// compare the genotypes
		
		// compare component count
		int cc1 = graph.numOfConnectedComponentsForSelectedEdges(gen1);
		int cc2 = graph.numOfConnectedComponentsForSelectedEdges(gen2);
		
		if(cc1 < cc2)
			return(1);
		else if(cc1 > cc2)
			return(-1);
		
		// components match. compare edge count
		int edg1 = graph.getNumberOfSelectedEdges(gen1);
		int edg2 = graph.getNumberOfSelectedEdges(gen2);
		
		if(edg1 < edg2)
			return(1);
		else if(edg1 > edg2)
			return(-1);
		
		// components and edge count match... compare edge weights
		int ew1 = graph.getSumOfWeightsOfAllSelectedEdges(gen1);
		int ew2 = graph.getSumOfWeightsOfAllSelectedEdges(gen2);
		
		if(ew1 < ew2)
			return(1);
		else if(ew1 == ew2)
			return(0);
		else
			return(-1);
		
	}

}
