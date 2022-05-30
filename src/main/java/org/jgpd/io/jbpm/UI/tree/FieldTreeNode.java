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
import org.jgpd.io.jbpm.definition.impl.FieldImpl;
import org.jgpd.io.jbpm.definition.impl.StateImpl;
import org.jgraph.pad.resources.ImageLoader;

/**
 * @author bensond
 *
 * Another node
 */
public class FieldTreeNode extends JGpdTreeNode {

	private static ImageIcon fieldIcon = ImageLoader.getImageIcon("fieldIcon.gif");
	
	/**
	 * @param cell
	 * @param tree
	 * @param allowsChildren
	 */
	public FieldTreeNode( Object cell, JTree tree )
	{
		super(cell, tree, false);
		// TODO Auto-generated constructor stub
	}

	public JPopupMenu createPopup()
	{
		JPopupMenu menu = new JPopupMenu();
		
		Action popItem1 = new AbstractAction("Delete Field", new ImageIcon("delete_icon.gif"))
		{
			public void actionPerformed(ActionEvent e)
			{
				tree.repaint();
				TreePath path = tree.getSelectionPath();

				JGpdTreeNode treeNode = (JGpdTreeNode)path.getLastPathComponent();
				FieldImpl fieldObject = (FieldImpl)treeNode.getUserObject();
				if (fieldObject != null)
				{
					if (JOptionPane.showConfirmDialog(tree,
						"Do you want to delete this field?",
						"Confirm Field Delete", JOptionPane.YES_NO_OPTION)
							!= JOptionPane.YES_OPTION)
					{
						// exit if user does not confirm delete
						return;
					}

					JGpdTreeNode parent = (JGpdTreeNode)treeNode.getParent();
					StateImpl parentObject = (StateImpl)parent.getUserObject();
					parentObject.removeField(fieldObject);
					
					treeNode.removeFromParent();
					((DefaultTreeModel)(tree.getModel())).nodeStructureChanged(parent);
				}
			}
		};
		
		menu.add(popItem1);
		
		return menu;
	}

	public ImageIcon getIcon()
	{
		return FieldTreeNode.fieldIcon;
	}
}
