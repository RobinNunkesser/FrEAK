/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.runframe;

import freak.core.observer.*;
import freak.core.view.View;

import javax.swing.*;

/**
 * @author  Dirk, Dimo, Stefan
 */
class FreakInternalFrame extends JInternalFrame implements Comparable {

	public static final int DEFAULT_WIDTH = 400;
	public static final int DEFAULT_HEIGHT = 400;

	private View view;
	private int viewId;

	FreakInternalFrame() {
		super("", true, true, true, true);

		getContentPane().setLayout(new java.awt.BorderLayout());
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * @return  the viewId
	 * @uml.property  name="viewId"
	 */
	public int getViewId() {
		return viewId;
	}

	void setView(Observer observer, View view) {
		this.view = view;
		setTitle(observer.getName() + " - " + view.getName());
		setPanel(view.createPanel());
		viewId = view.getIdentificationNumber();
	}

	/**
	 * @return  the view
	 * @uml.property  name="view"
	 */
	View getView() {
		return view;
	}

	private void setPanel(JPanel panel) {
		getContentPane().removeAll();
		getContentPane().add(panel);
		revalidate();
	}

	public int compareTo(Object o) {
		if (!(o instanceof FreakInternalFrame))
			return 0;
		// two FreakInternalFrames are compared by the numbers of their views
		int diff = this.getViewId() - ((FreakInternalFrame)o).getViewId();
		return (diff == 0 ? 0 : (diff < 0 ? -1 : 1));
	}
}