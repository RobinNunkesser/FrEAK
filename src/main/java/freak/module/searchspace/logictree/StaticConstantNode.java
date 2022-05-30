/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import java.io.Serializable;

/**
 * @author  Melanie
 */

// this is the static Constant Node. that means the constant can't be changed.
// and it has no intern maximum for the constant value.
public class StaticConstantNode implements AtomicNode, Serializable{

	private byte value;
	private int standardMaxValue = 3;

	/**
	 * Creates a new constantNode with Value value.
	 * @value value Value to be set
	 */
	public StaticConstantNode(int value){
//		 * If value is not greater than zero, a random constant in 1..3 will be set.
//		if (value > 0){
		this.value = (byte)value;
	//	} else {
	//		this.value = Data.nextRandInt(standardMaxValue)+1;
	//	}
	}
	
	/**
	 * Returns the constant value of this node.
	 * @uml.property  name="value"
	 */
	public byte getValue(){
		return value;
	}
	
	/**
	 * Returns the constant value of this node.Independent from the value of row. 
	 */
	public int getValue(byte[] row){
		return (int)getValue();
	}
	
	// ************ overwritten java methods ******************************************************
	
	/**
	 * Returns whether this constantNode is equal to a given object.
	 * Equality means that the given object is also a constantNode 
	 * and that both have the same constant value.
	 */
	public boolean equals(Object o) {
		if ( (o==null) || (!(o instanceof StaticConstantNode))) return false;
		return ( ((StaticConstantNode)o).getValue() == getValue() );
	}
	
	/**
	 * Overrides the standard function to provide more information about this constantNode.
	 * Simply gives the constant value.
	 */
	public String toString(){
		String rueckgabe;
		rueckgabe = ""+value;
		return rueckgabe;
	}
	
	/**
	 * Clones this constantNode.
	 */
	public Object clone(){
		StaticConstantNode cn = new StaticConstantNode(value);
		return cn;
	}
}
