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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.jgpd.io.ModelExportInterface;
import org.jgraph.GPGraphpad;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;

public class JGpdTree extends JPanel implements GraphSelectionListener
{
	protected GPGraph graph;
	protected GPDocument document;

	// Store last cell selected as if the same cell is selected twice
	// the new selection event still fires
	protected Object lastCellSelected = null;
	// Also store userObject associated with this cell
	protected Object currentUserObject = null;

	protected DefaultMutableTreeNode rootNode;
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	protected TreePath lastPath;
	protected Action expandAction;
	
	public JGpdTree(GPGraph g, GPDocument d, GPGraphpad gp)
	{
		super(new GridLayout(1,0));
        
        graph = g;
        document = d;
        
		graph.getSelectionModel().addGraphSelectionListener(this);

		rootNode = new DefaultMutableTreeNode("Process");
		treeModel = new DefaultTreeModel(rootNode);
		treeModel.addTreeModelListener(new MyTreeModelListener());

		tree = new JTree(treeModel) {
			public boolean isPathEditable(TreePath path) {
				if (path == null || path.getPathCount() < 3)
				{
					return false;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				
				if (node == null)
				{
					return false;
				}
				
				if ( node instanceof JGpdTreeNode )
				{
					return true; // FIXME, we can be selective by asking
								 // the node itself if we can edit
				}

				return false;
			}
		};
		
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		JGpdTreeNodeRenderer renderer = new JGpdTreeNodeRenderer();
		tree.setCellRenderer(renderer);
		
		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane);
		
		tree.addMouseListener(new PopupMenu());
	}

	/** Remove all nodes except the root node. */
	public void clear() {
		rootNode.removeAllChildren();
		treeModel.reload();
	}

	/** Remove the currently selected node. */
	public void removeCurrentNode() {
		TreePath currentSelection = tree.getSelectionPath();
		if (currentSelection != null) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
						 (currentSelection.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
			if (parent != null) {
				treeModel.removeNodeFromParent(currentNode);
				return;
			}
		} 

		// Either there was no selection, or the root was selected.
		// FIXME
	}

	/** DynamicTree factory that returns instance with small inset as a buffer.*/
	public static JPanel createTreePanel(GPGraph g,
										 GPDocument d,
										 GPGraphpad gp)
	{
		JPanel panelWithInternalOffset = new JPanel();
		panelWithInternalOffset.setLayout(new BorderLayout());
		panelWithInternalOffset.setBorder(new MatteBorder(2,2,2,2,Color.WHITE));
		panelWithInternalOffset.add(new JGpdTree(g,d,gp), BorderLayout.CENTER);
		return panelWithInternalOffset;
	}

	public static JTree getTree(JPanel treePanel)
	{
		int components = treePanel.getComponentCount();
		if (components == 1)
		{
			JGpdTree innerPanel = (JGpdTree)treePanel.getComponent(0);
			return innerPanel.getTree();
		}
		
		return null;
	}

	/** Add child to the currently selected node. */
	public DefaultMutableTreeNode addObject(Object child) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = tree.getSelectionPath();

		if (parentPath == null) {
			parentNode = rootNode;
		} else {
			parentNode = (DefaultMutableTreeNode)
						 (parentPath.getLastPathComponent());
		}

		return addObject(parentNode, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
											Object child) {
		return addObject(parent, child, false);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
											Object child, 
											boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = 
				new DefaultMutableTreeNode(child);

		if (parent == null) {
			parent = rootNode;
		}

		treeModel.insertNodeInto(childNode, parent, 
								 parent.getChildCount());

		//Make sure the user can see the new node.
		if (shouldBeVisible) {
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}

	class MyTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode)
					 (e.getTreePath().getLastPathComponent());

			/*
			 * If the event lists children, then the changed
			 * node is the child of the node we've already
			 * gotten.  Otherwise, the changed node and the
			 * specified node are the same.
			 */
			try {
				int index = e.getChildIndices()[0];
				node = (DefaultMutableTreeNode)
					   (node.getChildAt(index));
			} catch (NullPointerException exc) {}

		}
		public void treeNodesInserted(TreeModelEvent e) {
		}
		public void treeNodesRemoved(TreeModelEvent e) {
		}
		public void treeStructureChanged(TreeModelEvent e) {
		}
	}
	

	class PopupMenu extends MouseAdapter
	{
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3)
			{
				int x = e.getX();
				int y = e.getY();
				lastPath = tree.getPathForLocation(x, y);
				if ( (lastPath == null) || (lastPath.getPathCount() < 2) )
				{
					// Cannot act upon the root node
					return;
				}

				tree.setSelectionPath(lastPath);
				tree.scrollPathToVisible(lastPath);
				
				// Based on whichever is the currently selected node's user object,
				// build the appropriate popup menu
				DefaultMutableTreeNode node =
						(DefaultMutableTreeNode)lastPath.getLastPathComponent();
				
				if ( node instanceof JGpdTreeNode )
				{
					JPopupMenu menu = ((JGpdTreeNode)node).createPopup();
					if ( menu != null )
					{
						// null is a legal return to indicate no popup
						menu.show(tree, x, y);
					}
				}
			}
		}
	}
	
	
	//
	// GraphModelListener
	//

	/**
	 * Update the panel when the selection changes
	 */
	public void valueChanged(GraphSelectionEvent e)
	{
		// Obtain reference to the selected cell
		DefaultGraphCell cell = getSelectedCell();
                
		// Send object hashCode to model interface to ask
		// for panel details if this panel is visible
		
		// FIXME todo if panel !visibile return
		if ( (document != null) && (cell != null) )
		{
			if ( cell.equals(lastCellSelected) )
			{
				// if you select the same cell twice
				// this function still fires twice
				// ignore subsequent fires
			}
			else
			{	
				lastCellSelected = cell;
				ModelExportInterface exportModel = document.getExportModel();
				if ( exportModel != null )
				{
					currentUserObject =
						((DefaultGraphCell)cell).getUserObject();
					
					// FIXME, get the tree for this node
					DefaultMutableTreeNode treeNode = exportModel.createTreeNode
															(currentUserObject, tree);

					this.clear();

					if ( treeNode != null )
					{
						rootNode.add(treeNode);
						treeModel.reload();
					}
				}
				else
				{
					lastCellSelected = null;
					clear();
					// FIXME todo log error of no export model available
				}
			}
		}
		else
		{
			lastCellSelected = null;
			clear();
		}		
	}
	
	/**
	 * Returns the currently selected graph cell. Returns null if
	 * none are selected or if there is a multiple selection
	 */
	protected DefaultGraphCell getSelectedCell()
	{
		if (graph != null)
		{
			// Check only one object is selected
			if (graph.getSelectionCount() == 1)
			{
				// Obtain reference to the selected object
				return ( (DefaultGraphCell)graph.getSelectionCell() );
                
			}
			else
			{
				// > 1 cell selected
				return null;
			}
		}
		
		return null;
	}

	protected JTree getTree()
	{
		return tree;
	}
	
	public JComponent getMainComponent()
	{
		return tree;
	}
	
	class JGpdTreeNodeRenderer extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus)
		{
			super.getTreeCellRendererComponent(
					tree, value, sel, expanded, leaf, row, hasFocus);
			if ( value instanceof JGpdTreeNode )
			{
				JGpdTreeNode node = (JGpdTreeNode)value;
				ImageIcon icon = node.getIcon();
	
				if (icon != null)
				{
					setIcon(icon);
				}
			}
			
			return this;
		}
	}
}