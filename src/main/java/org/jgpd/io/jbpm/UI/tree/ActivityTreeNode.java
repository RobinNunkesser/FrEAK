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
import org.jgpd.io.jbpm.definition.impl.ActionImpl;
import org.jgpd.io.jbpm.definition.impl.ActivityStateImpl;
import org.jgpd.io.jbpm.definition.impl.AssignmentImpl;
import org.jgraph.pad.resources.ImageLoader;

/**
 * @author bensond
 *
 */
public class ActivityTreeNode extends JGpdTreeNode
{
	private static ImageIcon activityIcon = ImageLoader.getImageIcon("cog.gif");
	
	public ActivityTreeNode ( Object cell, JTree tree  )
	{
		super( cell, tree, true );
		
		ActivityStateImpl jbpmCell = (ActivityStateImpl)cell;
		
		// Actions subtree
		ActionsTreeNode actionsNode = new ActionsTreeNode(cell, tree);
		this.add(actionsNode);
		
		LinkedList actions = jbpmCell.getActions();
		
		Iterator iter = actions.iterator();
		while ( iter.hasNext() )
		{
			ActionImpl action = (ActionImpl) iter.next();
			ActionTreeNode actionNode = new ActionTreeNode(action, tree);
			actionsNode.add(actionNode);
		}

		// Fields subtree
		FieldsTreeNode fieldsNode = new FieldsTreeNode(cell, tree);
		this.add(fieldsNode);
		
		AssignmentImpl assignment = jbpmCell.getAssignment();
		if ( assignment != null )
		{
			AssignmentTreeNode assignmentNode = new AssignmentTreeNode( assignment, tree );
			this.add(assignmentNode);
		}
	}

	public JPopupMenu createPopup()
	{
		JPopupMenu menu = new JPopupMenu();
		
		ActivityStateImpl actNode = (ActivityStateImpl)getUserObject();
		AssignmentImpl assignment = actNode.getAssignment();
		if ( assignment != null )
		{
			Action popItem = new AbstractAction(
										"Create Assignment Handler",
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
						if ( actNode.createAssignment() == true )
						{
							AssignmentTreeNode newNode =
										new AssignmentTreeNode(actNode.getAssignment(), tree);
							treeNode.add(newNode);
							((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(treeNode);
							
							path = path.pathByAddingChild(newNode);
							tree.scrollPathToVisible(path);
							tree.startEditingAtPath(path);
						}
					}
				}
			};
			menu.add(popItem);		
		}

		// FIXME add option to delete assignment handler if one exists, else
		// add option to add assignment handler if one doesn't exist
		return menu;
	}

	public ImageIcon getIcon()
	{
		return ActivityTreeNode.activityIcon;
	}
}
