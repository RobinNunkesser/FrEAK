package org.jgpd.io.jbpm.definition.impl;

import java.util.*;

import org.jgpd.io.jbpm.definition.*;

public class StateImpl extends NodeImpl implements State {


	public StateImpl() {}
	{
	    this.fields = new LinkedList();
    }

	public String writeXML(String indent)
	{
		return (super.writeXML(indent));
	}
	
	
	public Integer[] getImageCoordinates() { return new Integer[] { x1, y1, x2, y2 }; }
	public void setImageCoordinates( Integer[] imageCoordinates ) {
		this.x1 = imageCoordinates[0];
		this.y1 = imageCoordinates[1];
		this.x2 = imageCoordinates[2];
		this.y2 = imageCoordinates[3];
	}
	
	public LinkedList getFields() { return this.fields; }
	public void setFields(LinkedList fields) { this.fields = fields; }
	public void clearFields()
	{
		this.fields.clear();
	}
	
	public FieldImpl createNewField()
	{
		Collection fields = getFields();
		FieldImpl field = new FieldImpl();
		fields.add(field);
		
		return field;
	}
	
	public void removeField(FieldImpl field)
	{
		getFields().remove(field);
	}
	
	protected void moveFieldDown(FieldImpl field)
	{
		int pos=fields.indexOf(field);
		if ( pos+1<fields.size() )
		{		
			fields.remove(pos);
			fields.add(pos+1,field);
		}		
	}

	protected void moveFieldUp(FieldImpl field)
	{
		int pos=fields.indexOf(field);
		if ( pos!=0 )
		{		
			fields.remove(pos);
			fields.add(pos-1,field);
		}
	}

	private Integer getCoordinateX1() { return x1; }
	private void setCoordinateX1( Integer coordinate ) { x1 = coordinate; }
	
	private Integer getCoordinateY1() { return y1; }
	private void setCoordinateY1( Integer coordinate ) { y1 = coordinate; }
	
	private Integer getCoordinateX2() { return x2; }
	private void setCoordinateX2( Integer coordinate ) { x2 = coordinate; }
	
	private Integer getCoordinateY2() { return y2; }
	private void setCoordinateY2( Integer coordinate ) { y2 = coordinate; }
	
	/**
	 * a state does not have a role (=swim-lane) so the default is not to add
	 * anything.  In {@link ActivityStateImpl} this behaviour is overridden
	 * and the actor is stored in the role-attribute.
	 */
	public Map addRoleAttributeValue(Map attributeValues, String actorId) {
		return attributeValues;
	}
	
	private LinkedList fields;
	private Integer x1 = null;
	private Integer y1 = null;
	private Integer x2 = null;
	private Integer y2 = null;
}
