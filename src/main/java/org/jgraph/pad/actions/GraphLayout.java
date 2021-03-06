/*
 * @(#)GraphLayout.java	1.2 02.02.2003
 *
 * Copyright (C) 2003 sven.luzar
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jgraph.pad.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.jgraph.GPGraphpad;

/**
 * 
 * @author sven.luzar
 * @version 1.0
 *
 */
public class GraphLayout extends AbstractActionCheckBox {

	/**
	 * Constructor for GraphLayout.
	 * @param graphpad
	 */
	public GraphLayout(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * Constructor for GraphLayout.
	 * @param graphpad
	 * @param name
	 */
	public GraphLayout(GPGraphpad graphpad, String name) {
		super(graphpad, name);
	}

	/**
	 * Constructor for GraphLayout.
	 * @param graphpad
	 * @param name
	 * @param icon
	 */
	public GraphLayout(GPGraphpad graphpad, String name, Icon icon) {
		super(graphpad, name, icon);
	}

	/**
	 * @see org.jgraph.pad.actions.AbstractActionToggle#isSelected(String)
	 */
	public boolean isSelected(String actionCommand) {
		if (getCurrentDocument() == null) return false;

		return getCurrentDocument().getTouch().isRunning();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
				if (getCurrentDocument().getTouch().isRunning()) //(layout.isRunning())
					getCurrentDocument().getTouch().stop();
				else
					getCurrentDocument().getTouch().start();
	}

}
