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
public class VariableConstantNode implements AtomicNode, Serializable{

	private int value;
	private final int standard = 3;
	private int maxValue = 0;

	/**
	 * Creates a new constantNode with a random value between 1 and MaxValue (inclusively).
	 * If maxValue is <= 0, 3 is used as maxValue.
	 * @param maxValue maximum value for the random constant value of this node
	 */
	public VariableConstantNode(int maxValue){
		if (maxValue>0) {
			this.maxValue = maxValue;
		} else {
			this.maxValue = standard;
		}
		this.value = Data.nextRandInt(this.maxValue)+1;						
	}
	
	/**
	 * Creates a new constantNode with Value value and value range 1..maxValue (inclusively).
	 * If maxValue is <= 0, 3 is used as maxValue.
	 * @param maxValue maximum value for the constant value of this node
	 * @value value Value to be set, will be ignored and replaced by random value if greater than maxValue
	 */
	public VariableConstantNode(int maxValue, int value){
		if (maxValue>0) {
			this.maxValue = maxValue;
		} else {
			this.maxValue = standard;
		}
		if (value > 0 && value <= this.maxValue) {
			this.value = value; 		
		} else {
			this.value = Data.nextRandInt(this.maxValue)+1;			
		}
	}
	
	/**
	 * Returns the constant value of this node.
	 * @uml.property  name="value"
	 */
	public int getValue(){
		return value;
	}
	
	/**
	 * Returns the constant value of this node.Independent from the value of row. 
	 */
	public int getValue(byte[] row){
		return getValue();
	}
	
	
	/**
	 * Sets the constant value of this ConstantNode to val. Maximum value can be set in another function.
	 * @param val  wanted value, must be in range 1 .. maxValue.
	 * @uml.property  name="value"
	 */
	void setValue(int val){
		if (val > 0 && val <= maxValue) value = val;
	}
	
	/**
	 * Sets the maximum value for this constant node.
	 * @param  maxValue, must be > 0.
	 * @uml.property  name="maxValue"
	 */
	void setMaxValue(int maxValue){
		if (maxValue > 0) this.maxValue = maxValue;
	}
	
	/**
	 * Sets the constant value of this ConstantNode to a random value between one and the maximum value.
	 * Maximum value can be set in another function.
	 */
	void setRandomValue(){
		value = Data.nextRandInt(maxValue)+1;
	}
	
	// ************ overwritten java methods ******************************************************
	
	/**
	 * Returns whether this constantNode is equal to a given object.
	 * Equality means that the given object is also a constantNode 
	 * and that both have the same constant value.
	 */
	public boolean equals(Object o) {
		if ( (o==null) || (!(o instanceof VariableConstantNode))) return false;
		return ( ((VariableConstantNode)o).getValue() == getValue() );
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
		VariableConstantNode cn = new VariableConstantNode(maxValue,value);
		return cn;
	}
}
