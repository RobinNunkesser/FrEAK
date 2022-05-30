/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport;

import freak.core.control.*;

/**
 * An abstract superclass for all modules except operators and parameter controllers, which are direct subclasses of AbstractBasicModule. Provides standard implementations for common methods of most modules.
 * @author  Stefan
 */
public abstract class AbstractModule extends AbstractBasicModule {
	protected Schedule schedule;
	
	/**
	 * Establishes an uplink to the Schedule containing this module.
	 */
	public AbstractModule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	/**
	 * Returns the Schedule containing this module.
	 * @uml.property  name="schedule"
	 */
	public Schedule getSchedule() {
		return schedule;
	}
}
