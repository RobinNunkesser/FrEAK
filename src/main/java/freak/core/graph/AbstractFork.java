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
 * An abstract superclass for all operators with one inport and an arbitrary number 
 * of outports.
 * 
 * @author Dirk 
 */
abstract public class AbstractFork extends AbstractOperator {
	/**
	 * Creates a new <code>AbstractFork</code> operator.
	 * 
	 * @param graph a link back to the operator graph.
	 */
	public AbstractFork(OperatorGraph graph) {
		super(graph);
		super.addInPort(0);
	}

	/**
	 * Adding inports is not supported and therefore throws an
	 * UnsupportedOperationException.
	 * Note that void addInPort() is based on this function and
	 * isn't supported either.
	 */
	public void addInPort(int index) {
		throw new UnsupportedOperationException("Can't add inPort to this type of operator");
	}

	/**
	 * Removing inports is not supported and therefore throws an
	 * UnsupportedOperationException.
	 */
	public void removeInPort(int index) {
		throw new UnsupportedOperationException("Can't remove inPort from this type of operator");
	}

}
