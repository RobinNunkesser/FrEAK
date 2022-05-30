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
import freak.core.mapper.AbstractMapper;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;
import java.util.ArrayList;

/**
 * Maps spanning trees (edge selections of search space GraphEdgeSelection) to Prüfer numbers and vice versa. For this mapper to work correctly the underlying graph of the GraphEdgeSelection search space must be full.) The mapper performs the mapping from GraphEdgeSelection to GeneralString.
 * @author  Oliver
 */
public class PruferNrMapper extends AbstractMapper {

	private GeneralString genotypeSearchSpace;

	public PruferNrMapper(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();
		
		genotypeSearchSpace = new GeneralString(schedule);
		GraphEdgeSelection phenotypeSearchSpace = (GraphEdgeSelection)schedule.getPhenotypeSearchSpace();
		int vertices = phenotypeSearchSpace.getPropertyNumberOfVertices().intValue();
		genotypeSearchSpace.setPropertyDimension(new Integer(vertices - 2));
		genotypeSearchSpace.setPropertyNumberOfChars(new Integer(vertices));
		
//		schedule.callInitialize();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		GraphEdgeSelection phenotypeSearchSpace = (GraphEdgeSelection)schedule.getPhenotypeSearchSpace();
		int vertices = phenotypeSearchSpace.getPropertyNumberOfVertices().intValue();
		if (phenotypeSearchSpace.getPropertyNumberOfEdges().intValue() != (vertices * (vertices - 1) / 2))
			throw new UnsupportedEnvironmentException();
	}

	/**
	 * @return  the genotypeSearchSpace
	 * @uml.property  name="genotypeSearchSpace"
	 */
	public SearchSpace getGenotypeSearchSpace() {
		return genotypeSearchSpace;
	}

	public Genotype genotypeToPhenotype(Genotype genotype) {
		if (genotype == null) return null;
		
		// -- get the graph
		GraphEdgeSelection ss = (GraphEdgeSelection)schedule.getPhenotypeSearchSpace();
		GraphEdgeSelection.Graph graph = ss.getGraph();
		// -- create a new genotype
		GraphEdgeSelectionGenotype gene = new GraphEdgeSelectionGenotype(graph);
		boolean[] edgeSelection = gene.getEdgeSelection();

		// -- get prufernr
		int[] pruferNr = ((GeneralStringGenotype)genotype).getIntArray();
		
		// -- the lookupMap contains as many entries as nodes and each entry represents how often the node is "selected"
		int[] lookupMap = new int[pruferNr.length + 2];
		for (int i = 0; i < pruferNr.length; i++) lookupMap[pruferNr[i]]++;
		
		int startAt = 0;
		
		// -- now parse the prufer number
		for (int i = 0; i < pruferNr.length; i++) {
			// -- we search the first node that doesn't occur in the lookupMap
			int j = startAt;
			while (j < graph.getNumberOfNodes() && lookupMap[j] != 0) j++;
			// -- the next number of the prufer number is removed here
			lookupMap[pruferNr[i]]--;
			// -- and the found node is added
			lookupMap[j]++;
			// -- adjust index to start next scan
			if (lookupMap[pruferNr[i]] == 0 && pruferNr[i] < j) startAt = pruferNr[i];
			else startAt = j+1;
			// -- select edge
			edgeSelection[graph.findEdgeSelectionIndexForEdge(pruferNr[i], j)] = true;
		}
		// -- add last edge
		int node1 = startAt;
		while (node1 < graph.getNumberOfNodes() && lookupMap[node1] != 0) node1++;
		int node2 = node1 + 1;
		while (node2 < graph.getNumberOfNodes() && lookupMap[node2] != 0) node2++;
		edgeSelection[graph.findEdgeSelectionIndexForEdge(node1, node2)] = true;

		gene.setEdgeSelection(edgeSelection);
		
		return gene;
	}

	public Genotype phenotypeToGenotype(Genotype phenotype) {
		if (phenotype == null) return null;

		GraphEdgeSelectionGenotype originalGraphGenotype = (GraphEdgeSelectionGenotype)phenotype;
		GraphEdgeSelection.Graph graph = originalGraphGenotype.getGraph();

		// -- create new phenotype on which we can work
		GraphEdgeSelectionGenotype graphGenotype = new GraphEdgeSelectionGenotype(graph);
		boolean[] edgeSelection = originalGraphGenotype.getEdgeSelection();
		boolean[] workEdgeSelection = new boolean[edgeSelection.length];
		for (int i = 0; i < edgeSelection.length; i++) workEdgeSelection[i] = edgeSelection[i];
		graphGenotype.setEdgeSelection(workEdgeSelection);

		// -- the resulting Prüfer number
		int[] pnum = new int[graph.getNumberOfNodes() - 2];

		// -- build incidenceTable
		int[] incidenceTable = new int[graph.getNumberOfNodes()];
		for (int i = 0; i < graph.getNumberOfNodes(); i++)
			incidenceTable[i] = graph.getNumberOfSelectedIncidentEdgesForNode(i, graphGenotype);
		
		int startAt = 0;

		for (int i = 0; i < pnum.length; i++) {
			// -- determine lowest leaf
			int j = startAt;
			while (j < graph.getNumberOfNodes() && incidenceTable[j] != 1) j++;
			
			// -- now j is the number of the lowest leaf and edges contains the edge
			ArrayList edges = graph.getSelectedIncidentEdgesForNode(j, graphGenotype);
			GraphEdgeSelection.Graph.Edge edge = (GraphEdgeSelection.Graph.Edge)edges.get(0);
			pnum[i] = edge.getEndNode();
			
			// -- remove the edge from the genotype
			workEdgeSelection[edge.getIndexInGenotypeArray()] = false;
			
			incidenceTable[edge.getStartNode()]--;
			incidenceTable[edge.getEndNode()]--;
		}
		
		// -- create new Genotype and return it
		GeneralStringGenotype gene = new GeneralStringGenotype(pnum, graph.getNumberOfNodes());
		return gene;
	}

	public String getName() {
		return "Edge Selection to Prüfer Number";
	}

	public String getDescription() {
		return "This mapper works only on complete graphs. It performs a bijection between spanning-trees and a generalstring which represents the corresponding Prüfer-number. The mapper works bidirectional.";
	}

}
