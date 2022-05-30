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
import javax.swing.*;

public class ROverlayPane extends JLayeredPane {
	public ROverlayPane(JDesktopPane desktopPane, ReplayPanel replayPanel) {
		setLayout(new ROverlayPaneLayoutManager(desktopPane, replayPanel));
		add(desktopPane, DEFAULT_LAYER);
		add(replayPanel, POPUP_LAYER);
	}

	/**
	 * @author  nunkesser
	 */
	private static class ROverlayPaneLayoutManager implements LayoutManager {
		private JDesktopPane desktopPane;
		private ReplayPanel replayPanel;

		public ROverlayPaneLayoutManager(JDesktopPane desktopPane, ReplayPanel replayPanel) {
			this.desktopPane = desktopPane;
			this.replayPanel = replayPanel;
		}

		public void layoutContainer(Container container) {
			desktopPane.setSize(container.getSize());

			Dimension space = container.getSize();
			Dimension needed = replayPanel.getPreferredSize();
			replayPanel.setBounds(space.width - needed.width - 20, 10, needed.width, needed.height);
		}

		public void addLayoutComponent(String arg0, Component arg1) {
		}
		public void removeLayoutComponent(Component arg0) {
		}
		public Dimension preferredLayoutSize(Container arg0) {
			return new Dimension(0, 0);
		}
		public Dimension minimumLayoutSize(Container arg0) {
			return new Dimension(0, 0);
		}
	}
}
