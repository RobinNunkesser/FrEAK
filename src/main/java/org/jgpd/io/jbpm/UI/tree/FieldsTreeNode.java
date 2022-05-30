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

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jgpd.UI.Tree.JGpdTreeNode;
import org.jgpd.io.jbpm.definition.impl.ActivityStateImpl;
import org.jgpd.io.jbpm.definition.impl.FieldImpl;
import org.jgraph.pad.resources.ImageLoader;

/**
 * @author bensond
 *
 */
public class FieldsTreeNode extends JGpdTreeNode
{
	public FieldsTreeNode ( Object cell, JTree tree  )
	{
		super( cell, tree, true );
		
		ActivityStateImpl jbpmCell = (ActivityStateImpl)cell;
		
		LinkedList fields = jbpmCell.getFields();
		
		Iterator iter = fields.iterator();
		while ( iter.hasNext() )
		{
			FieldImpl field = (FieldImpl) iter.next();
			FieldTreeNode fieldNode = new FieldTreeNode(field, tree);
			this.add(fieldNode);
		}
	}

	public JPopupMenu createPopup()
	{
		JPopupMenu menu = new JPopupMenu();
		
		Action popItem = new AbstractAction(
									"Create Field",
									ImageLoader.getImageIcon("cog.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				tree.repaint();
				TreePath path = tree.getSelectionPath();

				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
				ActivityStateImpl actNode = (ActivityStateImpl)treeNode.getUserObject();
				if (actNode != null)
				{
					FieldImpl field = actNode.createNewField();
					FieldTreeNode newNode = new FieldTreeNode(field, tree);
					treeNode.add(newNode);
					((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(treeNode);
					
					path = path.pathByAddingChild(newNode);
					tree.scrollPathToVisible(path);
					tree.startEditingAtPath(path);
				}
			}
		};
		menu.add(popItem);

		return menu;
	}

	public ImageIcon getIcon()
	{
		return ActionTreeNode.actionIcon;
	}
}
