/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.event;

/**
 * @see freak.core.event.ScheduleEvent
 * 
 * @author Stefan
 */
public interface ScheduleEventListener extends EventListener {
	/**
	 * The Schedule was started for the first time.
	 */
	void scheduleStarted(ScheduleEvent evt);
	/**
	 * The Schedule was started, after the user edited it. Note, that this is
	 * always true on the first start.
	 */
	void scheduleEdited(ScheduleEvent evt);
}
