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
 * A split operator distributes the ingoing individuals among the outports.
 * 
 * @author Dirk 
 */
abstract public class Split extends AbstractFork {

	/**
	 * Constructs a new <code>Split</code> operator.
	 * 
	 * @param graph a link back to the operator graph.
	 */
	public Split(OperatorGraph graph) {
		super(graph);
	}
}
