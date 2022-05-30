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
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.observer.AbstractObserver;

/**
 * Computes the average run time of all runs in the current batch.
 * 
 * @author Dirk
 */
public class AverageRunTime extends AbstractObserver implements BatchEventListener, RunEventListener {

	private double averageRunTime = 0;
	// can differ from the schedule's current run if the observer has been 
	// added later on
	private double numberOfObservedRuns = 0;
	
	public AverageRunTime(Schedule schedule) {
		super(schedule);
		
		setMeasure(BATCHES);
	}

	public Class getOutputDataType() {
		return Double.class;
	}

	public String getName() {
		return "Average Run Time";
	}

	public String getDescription() {
		return "Computes the average run time within all runs in the current batch.";
	}

	public void runFinalize(RunEvent evt) {
		int newValue = getSchedule().getCurrentGeneration();
		// compute new average value with a weighted sum
		averageRunTime = ((numberOfObservedRuns * averageRunTime) + newValue) / (numberOfObservedRuns + 1);
		
		numberOfObservedRuns++;
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

	public void batchFinished(BatchEvent evt) {
		updateViews(new Double(averageRunTime));
		numberOfObservedRuns = 0;
	}

}
