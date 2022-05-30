/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer;

import freak.core.control.Schedule;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;

/**
 * Computes the best individuals of all individuals observed in the current 
 * run.
 * 
 * To work properly, the fitness of an individual must be deterministic 
 * and static: <code>evaluate</code> 
 * must always return the same fitness value for a 
 * specified individual.
 *  
 * @author Patrick, Dirk
 */
public class BestIndividualsInRun extends BestIndividualsInSomeTimePeriod implements RunEventListener {

	public BestIndividualsInRun(Schedule schedule) {
		super(schedule);
	}

	public String getName() {
		return "Best Individuals in Run";
	}

	public String getDescription() {
		return "Computes the best individuals created in the run. " +
			"To work properly, the fitness must be deterministic, i.e., " +
			"multiple fitness evaluations of the same individual must return " +
			"the same fitness value.";
	}

	public void runFinalize(RunEvent evt) {
		reset();
	}
	
	public void createEvents() {
		super.createEvents();
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}
}