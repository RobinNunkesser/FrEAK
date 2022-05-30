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

import java.util.Hashtable;
import javax.swing.*;

/**
 * A FreakModel to use JSliders.
 * @author  Stefan, Dirk
 */
public class FreakSliderModel implements FreakSwingModel {
	private transient JSlider slider;

	private boolean programmaticalChange;

	private int value;
	private int minimum;
	private int maximum;
	private boolean autoTicks;
	
	/**
	 * Link the values of the JSlider view to this model. The slider will be
	 * adapted once immidiately and after that on every call to flush.
	 */
	public void setView(JSlider view) {
		this.slider = view;
		flush();
	}

	/**
	 * Sets the position of the knob. Maximum and minimum are not adapted automatically.
	 * @uml.property  name="value"
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Sets the value of the left border. Value and maximum are not adapted automatically.
	 * @uml.property  name="minimum"
	 */
	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

   /**
 * Sets the value of the right border. Value and minimum are not adapted automatically.
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
	
	/**
	 * @param autoTicks  the autoTicks to set
	 * @uml.property  name="autoTicks"
	 */
	public void setAutoTicks(boolean b) {
		autoTicks = b;
	}

	
	public void flush() {
		if (slider == null) return;
		
		if (slider.getValueIsAdjusting()) {
			UpdateManager.markDirty(this);
			return;
		}

		programmaticalChange = true;


		if (autoTicks) {
			slider.setPaintTicks(false);
			slider.setPaintLabels(false);
				
			slider.getModel().setMinimum(minimum);
			slider.getModel().setMaximum(maximum);
			slider.getModel().setValue(value);
			createTicks();
			
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);	
		} else {
			slider.setMinimum(minimum);
			slider.setMaximum(maximum);
			slider.setValue(value);
		}
		
		programmaticalChange = false;
	}
	
	private void createTicks() {
		int n = maximum;
		int k = 1;
		while (n > 9) {
			n = n / 5;
			k = k * 5;
			if (n > 9) {
				n = n / 2;
				k = k * 2;
			}
		}
		
		int lastMajorLabel = maximum - ((maximum - minimum) % k);
		if (slider.getMajorTickSpacing() != k || (slider.getLabelTable().get(new Integer(lastMajorLabel)) == null)) {
			slider.setMinorTickSpacing(Math.max(1, k / 10));
			slider.setMajorTickSpacing(k);
			Hashtable labelTable = slider.createStandardLabels(k , minimum);
			slider.setLabelTable(labelTable);
		}
	}
}
