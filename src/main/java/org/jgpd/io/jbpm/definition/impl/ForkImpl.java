package org.jgpd.io.jbpm.definition.impl;

import javax.swing.JTable;

import org.jgpd.UI.PropPanelConfig;
import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.jbpm.definition.Fork;
import org.jgraph.GPGraphpad;
import org.w3c.dom.Node;

public class ForkImpl extends DecisionImpl implements Fork {
	
	public static final String nodeType = new String("fork");
	
	public ForkImpl() {}
	
	public ForkImpl(Node node)
	{
		super(node);
	}

	public PropPanelConfig propPanelModel(JTable[] table,
			JGpdTableModel[] tableModel,
			GPGraphpad gp)
	{
		PropPanelConfig panelConfig = super.propPanelModel(table,tableModel,gp);

		return panelConfig;
	}

	public String getNodeType()
	{
		return ForkImpl.nodeType;
	}
}
