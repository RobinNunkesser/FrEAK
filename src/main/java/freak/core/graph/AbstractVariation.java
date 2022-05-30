/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import freak.core.searchspace.*;

/**
 * An abstract superclass for all operators with the ability to create new 
 * genotypes.
 * 
 * @author Dirk 
 */
abstract public class AbstractVariation extends AbstractOperator {
	/**
	 * Creates a new <code>AbstractVariation</code> operator.
	 * 
	 * @param graph a link back to the operator graph.
	 */
	public AbstractVariation(OperatorGraph graph) {
		super(graph);
	}

	/**
	 * Returns the current genotype search space.
	 * 
	 * @return the current genotype search space.
	 */
	public SearchSpace getGenotypeSearchSpace() {
		return getOperatorGraph().getSchedule().getGenotypeSearchSpace();
	}
}
