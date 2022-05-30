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

import javax.swing.border.*;

/**
 * A freak model for titled borders.
 * @author  Stefan
 */
public class FreakTitledBorderModel implements FreakSwingModel {
	private transient TitledBorder border;

	private String title;

	/**
	 * Link the text of the JTextArea view to this model. The text area will be
	 * adapted once immidiately and after that on every call to flush.
	 */
	public void setView(TitledBorder view) {
		border = view;
		flush();
	}

	/**
	 * @param title  the title to set
	 * @uml.property  name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public void flush() {
		if (border == null)
			return;

		border.setTitle(title);
	}
}
