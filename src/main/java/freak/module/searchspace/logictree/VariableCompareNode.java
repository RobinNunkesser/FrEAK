/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

/**
 * @author Melanie
 */

import java.io.Serializable;
import java.util.BitSet;

public class VariableCompareNode implements OperatorNode, Serializable {

	private StaticConstantNode constant;
	private StaticInputNode input;
	private boolean less;
	private boolean equal;
	private boolean greater;
	// array gives allowed operators, 1st bit stands for less,
	//                                2nd stands for equal,
	//                                3rd stands for greater
	// => 011 = 3 => allowedOps[3] asks whether ">=" is allowed.
	// normally forbidden are "", ">", "<","<=>"
	// allowed are "=", ">=", "<>", "<="
	private boolean[] allowedOp = {false, false, true, true, false, true, true, false};
	private byte[] allowedOpList = {2,3,5,6};
	
	/**
	 * Creates a new compareNode with given children.
	 * The last three parameters specify which operators are allowed in the evaluation.
	 * @param maxConstant Maximum value for the constant node
	 * @param numVariables Number of input variables
	 * @param less    if true, this compare Node interprets in < cn as true 
	 * @param equal if true, this compare Node interprets in = cn as true
	 * @param greater if true, this compare Node interprets in > cn as true
	 */
	public VariableCompareNode(StaticConstantNode cn, StaticInputNode in, boolean less, boolean equal, boolean greater){
		constant = cn;
		input = in;
		if (allowedOp[internOpID(less,equal,greater)]) {
			this.less = less;
			this.equal = equal;
			this.greater = greater;			
		} else {
			setRandomState();
		}
	}	
	
	/**
	 * Creates a new compareNode which children will be created randomly according to 
	 * maxConstant and numVariables. 
	 * Operators will be chosen randomly out of the allowed ones.
	 * @param maxConstant Maximum value for the constant node
	 * @param numVariables Number of input variables
	 */
	public VariableCompareNode(int maxConstant, int numVariables){
		this(new StaticConstantNode(maxConstant),new StaticInputNode(numVariables),Data.nextRandBoolean(),Data.nextRandBoolean(),Data.nextRandBoolean());
	}
	
	// gives integer from three bits
	private byte internOpID(boolean less, boolean equal, boolean greater){
		byte rueckgabe = 0;
		if (less) rueckgabe += 4;
		if (equal) rueckgabe += 2;
		if (greater) rueckgabe += 1;
		return rueckgabe;
	}
	
	// gives third bit of three bit number
	private boolean getLFromInternOpID(byte id){
		return (id>=4);
	}
	
	// gives secord bit of three bit number
	private boolean getEFromInternOpID(byte id){
		return (id==2 || id==3 || id==6 || id==7);
	}
	
	// gives first bit of three bit number
	private boolean getGFromInternOpID(byte id){
		return (id==1 || id==3 || id==5 || id==7);
	}

	// creates the allowedOpList from the allowedOp - Array
	private void buildAllowedOpList(){
		byte anzahl = 0;
		for (byte i = 0; i < 8; i++) {
			if (allowedOp[i]) anzahl++;
		}		
		allowedOpList = new byte[anzahl];
		anzahl = 0;
		for (byte i = 0; i < 8; i++){
			if (allowedOp[i]) {
				allowedOpList[anzahl] = i; 
				anzahl++;
			}
		}
	}
	
	// returns a random operatorcombination that is allowed
	private byte getRandomAllowedOp(){
		byte zz = (byte)Data.nextRandInt(allowedOpList.length);
		return allowedOpList[zz];
	}
	
	// sets a random state that is allowed
	private void setRandomState(){
		byte op = getRandomAllowedOp();
		less = getLFromInternOpID(op);
		equal = getEFromInternOpID(op);
		greater = getGFromInternOpID(op);
	}
	
    /**
     * Allows an additional operatorcombination.
     */
	public void allowOperator(boolean less, boolean equal, boolean greater){
		allowedOp[internOpID(less,equal,greater)] = true;
		buildAllowedOpList();
	}
	
