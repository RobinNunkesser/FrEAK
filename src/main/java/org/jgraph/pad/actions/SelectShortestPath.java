/*
 * @(#)SelectShortestPath.java	1.2 01.02.2003
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

import org.jgraph.GPGraphpad;

/**
 * 
 * @author sven.luzar
 * @version 1.0
 *
 */
public class SelectShortestPath extends AbstractActionDefault {

	/**
	 * Constructor for SelectShortestPath.
	 * @param graphpad
	 */
	public SelectShortestPath(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object from = null, to = null;
		Object[] cells = getCurrentGraph().getSelectionCells();
		if (cells != null) {
			for (int i = cells.length - 1; i >= 0; i--) {
				if (getCurrentGraph().isVertex(cells[i])) {
					if (from == null)
						from = cells[i];
					else {
						to = cells[i];
						break;
					}
				}
			}
			if (from != null && to != null)
				getCurrentGraph().addSelectionCells(
					graphpad.getGraphTools().getShortestPath(
						getCurrentGraph(),
						from,
						to,
						null));
		}
	}

}
