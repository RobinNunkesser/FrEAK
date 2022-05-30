/*
 *
 * Copyright (C) 2004 David Benson
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

package org.jgpd.UI;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;

public abstract class JGpdPanel extends JPanel
{
	protected GPGraph graph;
	protected GPDocument document;
	protected GPGraphpad graphpad;

	protected boolean panelEnabled;
	// Store last cell selected as if the same cell is selected twice
	// the new selection event still fires
	protected Object lastCellSelected = null;
	// Also store userObject associated with this cell
	protected Object currentUserObject = null;
	protected JComponent listenedComponent;

	
	protected JGpdPanel(GPGraph g, GPDocument d, GPGraphpad gp)
	{
		graph = g;
		document = d;
		graphpad = gp;
	}

	public static JPanel getPanel(JPanel panel)
	{
		int components = panel.getComponentCount();
		if (components == 1)
		{
			JPanel innerPanel =
				(JPanel)panel.getComponent(0);
			return innerPanel;
		}
		
		return null;
	}

	public void paintChildren(Graphics g) {
		super.paintChildren(g);
	}

	public void setListenedComponent(JComponent comp)
	{
		listenedComponent = comp;
	}
	
	public JComponent getMainComponent()
	{
		return null;
	}
	
}