	/**
	 * Forbids an additional operatorcombination.
	 */
	public void forbidOperator(boolean less, boolean equal, boolean greater){
		allowedOp[internOpID(less,equal,greater)] = false;
		buildAllowedOpList();		
	}
	
	/**
	 * Evaluates the subtree where two arithmetic values are compared.
	 * Depending on the setting of this compare node, true is given
	 * if the two children have or don't have the same value. 
	 * row is needed for the evaluation of the inputNode-Child.
	 * @param row The row of the function table for which the tree should be evaluated. 
	 */
	public boolean getValue(byte[] row){
		int i = input.getValue(row);
		int j = constant.getValue(row);		
//		boolean a = ( (input.getValue(row) == constant.getValue()) && equality);
//		boolean b = ( (input.getValue(row) != constant.getValue()) && !equality);
		boolean a = ( equal   && (i == j));
		boolean b = ( less    && (i <  j));
		boolean c = ( greater && (i >  j));
//		boolean a = 
		return ( a || b || c);
//		return ( ( (input.getValue(row) == constant.getValue()) && equality)  // values are supposed to be equal
//		         || ( (input.getValue(row) != constant.getValue()) && !equality) ); // values are supposed to be inequal
	}	
	
	// only because new abstract operatorNode wants this
	public BitSet getValueBitset(){
		throw new RuntimeException("Unsupported method.");
	}

	public void updateBitset(){
		throw new RuntimeException("Unsupported method.");
	}
	
	public BitSet getLiteralBitSet () {
		throw new RuntimeException("Unsupported method.");
	}

	
	/**
	 * Returns the number of CompareNodes in this subtree.
	 * 
	 */
	public int getSubtreeSize(){
		return 1;
	}
	
	/**
	 * Returns the child that is a constantNode. Each compareNode has 
	 * two children, a constant and an input one.
	 */
	StaticConstantNode getConstantChild(){
		return constant;
	}

	/**
	 * Returns the child that is a inputNode. Each compareNode has 
	 * two children, a constant and an input one.
	 */
	StaticInputNode getInputChild(){
		return input;
	}
	
/*	/**
	 * Switches the state of this CompareNode.
	 * If it tested the equality of its two children, it will now test their inequality and vice versa.
	 
	void switchState(){
		equality = !equality;
	}*/
			
	
	/**
	 * Changes the state of this CompareNote randomly.
	 * CompareNodes test the equality or the inequality of their children.
	 */
	void randomState(){
		setRandomState();
	}
	
	/**
	 * Returns whether this compareNode is equal to a given object.
	 * Equality means that the given object is also a compareNode 
	 * and that both pair of children are equal.
	 */
	public boolean equals(Object o) {
		if ( (o==null) || (!(o instanceof VariableCompareNode))) return false;
		VariableCompareNode cn = (VariableCompareNode)o;
		return (    getConstantChild().equals(cn.getConstantChild()) 
				 && getInputChild().equals( cn.getInputChild() ) );
	}
	
	/**
	 * Overrides the standard function to provide more information about this compareNode.
	 * Example: (x_2!=2) 
	 * Example: (x_7=3) 
	 */
	public String toString(){
		String rueckgabe;
		rueckgabe = "("+input.toString();
//		if (equality) rueckgabe = rueckgabe + "="; else rueckgabe = rueckgabe + "!=";
		if (less) rueckgabe = rueckgabe +"<";
		if (equal) rueckgabe = rueckgabe + "=";
		if (greater) rueckgabe = rueckgabe + ">";
		rueckgabe = rueckgabe + constant.toString() + ")";
		return rueckgabe;
	}
	
	/**
	 * Clones this compareNode
	 **/
	public Object clone(){
	 StaticConstantNode cn = (StaticConstantNode)constant.clone();
	 StaticInputNode in = (StaticInputNode)input.clone();
	 VariableCompareNode com = new VariableCompareNode(cn,in,less, equal, greater);
	 return com;
	}
}
