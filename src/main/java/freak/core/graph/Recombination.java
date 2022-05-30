/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import freak.core.control.Schedule;
import freak.core.modulesupport.IncompatibleModuleException;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.searchspace.SearchSpace;

/**
 * An abstract superclass for all recombination operators.
 * 
 * @author Dirk 
 */
abstract public class Recombination extends AbstractVariation {

	/**
	 * Creates a new <code>Recombination</code> operator.
	 * 
	 * @param graph a link back to the operator graph.
	 */
	public Recombination(OperatorGraph graph) {
		super(graph);
	}

	public void testSchedule(Schedule schedule)
		throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		// test if the operator's package is compatible with the current
		// search space
		SearchSpace searchSpace = schedule.getGenotypeSearchSpace();
		String searchSpaceName = searchSpace.getClass().getName().toLowerCase();
		searchSpaceName = searchSpaceName.substring(searchSpaceName.lastIndexOf('.') + 1, searchSpaceName.length());
		
		String packageName = this.getClass().getPackage().getName();
		
		if (!(packageName.endsWith(searchSpaceName) || packageName.endsWith("common"))) {
			throw new IncompatibleModuleException(schedule.getGenotypeSearchSpace(), "Wrong search space.");
		}
	}


}
