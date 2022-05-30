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
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;

/**
 * Computes the best individuals of all individuals observed in the current 
 * batch.
 * 
 * To work properly, the fitness of an individual must be deterministic 
 * and static: <code>evaluate</code> 
 * must always return the same fitness value for a 
 * specified individual.
 *  
 * @author Patrick, Dirk, Stefan
 */
public class BestIndividualsInBatch extends BestIndividualsInSomeTimePeriod implements BatchEventListener {

	public BestIndividualsInBatch(Schedule schedule) {
		super(schedule);
	}

	public String getName() {
		return "Best Individuals in Batch";
	}

	public String getDescription() {
		return "Computes the best individuals created in the batch. " +
			"To work properly, the fitness must be deterministic, i.e., " +
			"multiple fitness evaluations of the same individual must return " +
			"the same fitness value.";
	}

	public void createEvents() {
		super.createEvents();
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}

	public void batchStarted(BatchEvent evt) {
		reset();
	}
}