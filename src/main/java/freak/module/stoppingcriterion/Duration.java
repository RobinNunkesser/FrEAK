/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.modulesupport.*;
import freak.core.stoppingcriterion.*;

/**
 * This stopping criterion is checked each time the graph representing the
 * algorithm is processed. If the time specified by the property <code>Duration
 * </code> has passed the run is stopped. 
 * 
 * @author Heiko, Stefan
 */
public class Duration extends AbstractStoppingCriterion implements Configurable, GenerationEventListener, RunEventListener {

	private int duration;
	/**
	 * The accumulated runtime at the moment the run was started.
	 */
	private double startTime;

	public Duration(Schedule schedule) {
		super(schedule);
		duration = 30;
	}

	public String getName() {
		return "Duration";
	}
	public String getDescription() {
		return "Stops a run after the specified duration in seconds.";
	}

	public void setPropertyDuration(Integer i) {
		if (i.intValue() > 0) {
			duration = i.intValue();
		}
	}

	public Integer getPropertyDuration() {
		return new Integer(duration);
	}

	public String getLongDescriptionForDuration() {
		return "The time after which the run is stopped in seconds.";
	}

	public void generation(GenerationEvent evt) {
		double time = getSchedule().getRunTime();
		if (time - startTime >= duration) {
			stopRun();
		}
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

	public void runStarted(RunEvent evt) {
		// We save the runtime already accumulated.
		startTime = getSchedule().getRunTime();
	}
}
