/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.stoppingcriterion;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.modulesupport.*;

/**
 * This class can be used as abstract superclass of all stopping criteria which
 * are checked each time a new generation is created. It provides a property
 * which specifies how often the stopping criterion is checked. If the
 * evalutation of a stopping criterion is non trivial and needs some running
 * time it can be desirable not to check it after every generation.
 * 
 * @author Heiko
 */
public abstract class AbstractGenerationStoppingCriterion extends AbstractStoppingCriterion implements Configurable, GenerationEventListener {

	private int numGen;

	/**
	 * Creates a new object with a reference to the schedule.
	 * @param schedule
	 */
	public AbstractGenerationStoppingCriterion(Schedule schedule) {
		super(schedule);
		numGen = 1;
	}

	public void setPropertyNumberGenerations(Integer num) {
		if (num.intValue() > 0) {
			numGen = num.intValue();
		}
	}

	public Integer getPropertyNumberGenerations() {
		return new Integer(numGen);
	}

	public String getShortDescriptionForNumberGenerations() {
		return "Period of checks";
	}

	public String getLongDescriptionForNumberGenerations() {
		return "The period specifying when the stopping criterion is checked in generations.";
	}

	public void generation(GenerationEvent evt) {
		if ((evt.getNumber() % numGen) == 0) {
			checkCriterion(evt);
		}
	}

	public void createEvents() {
		getSchedule().getEventController().addEvent(this, GenerationEvent.class, getSchedule());
	}

	abstract protected void checkCriterion(GenerationEvent evt);
}
