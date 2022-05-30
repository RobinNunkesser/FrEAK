package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.jbpm.definition.*;

public class TransitionImpl extends DefinitionObjectImpl implements Transition {
	
	
	public TransitionImpl() {}
	
	public Node getFrom() { return this.from; }
	public void setFrom(Node from) { this.from = from; }
	
	public Node getTo() { return this.to; }
	public void setTo(Node to) { this.to = to; }
	
	public String writeXML(String indent)
	{
		String xml = "";
		
		xml += indent + "<transition ";
		if ( getName() != null )
		{
			if ( !(getName().equals("")) )
			xml += "name=\"" + getName() + "\"\t\t";
		}
		
		xml += "to=\"" + to.getName() + "\" />\n";
		
		return xml;
	}
	
	private Node from = null;
	private Node to = null;
}
