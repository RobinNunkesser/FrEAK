/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.searchspace;

import freak.core.population.*;

/**
 * @author  Stefan Tannenbaum, Kai
 */
public class GraphEdgeSelectionGenotype extends Genotype {
	
	/**
	 * The graph in which we select edges.
	 */
	GraphEdgeSelection.Graph graph;
	
	/**
	 * In this array, the selection status of edges in a graph
	 * is stored.
	 */
	protected boolean[] edgeSelection;
		
	/**
	 * Creates a new instance. All edges in the graph are unselected.
	 */
	public GraphEdgeSelectionGenotype(GraphEdgeSelection.Graph graph) {
		
		int size = graph.getNumberOfEdges();
		
		this.edgeSelection = new boolean[size];
				
		this.graph = graph;
	}
	
	/**
	 * Returns the edgeSelection array.
	 * @uml.property  name="edgeSelection"
	 */
	public boolean[] getEdgeSelection(){
		return(edgeSelection);
	}

  
    /**
	 * Sets the edgeSelection array.
	 * @uml.property  name="edgeSelection"
	 */
    public void setEdgeSelection(boolean[] sel){
    	this.edgeSelection = sel;	
    }
	
	
	/**
	 * @return  the graph
	 * @uml.property  name="graph"
	 */
	public GraphEdgeSelection.Graph getGraph() {
		return graph;
	}

    /**
     * This method compares the selection status of the edges
     * in the two genotypes and the two graphs.
     */	
	public boolean equals(Object other) {
	
		GraphEdgeSelectionGenotype o = (GraphEdgeSelectionGenotype)other;
		
		if(!graph.equals(o.graph))
		    return(false);
				
		for(int i = 0; i < edgeSelection.length; i++)
		    if(edgeSelection[i] != o.edgeSelection[i])
		        return(false);
		    
		return(true);
				 
	}

	public int hashCode() {
				
		return graph.hashCode();
	}

	public String toString() {
		return graph.toString();
	}
}
