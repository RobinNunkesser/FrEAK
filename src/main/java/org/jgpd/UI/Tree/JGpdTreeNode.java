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

package org.jgpd.UI.Tree;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class JGpdTreeNode extends DefaultMutableTreeNode
{
	protected JTree tree;
	
	public JGpdTreeNode( Object cell, JTree tree, boolean allowsChildren )
	{
		super ( cell, allowsChildren );
		this.tree = tree;
	}

	public JPopupMenu createPopup()
	{
		// pop nothing up.
		return null;
	}
	
	public void moveNodeDown()
	{
		JGpdTreeNode parent = (JGpdTreeNode)this.getParent();
		int index = parent.getIndex(this);
		if ( index+1 < this.getParent().getChildCount() )
		{
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			model.removeNodeFromParent(this);
			model.insertNodeInto(this, parent, index+1);
		}
	}

	public void moveNodeUp()
	{
		JGpdTreeNode parent = (JGpdTreeNode)this.getParent();
		int index = parent.getIndex(this);
		if ( index!=0 )
		{
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			model.removeNodeFromParent(this);
			model.insertNodeInto(this, parent, index-1);
		}
	}
	
	public ImageIcon getIcon()
	{
		return null;
	}
}
