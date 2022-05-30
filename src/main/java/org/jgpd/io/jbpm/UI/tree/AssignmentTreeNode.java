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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jgpd.UI.Tree.JGpdTreeNode;
import org.jgpd.io.jbpm.definition.impl.ActivityStateImpl;
import org.jgpd.io.jbpm.definition.impl.AssignmentImpl;
import org.jgpd.io.jbpm.definition.impl.ParametersImpl;
import org.jgraph.pad.resources.ImageLoader;

/**
 * @author bensond
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AssignmentTreeNode extends JGpdTreeNode {

	private static ImageIcon assignmentIcon = ImageLoader.getImageIcon("assignment.gif");
	
	/**
	 * @param cell
	 * @param allowsChildren
	 */
	public AssignmentTreeNode(Object cell, JTree tree)
	{
		super( cell, tree, false );

		ParametersImpl params = ((AssignmentImpl)cell).getParameters();
		ParametersTreeNode paramsNode = new ParametersTreeNode(params, tree);
		this.add(paramsNode);
	}

	public JPopupMenu createPopup()
	{
		JPopupMenu menu = new JPopupMenu();
		
		Action popItem = new AbstractAction
				("Delete Assignment Handler", new ImageIcon("delete_icon.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				tree.repaint();
				TreePath path = tree.getSelectionPath();

				JGpdTreeNode treeNode = (JGpdTreeNode)path.getLastPathComponent();
				AssignmentImpl assignment = (AssignmentImpl)treeNode.getUserObject();
				if (assignment != null)
				{
					// FIXME this should be centred about the main
					// frame, not the tree
					if (JOptionPane.showConfirmDialog(tree,
							"Do you want to delete this assignment handler?",
							"Confirm Action Delete", JOptionPane.YES_NO_OPTION)
							!= JOptionPane.YES_OPTION)
					{
						// exit if user does not confirm delete
						return;
					}

					JGpdTreeNode parent = (JGpdTreeNode)treeNode.getParent();
					ActivityStateImpl parentObject = 
						(ActivityStateImpl)parent.getUserObject();
					parentObject.deleteAssignment();
					
					treeNode.removeFromParent();
					((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(parent);
				}
			}
		};
		
		menu.add(popItem);
		
		return menu;
	}

	public ImageIcon getIcon()
	{
		return AssignmentTreeNode.assignmentIcon;
	}
}
