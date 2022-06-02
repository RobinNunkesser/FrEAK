/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.gui.scheduleeditor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * This class renders tree elements of the observer-tree. It displays tooltips
 * for every <code>Observer</code>/<code>View</code> and shows an icon if the
 * node is configurable.
 * 
 * @author Oliver
 *
 */
public class ObserverTreeCellRenderer extends DefaultTreeCellRenderer {

	private final ImageIcon observerIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Find16.gif"));
	private final ImageIcon viewIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/PrintPreview16.gif"));

	/**
	 * Creates a new ObserverTreeCellRenderer
	 */
	public ObserverTreeCellRenderer() {
		setClosedIcon(observerIcon);
		setOpenIcon(observerIcon);
		setLeafIcon(viewIcon);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		if (node.getUserObject() != null) {
			ObserverTreeElement o = (ObserverTreeElement)node.getUserObject();
			setToolTipText(o.getDescription());
		}
		return this;
	}

}
