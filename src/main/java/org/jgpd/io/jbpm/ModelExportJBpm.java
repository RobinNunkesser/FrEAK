/*
 *
 * Copyright (C) 2003-2004 David Benson
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

package org.jgpd.io.jbpm;

import java.net.URL;

import org.jgpd.UI.JGpdPropertiesPanel;
import org.jgpd.UI.Tree.JGpdTree;
import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.ModelExportImpl;
import org.jgpd.io.jbpm.UI.tableModels.ActivityTableModel;
import org.jgpd.io.jbpm.UI.tableModels.DecisionTableModel;
import org.jgpd.io.jbpm.UI.tableModels.EndTableModel;
import org.jgpd.io.jbpm.UI.tableModels.ForkTableModel;
import org.jgpd.io.jbpm.UI.tableModels.HandlerTableModel;
import org.jgpd.io.jbpm.UI.tableModels.JoinTableModel;
import org.jgpd.io.jbpm.UI.tableModels.ParametersTableModel;
import org.jgpd.io.jbpm.UI.tableModels.StartTableModel;
import org.jgpd.io.jbpm.UI.tree.ActivityTreeNode;
import org.jgpd.io.jbpm.UI.tree.DecisionTreeNode;
import org.jgpd.io.jbpm.UI.tree.EndTreeNode;
import org.jgpd.io.jbpm.UI.tree.ForkTreeNode;
import org.jgpd.io.jbpm.UI.tree.JoinTreeNode;
import org.jgpd.io.jbpm.UI.tree.StartTreeNode;
import org.jgpd.io.jbpm.definition.impl.*;
import org.jgpd.io.jbpm.io.FileSaveJbpm;
import org.jgpd.io.utils.FilePackage;
import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class ModelExportJBpm extends ModelExportImpl {

    public ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl();

    public ModelExportJBpm()
	{
    	processDefinition = new ProcessDefinitionImpl();
    }

    public Object createActivity()
	{
	    // Create an activity in the jBpm model
		ActivityStateImpl actState = new ActivityStateImpl();
		return actState;
	}

	public Object createDecision()
	{
		// Create a decision in the jBpm model
		DecisionImpl decision = new DecisionImpl();
		return decision;
	}

	public Object createProcess()
	{
		// Create a process in the jBpm model
		ProcessStateImpl process = new ProcessStateImpl();
		return process;
	}

	public Object createFork()
	{
		// Create a process in the jBpm model
		ForkImpl fork = new ForkImpl();
		return fork;
	}

	public Object createJoin()
	{
		// Create a process in the jBpm model
		JoinImpl join = new JoinImpl();
		return join;
	}

	public Object createStart()
	{
		// Create a start block in the jBpm model
		StartStateImpl startState = new StartStateImpl();
		processDefinition.setStartState(startState);
		return startState;
	}

	public Object createEnd()
	{
		// Create an end block in the jBpm model
		EndStateImpl endState = new EndStateImpl();
		processDefinition.setEndState(endState);
		return endState;
	}

	public void promptProcessProps(GPGraph graph)
	{
		processDefinition.getUIProperties(graph);
	}
	
	public void saveFile( 	GPGraphpad graphpad,
							GPDocument doc,
							URL filename,
							GPGraph gpGraph)
	{
		FileSaveJbpm fileSave = new FileSaveJbpm(graphpad,
												 doc,
												 filename,
												 gpGraph);

		fileSave.saveFile();
	}
	
	/**
	 * @see org.jgpd.io.ModelExportInterface#exportModelXML()
	 */
	public String exportModelXML(GPGraph graph)
	{
		// Create the necessary parts and package them as a .par
		return processDefinition.writeXML(graph);
	}
	
	public JPanel createBottomPanel(GPGraph g,
									GPDocument d,
									GPGraphpad gp)
	{
		return (JGpdPropertiesPanel.createPropPanel(g,d,gp));
	}
	
	public JPanel createSidePanel(GPGraph g,
								GPDocument d,
								GPGraphpad gp)
	{
		return (JGpdTree.createTreePanel(g,d,gp));
	}

	public DefaultMutableTreeNode createTreeNode( Object cell, JTree tree  )
	{
		if (cell != null)
		{
			// Check hashCode of object against current model objects
			// Note you must check node of sub-classes before parents
			// otherwise the sub-class will branch on the parent condition
			if (cell instanceof StartStateImpl)
			{
				return new StartTreeNode(cell, tree);
			}
			else if (cell instanceof EndStateImpl)
			{
				return new EndTreeNode(cell, tree);
			}
			else if (cell instanceof ActivityStateImpl)
			{
				return new ActivityTreeNode(cell, tree);
			}
			else if (cell instanceof ForkImpl)
			{
				return new ForkTreeNode(cell, tree);
			}
			else if (cell instanceof JoinImpl)
			{
				return new JoinTreeNode(cell, tree);
			}
			else if (cell instanceof DecisionImpl)
			{
				return new DecisionTreeNode(cell, tree);
			}
		}
		else
		{
			// FIXME it's an error for the cell to be null
		}

		return null; // indicates error in cell selection
	}
	
	public JGpdTableModel createTableModel( Object cell )
	{
		if (cell != null)
		{
			// Check hashCode of object against current model objects
			// Note you must check node of sub-classes before parents
			// otherwise the sub-class will branch on the parent condition
			if (cell instanceof StartStateImpl)
			{
				return new StartTableModel(cell);
			}
			else if (cell instanceof EndStateImpl)
			{
				return new EndTableModel(cell);
			}
			else if (cell instanceof ActivityStateImpl)
			{
				return new ActivityTableModel(cell);
			}
			else if (cell instanceof ForkImpl)
			{
				return new ForkTableModel(cell);
			}
			else if (cell instanceof JoinImpl)
			{
				return new JoinTableModel(cell);
			}
			else if (cell instanceof DecisionImpl)
			{
				return new DecisionTableModel(cell);
			}
			else if (cell instanceof AssignmentImpl)
			{
				return new HandlerTableModel(cell);
			}
			else if (cell instanceof ParametersImpl)
			{
				return new ParametersTableModel(cell);
			}
			else
			{
				// fIXME what the hell are we doing here?
			}
		}
		else
		{
			// FIXME it's an error for the cell to be null
		}

		return null; // indicates error in cell selection
	}
	
	public void registerComponentListeners(JPanel sidePanel,
										   JPanel bottomPanel)
	{
		JTree tree = JGpdTree.getTree(sidePanel);
		JPanel tablePanel = JGpdPropertiesPanel.getPanel(bottomPanel);
		tree.addTreeSelectionListener((JGpdPropertiesPanel)tablePanel);
		
		((JGpdPropertiesPanel)tablePanel).setListenedComponent(tree);
		
	}
}