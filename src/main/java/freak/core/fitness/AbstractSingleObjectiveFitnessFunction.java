/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.fitness;

import freak.core.control.Schedule;

/**
 * An abstract superclass for all single objective fitness functions with a link
 * back to the current schedule.
 * 
 * @author Heiko, Michael
 */
public abstract class AbstractSingleObjectiveFitnessFunction extends AbstractFitnessFunction implements SingleObjectiveFitnessFunction {

	/**
	 * Constructs a new <code>AbstractSingleObjectiveFitnessFunction</code> with
	 * a link back to the current schedule.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public AbstractSingleObjectiveFitnessFunction(Schedule schedule) {
		super(schedule);		
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public double getUpperBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public int compareIndividuals(freak.core.population.Individual ind1, freak.core.population.Individual ind2) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
