/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.view.swingsupport;

import java.awt.event.*;

import javax.swing.*;

/**
 * Automatically sets the specified scrollbar to the maximum with the following
 * strategy: if the user drags the scrollbar to the maximum manually,
 * it is kept at maximum even when the scrollpane changes size.
 * Else, the scrollbar is left at its current position.  
 * 
 * @author Dirk
 */
public class FreakScrollBarAdjustmentListener implements AdjustmentListener {

	private JScrollBar scrollBar;
	private boolean adjustScrollBar;

	// last values are maintained to detect changes
	private int oldScrollBarMaximum;
	private int oldScrollBarExtent;

	/**
	 * Constructs a new <code>FreakScrollBarAdjustmentListener</code>.
	 * 
	 * @param scrollBar the scroll bar that is to be adjusted.
	 * @param defaultAdjusting the default value decides if adjusting is activated by default. 
	 */
	public FreakScrollBarAdjustmentListener(JScrollBar scrollBar, boolean defaultAdjusting) {
		this.scrollBar = scrollBar;
		adjustScrollBar = defaultAdjusting;
		oldScrollBarMaximum = scrollBar.getMaximum();
		oldScrollBarExtent = scrollBar.getModel().getExtent();
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (!scrollBar.isValid()) {
			return;
		}

		if (oldScrollBarExtent != scrollBar.getModel().getExtent()) {
			// case 1: the extent has changed due to modifications 
			// of the viewport; adjust is not changed.
		} else {
			if (oldScrollBarMaximum != scrollBar.getMaximum()) {
				// case 2: the maximum changed due to modifications
				// of the scroll client; adjust is not changed.
			} else {
				// case 3: the value changed only, perhaps due to direkt 
				// manipulation by the user. 
				// So we check if the knob is pulled to the maximum 
				adjustScrollBar = (e.getValue() + scrollBar.getModel().getExtent() == scrollBar.getMaximum());
			}
		}

		oldScrollBarMaximum = scrollBar.getMaximum();
		oldScrollBarExtent = scrollBar.getModel().getExtent();

		if (adjustScrollBar) {
			// scroll to maximum 
			scrollBar.setValue(scrollBar.getMaximum() - scrollBar.getModel().getExtent());
		}
	}

}
