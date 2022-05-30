/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport;

import java.io.Serializable;

import freak.core.control.Schedule;

/**
 * The interface for all modules. Modules are components that can be selected 
 * by the user and therefore they provide a name and a textual description that
 * can be displayed by the GUI. 
 * 
 * @author Dirk
 */
public interface Module extends Serializable {
	/**
	 * Returns the name of the module.
	 * 
	 * @return the name of the module.
	 */
	public String getName();

	/**
	 * Returns a textual description of the module and its functionality that
	 * can be displayed by the GUI.
	 * 
	 * @return a textual description of the module. 
	 */
	public String getDescription();

	/**
	 * This method adds every <code>Event</code> the Module wants to register
	 * itself at to the global <code>EventController</code> in the <code>Schedule</code>.
	 * Event-registration and removement from event queues is managed by this
	 * <code>EventController</code>.
	 * <code>createEvents</code> creates static as well as non-static events.
	 */
	public void createEvents();
	
	/**
	 * Initializes the module after initialization or when the schedule is 
	 * changed.
	 * <code>initialize</code> is called after the module's instantiation and 
	 * every time a relevant module in the current schedule is re-configured or 
	 * exchanged by another module. 
	 */
	public void initialize();
	
	/**
	 * Tests if the module can run with the specified schedule.
	 * 
	 * @param schedule the schedule to be tested.
	 * @throws UnsupportedEnvironmentException if the module cannot run with the specified schedule.
	 */
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException;
	
}
