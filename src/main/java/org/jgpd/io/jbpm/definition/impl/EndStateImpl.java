package org.jgpd.io.jbpm.definition.impl;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.jgpd.io.jbpm.definition.EndState;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EndStateImpl extends StateImpl implements EndState {

	public static final String nodeType = new String("end-state");

	public EndStateImpl() {}

	public EndStateImpl(Node node)
	{
		String nodeValue = null;

		// Get attributes
		NamedNodeMap attrMap = node.getAttributes();
		Node nameNode = attrMap.getNamedItem("name");
		String name = nameNode.getNodeValue();
		setName(name);
	}

	public void applyProperties(DefaultTableModel[] tableModel)
	{
		Vector dataVector = tableModel[0].getDataVector();
		setName(((Vector)dataVector.get(0)).get(1).toString());
	}

  public String getDisplayedNodeType()
  {
  	return nodeType;
  }

  public String writeXML(String indent)
  {
    String xml = indent + "<end-state name=\"";
    xml += getName() + "\"/>\n";
    return xml;
  }

  protected void validateLeavingTransitions()
  {
    // overwriting the test of the node that requires that a node has to have
    // at least one leaving transition
  }
}
