/*
 *
 *
 * Copyright (C) 2003 David Benson
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

package org.jgpd.io;

import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;


public interface ModelExportInterface {

	public Object createActivity();

	public Object createDecision();

	public Object createProcess();

	public Object createFork();

	public Object createJoin();

	public Object createStart();

	public Object createEnd();

	public void promptProcessProps(GPGraph graph);
	
	public void saveFile( 	GPGraphpad graphpad,
							GPDocument doc,
							URL filename,
							GPGraph gpGraph);
	
	public String exportModelXML(GPGraph graph);
	
	public JPanel createBottomPanel(GPGraph g,
									GPDocument d,
									GPGraphpad gp);
	
	public JPanel createSidePanel(GPGraph g,
								GPDocument d,
								GPGraphpad gp);
								
	public DefaultMutableTreeNode createTreeNode( Object cell , JTree tree );

	public JGpdTableModel createTableModel( Object cell );
	
	public void registerComponentListeners(JPanel sidePanel,
										   JPanel bottomPanel);
}