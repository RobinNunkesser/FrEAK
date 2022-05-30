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
import freak.core.observer.*;
import freak.core.view.View;
import freak.module.view.ScheduleView;

/**
 * Provides information about the current schedule.
 * 
 * @author Dirk
 */
public class ScheduleInformation extends AbstractObserver {

	/**
	 * Constructs a new <code>ScheduleInformation</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public ScheduleInformation(Schedule schedule) {
		super(schedule);
	}

	public Class getOutputDataType() {
		return Schedule.class;
	}

	public String getName() {
		return "Schedule Information";
	}

	public String getDescription() {
		return "An overview of the current schedule and its components.";
	}

	public void addView(View view) throws ObserverViewMismatchException {
		if (!(view instanceof ScheduleView)) throw new ObserverViewMismatchException("Schedule Information can only be combined with appropriate views.");
		super.addView(view);
	}

}
