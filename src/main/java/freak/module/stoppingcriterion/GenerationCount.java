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
 * This criterion stopps the algorithm after a given number of generations.
 * 
 * @author Heiko
 */
public class GenerationCount extends AbstractStoppingCriterion implements Configurable, GenerationEventListener {

	private int count;

	/**
	 * Creates a new object with a link to the schedule. Sets the standard
	 * number of generations to 10000.
	 * @param schedule
	 */
	public GenerationCount(Schedule schedule) {
		super(schedule);
		count = 10000;
	}

	/**
	 * @return "generation count criterion".
	 */
	public String getName() {
		return "Generation Count";
	}

	/**
	 * @return "stops the algorithm after count many generations".
	 */
	public String getDescription() {
		return "Stops a run after the specified number of generations.";
	}

	/**
	 * The property count defines the number of generations after which the
	 * run is stopped.
	 * @param count the number generations after which the run is stopped.
	 */
	public void setPropertyCount(Integer count) {
		if (count.intValue() > 0) {
			this.count = count.intValue();
		}
	}

	/**
	 * @return the number generations after which the run is stopped.
	 */
	public Integer getPropertyCount() {
		return new Integer(count);
	}

	public String getShortDescriptionForCount() {
		return "Generations";
	}

	public String getLongDescriptionForCount() {
		return "The number of generations after which the run is to be stopped.";
	}

	/**
	* This method is called when a new generation is completely created. It
	* checks whether already count many generation have been created. If so it
	* stops the run. 
	*/
	public void generation(GenerationEvent evt) {
		if (evt.getNumber() >= count) {
			stopRun();
		}
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
	}
}
