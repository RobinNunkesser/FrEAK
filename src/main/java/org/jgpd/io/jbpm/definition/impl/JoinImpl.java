package org.jgpd.io.jbpm.definition.impl;

import javax.swing.JTable;

import org.jgpd.UI.PropPanelConfig;
import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.jbpm.definition.Join;
import org.jgraph.GPGraphpad;
import org.w3c.dom.Node;

public class JoinImpl extends DecisionImpl implements Join {
	
	public static final String nodeType = new String("join");
	
	public JoinImpl() {}
	
	public JoinImpl(Node node)
	{
		super(node);
		// FIXME joins also have 0-n actions
	}
	
	public PropPanelConfig propPanelModel(JTable[] table,
			JGpdTableModel[] tableModel,
			GPGraphpad gp)
	{
		PropPanelConfig panelConfig = super.propPanelModel(table,tableModel,gp);

		// Add actions panel
		int nextTab = panelConfig.numTabsUsed;
		panelConfig.numTabsUsed++;
		
		panelConfig.tabStrings.add("Parameters");

		// Properties tab
		tableModel[nextTab].setRowCount(0);
		tableModel[nextTab].setColumnIdentifiers(new Object[]{"Property",
		"Value"});
		// No entries allowed, set any default entry string
		tableModel[nextTab].setDefaultCellEntries(new Object[]{"", ""});
		tableModel[nextTab].setAddDeletingAllowed(false);
		tableModel[nextTab].setColumnEditable(new boolean[]{false,true});
		tableModel[nextTab].setMaxNumDynamicRows(0);
		tableModel[nextTab].insertRow( 0, new Object[] { "Name",	getName(), });
		tableModel[nextTab].setNumStaticRows(1);

		return panelConfig;
	}

	public String getNodeType()
	{
		return JoinImpl.nodeType;
	}
}
