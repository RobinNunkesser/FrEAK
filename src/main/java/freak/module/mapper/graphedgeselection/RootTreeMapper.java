/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.mapper.graphedgeselection;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.mapper.AbstractMapper;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;

/**
 * @author  Michael, Stefan
 */
public class RootTreeMapper extends AbstractMapper implements BatchEventListener {
	
	private GeneralString genotypeSearchSpace;
	
	public RootTreeMapper(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();
		
		try {
			int dim = ((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getDimension();
			if (genotypeSearchSpace == null || genotypeSearchSpace.getDimension() != dim) createGenotypeSearchSpace();
		} catch (IllegalStateException exc) {
			// dimension not defined / no graph created yet
			genotypeSearchSpace = new GeneralString(schedule);
		}
	}
	
	private void createGenotypeSearchSpace() {
		genotypeSearchSpace = new GeneralString(schedule);
		GraphEdgeSelection phenotypeSearchSpace = (GraphEdgeSelection)schedule.getPhenotypeSearchSpace();
		int numberOfNodes = phenotypeSearchSpace.getDimension();
		genotypeSearchSpace.setPropertyDimension(new Integer(numberOfNodes - 1));
		genotypeSearchSpace.setPropertyNumberOfChars(new Integer(numberOfNodes - 1));
		
		schedule.callInitialize();
	}
	
	/**
	 * @return  the genotypeSearchSpace
	 * @uml.property  name="genotypeSearchSpace"
	 */
	public SearchSpace getGenotypeSearchSpace() {
		return genotypeSearchSpace;
	}
	
	public Genotype genotypeToPhenotype(Genotype genotype) {
		// -- get the graph
		GraphEdgeSelection ss = (GraphEdgeSelection)schedule.getPhenotypeSearchSpace();
		GraphEdgeSelection.Graph graph = ss.getGraph();
		// -- create a new genotype
		GraphEdgeSelectionGenotype gene = new GraphEdgeSelectionGenotype(graph);
		boolean[] edgeSelection = gene.getEdgeSelection();
		
		int[] rootDirectedTree = ((GeneralStringGenotype)genotype).getIntArray();
		
		for (int i = 0; i < rootDirectedTree.length; i++) {
			//edge between node v and v isn't allowed => now edge to the last node
			if (rootDirectedTree[i] == i+1)
				rootDirectedTree[i] = rootDirectedTree.length;
			if (graph.containsEdge(i+1, rootDirectedTree[i])) {
				edgeSelection[graph.findEdgeSelectionIndexForEdge(i+1, rootDirectedTree[i])] = true;
			} else {
				return new GraphEdgeSelectionGenotype(graph);
			}
		}
		
		gene.setEdgeSelection(edgeSelection);
		return gene;
	}
	
	public String getName() {
		return "Root Directed Tree (General String)";
	}
	
	public String getDescription() {
		return "Complete graphs are recommended.";
	}
	
	public void batchStarted(BatchEvent evt) {
		createGenotypeSearchSpace();
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}

}
