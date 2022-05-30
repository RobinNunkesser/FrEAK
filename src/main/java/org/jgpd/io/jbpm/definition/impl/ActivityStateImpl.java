package org.jgpd.io.jbpm.definition.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jgpd.UI.PropPanelConfig;
import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.jbpm.definition.ActivityState;
import org.jgpd.io.jbpm.definition.FieldAccess;
import org.jgpd.io.utils.FilePackage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ActivityStateImpl extends StateImpl implements ActivityState {

	private String actorRoleName = new String("Edit");
	public static final String nodeType = new String("activity-state");
	protected AssignmentImpl assignment = null;
	
	public ActivityStateImpl()
	{
		super();
	}

	public ActivityStateImpl(Node node)
	{
		String nodeValue = null;
		LinkedList newFields = new LinkedList();

        // Get attributes
        NamedNodeMap attrMap = node.getAttributes();
        Node nameNode = attrMap.getNamedItem("name");
        String name = nameNode.getNodeValue();
        setName(name);

        // Get sub-elements
		for (int i = 0; i < node.getChildNodes().getLength(); i++)
		{
			Node child = node.getChildNodes().item(i);
			String nextChildString = child.getNodeName().toLowerCase();
			if (child.getNodeName().toLowerCase().equals("description"))
			{
				Node next_child = child.getFirstChild();
			try {
				nodeValue = next_child.getNodeValue();
			} catch (Exception e) {
				// FIXME - do something
			}
			    if ( nodeValue != null )
			    {
			    	setDescription(nodeValue);
			    }
			}
			else if (child.getNodeName().toLowerCase().equals("role"))
			{
				Node next_child = child.getFirstChild();
			try {
				nodeValue = next_child.getNodeValue();
			} catch (Exception e) {
				// FIXME - do something
			}
				if ( nodeValue != null )
				{
					setActorRoleName(nodeValue);
				}
			}
			else if (child.getNodeName().toLowerCase().equals("field"))
			{
				FieldImpl field = new FieldImpl(child);
				newFields.add(field);
			}
		}

		setFields(newFields);
	}

	public PropPanelConfig propPanelModel(JTable[] table,
	                                      JGpdTableModel[] tableModel)
	{

		TableColumn editColumn = table[0].getColumnModel().getColumn(1);
		JTextField textField = new JTextField();
		editColumn.setCellEditor(new DefaultCellEditor(textField));

		// Fields tab
		tableModel[1].setRowCount(0);
		tableModel[1].setColumnIdentifiers(new Object[]{"Attribute",
			                                          "Access"});
		tableModel[1].setDefaultCellEntries(new Object[]{"Enter Attribute name",
														 FieldAccess.READ_ONLY});
		tableModel[1].setAddDeletingAllowed(true);
		tableModel[1].setColumnEditable(new boolean[]{true,true});
		tableModel[1].setMaxNumDynamicRows(50000);
		tableModel[1].setNumStaticRows(0);

		Collection fields = getFields();
		Iterator iter = fields.iterator();
		while (iter.hasNext())
		{
			FieldImpl field = (FieldImpl) iter.next();
			tableModel[1].addRow( new Object[] {
					field.getName(),
					field.getAccess().toString()});
		}

		editColumn = table[1].getColumnModel().getColumn(1);

		JComboBox comboBox = new JComboBox();
		comboBox.addItem(FieldAccess.NOT_ACCESSIBLE.toString());
		comboBox.addItem(FieldAccess.READ_ONLY.toString());
		comboBox.addItem(FieldAccess.WRITE_ONLY.toString());
		comboBox.addItem(FieldAccess.WRITE_ONLY_REQUIRED.toString());
		comboBox.addItem(FieldAccess.READ_WRITE.toString());
		comboBox.addItem(FieldAccess.READ_WRITE_REQUIRED.toString());

		editColumn.setCellEditor(new DefaultCellEditor(comboBox));		
		
		return null;
	}

	public void applyProperties(DefaultTableModel[] tableModel)
	{
		Vector fieldsVector = tableModel[1].getDataVector();
		if ( fieldsVector.isEmpty() )
		{
			clearFields();
		}
		else
		{
			LinkedList newFields = new LinkedList();
			for (int i = 0; i < fieldsVector.size(); i++)
			{
				FieldImpl field = new FieldImpl();
				field.setName(((Vector)fieldsVector.get(i)).get(0).toString());
				String access = ((Vector)fieldsVector.get(i)).get(1).toString();
				field.setAccess(FieldAccess.fromText(access));

				//FIXME need to add initial field value?
				newFields.add( field );
			}
			setFields(newFields);
		}
	}

	public String writeXML(String indent)
	{
		String xml = indent + "<activity-state name=\"" + this.name + "\">\n";
		
		String local_indent = indent + "  ";
		
		xml += local_indent + "<description>" + this.description + "</description>\n";
		
		// FIXME TODO db assignment handler here
		
		xml += local_indent + "<role>" + actorRoleName + "</role>\n";
		
		Collection fields = getFields();
		Iterator iter = fields.iterator();
		while (iter.hasNext())
		{
			FieldImpl field = (FieldImpl) iter.next();
			xml += field.writeXML( local_indent+ "  " );
		}
		
		xml += super.writeXML(local_indent);
		
		xml += indent + "</activity-state>\n\n";
		
		return xml;
	}
	
	public String getActorRoleName() { return actorRoleName; }
	public void setActorRoleName( String actorRoleName ) { this.actorRoleName = actorRoleName; }
	
	public String getDisplayedNodeType()
	{
		return nodeType;
	}

	/**
	 * @return Returns the assignment.
	 */
	public AssignmentImpl getAssignment() {
		return assignment;
	}

	/**
	 * @param assignment The assignment to set.
	 */
	public void setAssignment(AssignmentImpl assignment) {
		this.assignment = assignment;
	}

	public boolean createAssignment()
	{
		// start node inheritance hack
		if ( !(this instanceof StartStateImpl) )
		{
			if ( assignment == null )
			{
				assignment = new AssignmentImpl();
				assignment.setHandler(new FilePackage(null ,null));
				return true;
			}
		}

		return false;
	}
	
	public boolean deleteAssignment()
	{
		// start node inheritance hack
		if ( !(this instanceof StartStateImpl) )
		{
			if ( assignment != null )
			{
				assignment = null;
				return true;
			}
		}

		return false;
	}
}
