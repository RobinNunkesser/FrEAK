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
import org.jgpd.io.jbpm.definition.impl.ActionImpl;
import org.jgpd.io.jbpm.definition.impl.DefinitionObjectImpl;
import org.jgraph.pad.resources.ImageLoader;

/**
 * @author bensond
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ActionTreeNode extends JGpdTreeNode {

	public static ImageIcon actionIcon = ImageLoader.getImageIcon("cog.gif");
	
	/**
	 * @param cell
	 * @param allowsChildren
	 */
	public ActionTreeNode(Object cell, JTree tree)
	{
		super( cell, tree, false );
		// TODO Auto-generated constructor stub
	}

	public JPopupMenu createPopup()
	{
		JPopupMenu menu = new JPopupMenu();
		
		Action popItem1 = new AbstractAction("Move Action Up", new ImageIcon("up_icon.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				tree.repaint();
				TreePath path = tree.getSelectionPath();

				JGpdTreeNode treeNode = (JGpdTreeNode)path.getLastPathComponent();
				ActionImpl actObject = (ActionImpl)treeNode.getUserObject();
				if (actObject != null)
				{
					// Move the node up one in the tree
					treeNode.moveNodeUp();
					// Move the action up one in the parent jbpm object
					JGpdTreeNode parent = (JGpdTreeNode)treeNode.getParent();
					DefinitionObjectImpl parentObject = 
						(DefinitionObjectImpl)parent.getUserObject();
					parentObject.moveActionUp(actObject);

					((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(treeNode);
					
					path = path.pathByAddingChild(treeNode);
					tree.scrollPathToVisible(path);
					tree.startEditingAtPath(path);
				}
			}
		};

		Action popItem2 = new AbstractAction("Move Action Down", new ImageIcon("down_icon.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				tree.repaint();
				TreePath path = tree.getSelectionPath();

				JGpdTreeNode treeNode = (JGpdTreeNode)path.getLastPathComponent();
				ActionImpl actObject = (ActionImpl)treeNode.getUserObject();
				if (actObject != null)
				{
					// Move the node up one in the tree
					treeNode.moveNodeDown();
					// Move the action up one in the parent jbpm object
					JGpdTreeNode parent = (JGpdTreeNode)treeNode.getParent();
					DefinitionObjectImpl parentObject = 
						(DefinitionObjectImpl)parent.getUserObject();
					parentObject.moveActionDown(actObject);

					((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(treeNode);
					
					path = path.pathByAddingChild(treeNode);
					tree.scrollPathToVisible(path);
					tree.startEditingAtPath(path);
				}
			}
		};

		Action popItem3 = new AbstractAction("Delete Action", new ImageIcon("delete_icon.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				tree.repaint();
				TreePath path = tree.getSelectionPath();

				JGpdTreeNode treeNode = (JGpdTreeNode)path.getLastPathComponent();
				ActionImpl actObject = (ActionImpl)treeNode.getUserObject();
				if (actObject != null)
				{
					if (JOptionPane.showConfirmDialog(tree,
						"Do you want to delete this action?",
						"Confirm Action Delete", JOptionPane.YES_NO_OPTION)
							!= JOptionPane.YES_OPTION)
					{
						// exit if user does not confirm delete
						return;
					}

					JGpdTreeNode parent = (JGpdTreeNode)treeNode.getParent();
					DefinitionObjectImpl parentObject = 
						(DefinitionObjectImpl)parent.getUserObject();
					parentObject.removeAction(actObject);
					
					treeNode.removeFromParent();
					((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(parent);
				}
			}
		};
		
		// Only add a move action up action if this action is not
		// the first action
		JGpdTreeNode parent = (JGpdTreeNode)this.getParent();
		int index = parent.getIndex(this);
		if ( index!=0 )
		{
			menu.add(popItem1);
		}

		// Only add a move action down action if this action is not
		// the last action
		if ( index+1 < this.getParent().getChildCount() )
		{
			menu.add(popItem2);
		}

		menu.add(popItem3);
		
		return menu;
	}

	public ImageIcon getIcon()
	{
		return ActionTreeNode.actionIcon;
	}
}
