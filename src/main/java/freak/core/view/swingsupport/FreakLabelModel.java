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
 * A freak model for JLabels.
 * @author  Stefan
 */
public class FreakLabelModel implements FreakSwingModel {
	private transient JLabel label;

	private String text;

	/**
	 * Link the text of the JLabel view to this model. Thelabel will be updated
	 * once immidiately and after that on every call to flush.
	 */
	public void setView(JLabel view) {
		label = view;
		flush();
	}

	/**
	 * @param text  the text to set
	 * @uml.property  name="text"
	 */
	public void setText(String text) {
		this.text = text;
	}

	public void flush() {
		if (label == null) return;

		label.setText(text);
	}
}
