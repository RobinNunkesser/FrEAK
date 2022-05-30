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
import org.jgpd.io.jbpm.definition.impl.DecisionImpl;
import org.jgpd.io.utils.FilePackage;
import org.jgraph.pad.resources.ImageLoader;


/**
 * @author bensond
 *
 */
public class DecisionTreeNode extends JGpdTreeNode {

	private static ImageIcon decisionIcon = ImageLoader.getImageIcon("decisionIcon.gif");
	
	/**
	 * @param cell
	 */
	public DecisionTreeNode(Object cell, JTree tree)
	{
		super( cell , tree, true );

		DecisionImpl jbpmCell = (DecisionImpl)cell;
		
		// Parameters subtree
		// Note we create the parameters tree node with this decision as
		// the user object.
		ParametersTreeNode paramsNode =
				new ParametersTreeNode( jbpmCell.getParameters(), tree );
		this.add(paramsNode);

		// Handler subtree
		FilePackage handler = jbpmCell.getHandler();
		HandlerTreeNode handlerNode = new HandlerTreeNode( handler, tree );
		this.add(handlerNode);
	}

	public JPopupMenu createPopup()
	{
		return null;
	}

	public ImageIcon getIcon()
	{
		return DecisionTreeNode.decisionIcon;
	}
}
