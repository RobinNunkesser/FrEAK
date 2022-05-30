/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

/**
 * Selects individuals from the ingoing individual list and outputs the 
 * selected individuals as well as the ingoing individuals.
 * 
 * @author Dirk 
 */
abstract public class Selection extends AbstractOperator {

	/**
	 * Create a new <code>Selection</code> operator.
	 * 
	 * @param graph a link back to the operator graph.
	 */
	public Selection(OperatorGraph graph) {
		super(graph);
	}
}
