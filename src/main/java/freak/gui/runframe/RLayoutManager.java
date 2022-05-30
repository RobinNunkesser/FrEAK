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

import java.awt.*;

/**
 * Special LayoutManager to display the blinking R in the upper right corner.
 * @author  Stefan, Andrea
 */
class RLayoutManager implements LayoutManager {
	private LayoutManager delegate;
	private ReplayPanel replayPanel;

	public RLayoutManager(LayoutManager delegate, ReplayPanel replayPanel) {
		this.delegate = delegate;
		this.replayPanel = replayPanel;
	}

	public void layoutContainer(Container container) {
		if (delegate != null) {
			delegate.layoutContainer(container);
		}

		Dimension space = container.getSize();
		Dimension needed = replayPanel.getPreferredSize();
		replayPanel.setBounds(space.width - needed.width - 20, 20, needed.width, needed.height);
	}

	public void addLayoutComponent(String arg0, Component arg1) {
		if (delegate != null) {
			delegate.addLayoutComponent(arg0, arg1);
		}
	}

	public Dimension minimumLayoutSize(Container arg0) {
		if (delegate != null) {
			return delegate.minimumLayoutSize(arg0);
		} else {
			return null;
		}
	}

	public Dimension preferredLayoutSize(Container arg0) {
		if (delegate != null) {
			return delegate.preferredLayoutSize(arg0);
		} else {
			return null;
		}
	}

	public void removeLayoutComponent(Component arg0) {
		if (delegate != null) {
			delegate.removeLayoutComponent(arg0);
		}
	}
}
