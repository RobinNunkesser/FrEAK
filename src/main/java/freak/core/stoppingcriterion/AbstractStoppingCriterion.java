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
import freak.core.modulesupport.*;

/**
 * This class represents an abstract stopping criterion. Some default
 * implementations of methods in the interface <code>Module</code> are given.
 * 
 * @author Heiko, Stefan
 */
public abstract class AbstractStoppingCriterion extends AbstractModule implements StoppingCriterion {

	public AbstractStoppingCriterion(Schedule schedule) {
		super(schedule);
	}

	/**
	 * This method can be called by subclasses in order to stop the run.
	 */
	protected void stopRun() {
		schedule.triggerStopCriterion();
	}
}
