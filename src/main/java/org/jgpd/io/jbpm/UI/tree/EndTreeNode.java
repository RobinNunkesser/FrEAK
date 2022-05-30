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

package org.jgpd.io.jbpm.UI.tree;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

import org.jgpd.UI.Tree.JGpdTreeNode;
import org.jgraph.pad.resources.ImageLoader;

/**
 * @author bensond
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EndTreeNode extends JGpdTreeNode {

	private static ImageIcon endIcon = ImageLoader.getImageIcon("endIcon.gif");
	
	/**
	 * @param cell
	 */
	public EndTreeNode(Object cell, JTree tree)
	{
		super( cell, tree, false );
		// TODO Auto-generated constructor stub
	}

	public JPopupMenu createPopup()
	{
		// No actions can be performed on an end node
		return null;
	}

	public ImageIcon getIcon()
	{
		return EndTreeNode.endIcon;
	}
}
