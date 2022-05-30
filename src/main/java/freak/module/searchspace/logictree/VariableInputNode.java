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

public class VariableInputNode implements AtomicNode, Serializable {

	private int inputNumber;
	private int numVars = 0;
	private String name;
	
	/**
	 * Creates a new inputNode with a random input variable. 
	 * The number of the variable is chosen between 1 and numVariables (inclusively).
	 * If numVariables is <= 0, the inputNode is set to -1.
	 * @param numVariables Number of input variables
	 */
	public VariableInputNode(int numVariables){
		if (numVariables>0) {
			inputNumber = Data.nextRandInt(numVariables);	
			this.numVars = numVariables;
			name = Data.getVarName(this.inputNumber);
		} else {
			inputNumber = -1;
			numVars = 0;
		}
	}	
	
	/**
	 * Creates a new inputNode with the given variable number num. 
	 * If numVariables is <= 0, the variable number is set to -1.
	 * @num number of input variable, must be in 0..numVariables-1, elsewise its set randomly.
	 * @param numVariables Number of input variables
	 */
	public VariableInputNode(int numVariables, int num){
		if (numVariables>0) {
			this.numVars = numVariables;
			if (num >= 0 && num < numVariables) {
				inputNumber = num;
			} else inputNumber = Data.nextRandInt(numVariables);
			name = Data.getVarName(this.inputNumber);
		} else {
			numVars = 0;
			inputNumber = -1;
		}
	}	
	
	/**
	 * Returns the value of the corresponding input variable in the given row.
	 * @param row The row of the function table for which the tree should be evaluated. 
	 */
	public int getValue(byte[] row){
		if (inputNumber >= 0 && inputNumber < row.length){
			return row[inputNumber];
		} else {
			return 0;
		}
	}	
	
	/**
	 * Returns the number of the input variable this inputNode is connected to.
	 * @uml.property  name="inputNumber"
	 */
	int getInputNumber(){
		return inputNumber;
	}
	
	/**
	 * Connects this inputNode to the input variable with number n.
	 * @param n  number of the variable this node should be connected to, must be in range.
	 * @uml.property  name="inputNumber"
	 */
	void setInputNumber(int n){
		if (n>=0 && n < numVars) inputNumber = n;
	}
	
	/**
	 * Connects this inputNode to a random input variable with number 1..numVars.
	 * The number of variables can be set with another function. 
	 */
	void setRandomInputNumber(){
		inputNumber = Data.nextRandInt(numVars);
	}

	/**
	 * Sets the number of variables to num, if num >= 0.
	 * @param num  number of input variables.
	 * @uml.property  name="numVars"
	 */
	void setNumVars(int num){
		if (num >= 0) {
			numVars = num;
		}
	}
	
	// ************ overwritten java methods ******************************************************
	
	/**
	 * Returns whether this inputNode is equal to a given object.
	 * Equality means that the given object is also an inputNode 
	 * and that both refer to the same input variable.
	 */
	public boolean equals(Object o) {
		if ( (o==null) || (!(o instanceof VariableInputNode))) return false;
		return ( ((VariableInputNode)o).getInputNumber() == getInputNumber() );
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
		VariableInputNode in = new VariableInputNode(numVars,inputNumber);
		return in;
	}
		
}
