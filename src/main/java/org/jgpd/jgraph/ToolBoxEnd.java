/*
 *
 * Copyright (C) 2003 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgpd.jgraph;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JToggleButton;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPBarFactory;
import org.jgraph.pad.actions.AbstractActionDefault;;


public class ToolBoxEnd extends AbstractActionDefault {

	/**
	 * Constructor for ToolBoxEnd.
	 * @param graphpad
	 */
	public ToolBoxEnd(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	}
	/**
	 * @see org.jgraph.pad.actions.AbstractActionDefault#getToolComponent(String)
	 */
	protected Component getToolComponent(String actionCommand) {
		JToggleButton button = graphpad.getMarqueeHandler().getButtonEnd();
		GPBarFactory.fillToolbarButton(
					button,
					getName(),
					actionCommand);
		return button;
	}
	/**
	 *
	 */
	public void update() {
		super.update();
		graphpad.getMarqueeHandler().getButtonEnd().setEnabled(isEnabled());
	}

}
