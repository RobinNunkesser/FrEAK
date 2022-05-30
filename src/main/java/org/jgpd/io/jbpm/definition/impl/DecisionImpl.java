package org.jgpd.io.jbpm.definition.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jgpd.UI.PropPanelConfig;
import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.UI.utils.FileWizardEditor;
import org.jgpd.io.jbpm.definition.Decision;
import org.jgpd.io.utils.FilePackage;
import org.jgraph.GPGraphpad;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DecisionImpl extends NodeImpl implements Decision {

	public static final String nodeType = new String("decision");
	protected FilePackage handler = new FilePackage(null ,null);
	protected ParametersImpl parameters = new ParametersImpl(); // parameter elements 0-n

    public DecisionImpl()
	{
    	handler.setDescription(new String("Edit"));
    }

	public DecisionImpl(Node node)
	{
		String nodeValue = null;
		Vector newFields = new Vector();

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
			if (child.getNodeName().toLowerCase().equals("filepackage"))
			{
				handler = new FilePackage(child);
			}
			else if (child.getNodeName().toLowerCase().equals("parameter"))
			{
				Node next_child = child.getFirstChild();
				try {
					nodeValue = next_child.getNodeValue();
				} catch (Exception e) {
					// FIXME - do something
				}

				if ( nodeValue != null )
				{
					Parameter para = new Parameter(child, nodeValue);
					parameters.add(para);
				}
				else
				{
					// FIXME, no value to parameter - error
				}
			}
		}
	}

	public PropPanelConfig propPanelModel(JTable[] table,
			JGpdTableModel[] tableModel,
			GPGraphpad gp)
	{
		PropPanelConfig panelConfig = new PropPanelConfig();

		panelConfig.numTabsUsed = 3;

		panelConfig.tabStrings.add("Handler");
		panelConfig.tabStrings.add("Parameters");

		// Fields tab
		tableModel[1].setRowCount(0);
		tableModel[1].setColumnIdentifiers(new Object[]{"Name",
		"Handler Class"});
		TableColumn handlerColumn = table[1].getColumnModel().getColumn(1);
		FileWizardEditor fileWizard = new FileWizardEditor(gp.getFrame());
		handlerColumn.setCellEditor(fileWizard);

		// No entries allowed, set any default entry string
		tableModel[1].setDefaultCellEntries(new Object[]{"", ""});
		tableModel[1].setAddDeletingAllowed(false);
		tableModel[1].setColumnEditable(new boolean[]{true,true});
		tableModel[1].setMaxNumDynamicRows(0);
		tableModel[1].insertRow( 0, new Object[] {
				            handler.getDescription(),
							handler } );
		tableModel[1].setNumStaticRows(1);

		return panelConfig;
	}

	public void applyProperties(DefaultTableModel[] tableModel)
	{
		Vector propsVector = tableModel[0].getDataVector();
		setName(((Vector)propsVector.get(0)).get(1).toString());

		Vector handlerVector = tableModel[1].getDataVector();
		handler = (FilePackage)(((Vector)handlerVector.get(0)).get(1));

		handler.setDescription( ( (String)(((Vector)handlerVector.get(0)).get(0)) ) );

		// Parameters
		Vector paramsVector = tableModel[2].getDataVector();
		if ( paramsVector.isEmpty() )
		{
			setParameters(new ParametersImpl());
		}
		else
		{
			ParametersImpl newParams = new ParametersImpl();
			for (int i = 0; i < paramsVector.size(); i++)
			{
				String name = ((Vector)paramsVector.get(i)).get(0).toString();
				String value = ((Vector)paramsVector.get(i)).get(1).toString();
				Parameter param = new Parameter(name ,value);

				newParams.add( param );
			}
			setParameters(newParams);
		}
	}

	public String getDisplayedNodeType()
	{
		return nodeType;
	}

	public String writeXML(String indent)
	{
		String xml = indent + "<" + getNodeType() +
			" name=\"" +
			this.name +
			"\" ";

		if ( handler != null )
		{
			xml += "handler=\"";
			xml += handler.toString() + "\" ";
		}

		xml += ">\n";

		Collection params = getParameters();
		Iterator iter = params.iterator();
		while (iter.hasNext())
		{
			Parameter param = (Parameter) iter.next();
			xml += param.writeXML( indent+ "  " );
		}

		xml += super.writeXML(indent + "  ");
		
		xml += indent + "</" + getNodeType() + ">\n\n";

		return xml;
	}

	/**
	 * @return
	 */
	public ParametersImpl getParameters()
	{
		return parameters;
	}

	/**
	 * @param parameters
	 */
	public void setParameters(ParametersImpl parameters)
	{
		this.parameters = parameters;
	}

	public String getNodeType()
	{
		return DecisionImpl.nodeType;
	}
	/**
	 * @return
	 */
	public FilePackage getHandler()
	{
		return handler;
	}

	/**
	 * @param handler
	 */
	public void setHandler(FilePackage handler)
	{
		this.handler = handler;
	}

}
