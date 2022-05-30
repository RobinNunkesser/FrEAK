/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import java.io.Serializable;

/**
 * An InputNode is connected to one input variable and gives the corresponding value.
 * @author  Melanie
 */
//this is the static input Node. that means the input variable can't be changed.
//and it does not know about the number of variables.
public class StaticInputNode implements AtomicNode, Serializable {

	private int inputNumber;
	private String name;
	
	/**
	 * Creates a new inputNode with the given variable number num. 
	 * If num is < 0, the variable number is set to -1.
	 * @num number of input variable, must be >= 0
	 */
	public StaticInputNode(int num){
		if (num>=0) {
			inputNumber = num;
			name = Data.getVarName(this.inputNumber);
			} else inputNumber = -1;
	}	
	
	/**
	 * Returns the value of the corresponding input variable in the given row.
	 * Returns -1 if the variable number is out of range of the row.
	 * @param row The row of the function table for which the tree should be evaluated. 
	 */
	public int getValue(byte[] row){
		if (inputNumber >= 0 && inputNumber < row.length){
			return row[inputNumber];
		} else {
			return -1;
		}
	}	
	
	/**
	 * Returns the number of the input variable this inputNode is connected to.
	 * @uml.property  name="inputNumber"
	 */
	int getInputNumber(){
		return inputNumber;
	}
	
	// ************ overwritten java methods ******************************************************
	
	/**
	 * Returns whether this inputNode is equal to a given object.
	 * Equality means that the given object is also an inputNode 
	 * and that both refer to the same input variable.
	 */
	public boolean equals(Object o) {
		if ( (o==null) || (!(o instanceof StaticInputNode))) return false;
		return ( ((StaticInputNode)o).getInputNumber() == getInputNumber() );
	}
	
	/**
	 * Overrides the standard function to provide more information about this inputNode
	 * Gives the number of the connected variable like this: x_12
	 */
	public String toString(){
		String rueckgabe;
		rueckgabe= name; 
		return rueckgabe;
	}
	
	/**
	 * Clones this inputNode.
	 */
	public Object clone(){
		StaticInputNode in = new StaticInputNode(inputNumber);
		return in;
	}
		
}
