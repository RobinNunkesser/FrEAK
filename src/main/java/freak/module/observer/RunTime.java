/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.observer.*;

/**
 * Computes the run time of all runs.
 * 
 * @author Dirk
 */
public class RunTime extends AbstractObserver implements RunEventListener {

	/**
	 * Constructs a new <code>Run Time</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public RunTime(Schedule schedule) {
		super(schedule);
		setMeasure(RUNS);
	}

	public Class getOutputDataType() {
		return Integer.class;
	}

	public String getName() {
		return "Run Time";
	}

	public String getDescription() {
		return "Computes the run times of all runs. The run time is the number of generations until the selected stopping criterion stops the run.";
	}

	public void runFinalize(RunEvent evt) {
		updateViews(new Integer(getSchedule().getCurrentGeneration()));
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}
}
