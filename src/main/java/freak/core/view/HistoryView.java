/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.view;

import freak.core.control.*;
import freak.core.modulesupport.*;

/**
 * An abstract superclass for all views that display data for the last  
 * <code>numberOfDisplayedValues</code> values.
 * 
 * @author Dirk, Stefan
 */
public abstract class HistoryView extends AbstractView implements Configurable {

	protected int numberOfDisplayedValues;

	public HistoryView(Schedule schedule) {
		super(schedule);
		numberOfDisplayedValues = 0;
	}

	/**
	 * Sets the maximum number of displayed values.
	 * 
	 * @param number use zero to specify unlimited storage
	 */
	public void setPropertyNumberOfDisplayedValues(Integer number) {
		numberOfDisplayedValues = Math.max(number.intValue(), 0);
	}

	/**
	 * Returns the maximum number of displayed values.
	 * 
	 * @return zero represents an unlimited number of values
	 */
	public Integer getPropertyNumberOfDisplayedValues() {
		return new Integer(numberOfDisplayedValues);
	}

	public String getShortDescriptionForNumberOfDisplayedValues() {
		return "Displayed values";
	}

	public String getLongDescriptionForNumberOfDisplayedValues() {
		return "The number of values displayed by the view. Set to 0 to allow unlimited storage.";
	}
}
