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
public interface ScheduleEventSource extends EventSource {
	void addScheduleEventListener(ScheduleEventListener l);
	void removeScheduleEventListener(ScheduleEventListener l);
}
