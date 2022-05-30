/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.gui.runframe;

import java.awt.event.*;

import javax.swing.*;

/**
 * Brings the frame back to front.
 * 
 * @author Dimo, Stefan
 */
public class InternalFrameActionListener implements ActionListener {
	private JInternalFrame frame;

	/** Creates a new instance of InternalFrameAL */
	public InternalFrameActionListener(JInternalFrame frame) {
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			frame.setIcon(false);
			frame.setSelected(true);
			frame.setVisible(true);
			frame.moveToFront();
		} catch (java.beans.PropertyVetoException exc) {
			throw new RuntimeException(exc);
		}
	}
}
