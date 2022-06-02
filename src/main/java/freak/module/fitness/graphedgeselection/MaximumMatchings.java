/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.graphedgeselection;

import edu.ucsb.cs.jicos.applications.utilities.graph.WeightedMatch;
import freak.core.control.*;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.fitness.*;
import freak.core.population.*;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;

/**
 * @author  Michael, Christian
 */
				      
public class MaximumMatchings extends AbstractStaticSingleObjectiveFitnessFunction implements BatchEventListener, RunEventListener {
	
	private GraphEdgeSelectionGenotype optimum;
	int[] maxMatchingCache;
	boolean firstTime;
	int number;
	int lowerBound = 0;
	
	public MaximumMatchings(Schedule schedule) {
		super(schedule);
		firstTime = true;
		number = 0;
	}
	
	public double evaluate(Genotype genotype) {
		GraphEdgeSelectionGenotype edgeSelectionGenotype = (GraphEdgeSelectionGenotype)genotype;
		GraphEdgeSelection.Graph graph = edgeSelectionGenotype.getGraph();
		
		int nodes = graph.getNumberOfNodes();
		int edges = graph.getNumberOfEdges();
		
		int[] nodesCoveredCount = new int[nodes];
		
		boolean[] edgeSelection = edgeSelectionGenotype.getEdgeSelection();
		
		int edgeCount = 0;
		int penalty = 0;
		GraphEdgeSelection.Graph.Edge edge;
		
		for(int i = 0; i < edges; i++) {
			if (edgeSelection[i]) {
				edgeCount++;
				edge = graph.edgeForSpeedupIndex(i);
				int n1 = edge.getEndNode();
				int n2 = edge.getStartNode();
				
				nodesCoveredCount[n1]++;
				nodesCoveredCount[n2]++;
			}
		}
		
		//calculate penalty
		for(int i = 0; i < nodes; i++) {
			int count = nodesCoveredCount[i];
			if (count > 1)
				penalty += (count*(count-1))/2;
		}

		if (lowerBound > -penalty)
			lowerBound = -penalty;
		
		if (penalty > 0)
			return -penalty;
		
		return edgeCount;
	}
	
	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		//throw new UnsupportedOperationException();

		if (firstTime == true) {		
			GraphEdgeSelection.Graph graph = ((GraphEdgeSelection)(schedule.getPhenotypeSearchSpace())).getGraph();

			// -- if we don't have a graph yet, just return anything. Otherwise
			// -- the OptimumReached stopping criterium won't be available.
			if (graph == null) return 0;

			int nodes = graph.getNumberOfNodes();

			// convert graph into cost-matrix
			int costs[][] = new int [nodes][nodes];
			for (int i=0; i<nodes; i++) {
				costs[i][i] = 0;
				for (int j=i+1; j<nodes; j++) {
					int edgeNr = graph.findEdgeSelectionIndexForEdge(i,j);
					if (edgeNr != -1) costs[i][j] = 1; else costs[i][j] = 0;
					costs[j][i] = costs[i][j];
				}
			}

			// -- cache the matching as returned by WeightMatch
			maxMatchingCache = new WeightedMatch(costs).weightedMatch(false);

			// calculate the optimal number of matching-edges
			number = 0;
			for (int i = 1; i < maxMatchingCache.length - 1; i++)
				if (maxMatchingCache[i] != 0) number++;
			number = (int)(number/2);
			firstTime = false;
		}
		return number;
	}
	
	/**
	 * @return  the lowerBound
	 * @uml.property  name="lowerBound"
	 */
	public double getLowerBound() throws UnsupportedOperationException {
		//for fitness sharing
		return lowerBound;
	}
	
	public double getUpperBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		if (optimum == null) {
			GraphEdgeSelection.Graph graph = ((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getGraph();
			if (graph != null) {
				getOptimalFitnessValue();
				if (!firstTime) {
					optimum = new GraphEdgeSelectionGenotype(graph);
					boolean[] edgeSelection = optimum.getEdgeSelection();
					for (int i=1; i < maxMatchingCache.length - 1; i++) {
						int j = maxMatchingCache[i];
						if (j != 0) {
							int edgeIndex = graph.findEdgeSelectionIndexForEdge(i-1, j-1);
							edgeSelection[edgeIndex] = true;
						}
					}
					optimum.setEdgeSelection(edgeSelection);
				}
			}
		}
		return optimum;
	}
	
	public String getDescription() {
		return "(none)";
	}
	
	public String getName() {
		return "Maximum Matchings";
	}

	public void batchStarted(BatchEvent evt) {
		// -- reset to firstTime
		firstTime = true;
		lowerBound = 0;
	}

	public void runStarted(RunEvent evt) {
		// -- reset to firstTime
		firstTime = true;
		optimum = null;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

}
