package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.jbpm.definition.Attribute;
import org.jgpd.io.jbpm.definition.Field;
import org.jgpd.io.jbpm.definition.FieldAccess;
import org.jgpd.io.jbpm.definition.State;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FieldImpl implements Field {
	
	public FieldImpl() {}
	
	public FieldImpl(Node node)
	{
		// Get attributes
		NamedNodeMap attrMap = node.getAttributes();
		// Get name
		Node nameNode = attrMap.getNamedItem("attribute");
		String name = nameNode.getNodeValue();
		setName(name);
		// Get access
		Node accessNode = attrMap.getNamedItem("access");
		String accessText = accessNode.getNodeValue();
		this.access = FieldAccess.fromText( accessText );
		// FIXME what about initial value?
	}
	
	public String getDisplayedNodeType()
	{
		return nodeType;
	}
	
	public String writeXML(String indent)
	{
		String xml = indent + "<field attribute=\"" + getName() + "\"\t";
		
		xml += "access=\"";
		if ( this.access != null )
		{
			xml += this.access.writeXML();
		}
		else
		{
			// FIXME TODO db add error log here that no FieldAccess object
			// had been created
			xml += "read-only";
		}
		xml += "\"/>\n";
		
		return xml;
	}
	
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	
	public String getDescription() { return this.description; }
	public void setDescription(String description) {this.description = description; }
	
	public Integer getIndex() { return this.index; }
	public void setIndex(Integer index) { this.index = index; }
	
	public Attribute getAttribute() { return this.attribute; }
	public void setAttribute(Attribute attribute) { this.attribute = attribute; }
	
	public State getState() { return this.state; }
	public void setState(State state) { this.state = state; }
	
	public FieldAccess getAccess() { return this.access; }
	public void setAccess(FieldAccess fieldAccess) { this.access = fieldAccess; }
	
	public String toString()
	{
		return name;
	}
	
	private String name = new String("Enter field name");
	private String description = null;
	private Integer index = null;
	private State state = null;
	private Attribute attribute = null;
	private FieldAccess access = FieldAccess.READ_ONLY;
	public static final String nodeType = new String("field attribute");
}
