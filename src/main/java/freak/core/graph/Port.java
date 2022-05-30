/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.graph;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <code>Port</code>s are interfaces between <code>Operator</code>s. Also two Ports imply an edge in the <code>OperatorGraph</code> if they are connected (which means that the partner attributes point to each other). Ports always belong to one Operator.
 * @author  Matthias
 */
abstract public class Port implements Serializable {

	private int number;
	private String description;
	protected Operator operator;
	private ArrayList partner = new ArrayList();

	/**
	 * Creates a new <code>Port</code>.
	 * 
	 * @param operator a link back to the operator.
	 * @param number the index number of the port.
	 */
	public Port(Operator operator, int number) {
		if (operator == null) {
			throw new NullPointerException("Port must belong to a operator => operator must not be null");
		}
		this.operator = operator;
		this.number = number;
	}

	/**
	 * Sets the number of the port.
	 * @param number  the number to set.
	 * @uml.property  name="number"
	 */
	protected void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Returns the number of the port.
	 * @return  the number of the port.
	 * @uml.property  name="number"
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the description of the port.
	 * @param description  the description of the port.
	 * @uml.property  name="description"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return  the description of the port
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * Adds a communication partner. Also informs the partner to be added so that he
	 * includes this object to his partner list too.
	 * @param partner the partner to add.
	 */
	public void addPartner(Port partner) {
		if (partner == null) {
			throw new NullPointerException("can't add 'null' as partner");
		}

		// add new partner
		this.partner.add(partner);

		//inform the new partner
		// we don't use addPartner here to avoid an infinite loop 
		partner.partner.add(this);
	}
	
	/**
	 * Removes the communication Partner. Also informs the partner to be removed so that he
	 * removes this object from his list too. If there are multiple connections only one is removed.
	 * @param partner the partner to remove from the list.
	 */
	public void removePartner(Port partner) {
		if (partner == null) {
			throw new NullPointerException("can't remove 'null' as partner");
		}
		
		this.partner.remove(partner);
		partner.partner.remove(this);
	}

	/**
	 * Returns the i-th partner of the port.
	 * 
	 * @return the partner of the port. May be null.
	 */
	public Port getPartner(int i) {
		return (Port)partner.get(i);
	}
	
	protected Iterator getPartnerIterator() {
		return partner.listIterator();
	}
	
	public int getNumberOfPartners() {
		return partner.size();
	}

	/**
	 * Returns the operator to which the port belongs.
	 * @return  the operator.
	 * @uml.property  name="operator"
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Performs a syntax check and throws exceptions if the port is not 
	 * correct.
	 * 
	 * @throws GraphSyntaxException if the Port has syntax errors.
	 */
	public void checkSyntax() throws GraphSyntaxException {
		// nothing here... override to perform any syntax check
	}

	public String toString() {
		if (operator instanceof Finish && this instanceof OutPort) {
			return "Finish population";
		}

		String arrow = " -> ";
		if (this instanceof InPort)
			arrow = " <- ";
		
		String partner = "[";
		for (Iterator iter = getPartnerIterator(); iter.hasNext();) {
			Port port = (Port)iter.next();
			partner += port.operator.getName();
			if (iter.hasNext()) {
				partner += ", ";
			}
		}
		return operator.getName() + arrow + partner+"]";
	}
}