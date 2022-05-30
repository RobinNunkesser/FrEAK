/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.graphedgeselection;

import freak.core.control.Schedule;
import freak.core.population.Genotype;
import freak.module.searchspace.*;

/**
 * @author Oliver
 */
public class MSTOnSpanningTrees extends AbstractMSTFitnessFunction {

	public MSTOnSpanningTrees(Schedule schedule) {
		super(schedule);
	}

	public double evaluate(Genotype gen) {
		GraphEdgeSelectionGenotype genotype = (GraphEdgeSelectionGenotype)gen;
		GraphEdgeSelection.Graph graph = genotype.getGraph();
		return -graph.getSumOfWeightsOfAllSelectedEdges(genotype);
	}

	public String getName() {
		return "MST on Spanning Trees";
	}

	public String getDescription() {
		return "This function just sums up the edge weights to find a minimal spanning tree. "
			+ "It is only for use if every individual represents a spanning tree itself (like with Prüfer numbers). "
			+ "Calculations of connected components are not needed here as they are always 1.";
	}

}
