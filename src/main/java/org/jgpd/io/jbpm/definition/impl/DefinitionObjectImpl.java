package org.jgpd.io.jbpm.definition.impl;

import java.util.LinkedList;

import javax.swing.JTable;

import org.jgpd.UI.PropPanelConfig;
import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.JGpdModelNode;
import org.jgpd.io.jbpm.definition.*;

public class DefinitionObjectImpl extends JGpdModelNode
implements DefinitionObject
{
	protected LinkedList actions = new LinkedList();
	protected Long id = null;
	protected String name = new String("Name");
	protected String description = new String("Description");
	public static String modelType = new String("x-jbpm-");

	public DefinitionObjectImpl() {}
	
	public PropPanelConfig propPanelModel(JTable[] table, JGpdTableModel[] tableModel)
	{
		return null;
	}
	
	public String getExportModelPrefix()
	{
		return modelType;
	}
	
	public void setDisplayName(String name)
	{
		setName(name);
	}
	
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	public boolean hasName() { return (this.name != null ); }
	
	public String getDescription() { return description; }
	public void setDescription ( String description ) { this.description = description; }
	
	public LinkedList getActions() { return this.actions; }
	public void setActions(LinkedList actions) { this.actions = actions; }
	
	public ActionImpl createNewAction()
	{
		LinkedList actions = getActions();
		ActionImpl action = new ActionImpl();
		actions.add(action);
		
		return action;
	}

	public void removeAction(ActionImpl action)
	{
		getActions().remove(action);
	}

	public void moveActionDown(ActionImpl action)
	{
		int pos=actions.indexOf(action);
		if ( pos+1<actions.size() )
		{		
			actions.remove(pos);
			actions.add(pos+1,action);
		}		
	}

	public void moveActionUp(ActionImpl action)
	{
		int pos=actions.indexOf(action);
		if ( pos!=0 )
		{		
			actions.remove(pos);
			actions.add(pos-1,action);
		}
	}

	public String toString() {
		return getTypeName() + "[" + id + "|" + name + "]";
	}
	
	public String getTypeName()
	{
		String className = this.getClass().getName();
		int to = className.length();
		if ( className.endsWith( "Impl" ) )
		{
			to = to - 4;
		}
		int from = className.lastIndexOf( '.' ) + 1;
		return className.substring( from, to );
	}
}
