/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.view.swingsupport;

import javax.swing.*;

/**
 * A freak model to use JSpinners. The model is restricted to integer data for simplicity.
 * @author  Stefan
 */
public class FreakSpinnerModel implements FreakSwingModel {
	private transient SpinnerNumberModel readCopy;
	
	private int value;
	private int minimum;
	private int maximum;

	private boolean programmaticalChange;

	/**
	 * Link the values of the JSpinner view to this model. The spinner will be
	 * adapted once immidiately and after that on every call to flush.
	 */
	public void setView(JSpinner view) {
		readCopy = new SpinnerNumberModel();
		flush();

		view.setModel(readCopy);
	}

	/**
	 * Sets the current value. Maximum and minimum are not adapted automatically.
	 * @uml.property  name="value"
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Sets the minimal value that will be accepted as user inpud. Value and maximum are not adapted automatically.
	 * @uml.property  name="minimum"
	 */
	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	/**
	 * Sets the maximal value that will be accepted as user inpud. Value and maximum are not adapted automatically.
	 * @uml.property  name="maximum"
	 */
	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	/**
	 * @return  the programmaticalChange
	 * @uml.property  name="programmaticalChange"
	 */
	public boolean isProgrammaticalChange() {
		return programmaticalChange;
	}

	public void flush() {
		if (readCopy == null) return;

		programmaticalChange = true;
		
		readCopy.setMinimum(new Integer(minimum));
		readCopy.setMaximum(new Integer(maximum));
		readCopy.setValue(new Integer(value));
		
		programmaticalChange = false;
	}
}
