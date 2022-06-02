/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

/**
 * @author Melanie
 */

import freak.rinterface.control.RFlags;

import java.io.Serializable;
import java.util.BitSet;

// this class does not support allowed operators. 
// it does also not support changing the operator combination.
// this class simply gets the operator combination during constructing and is only evaluated then.
/**
 * @author  nunkesser
 */
public class StaticCompareNode implements OperatorNode, Serializable {

	private StaticConstantNode constant;

	private StaticInputNode input;

	private int index;
	private boolean less;
	private boolean equal;
	private boolean greater;
	
	private BitSet fulfilling = null;

	/**
	 * Creates a new compareNode with given children. The last three parameters
	 * specify the operator combination. The input table is needed to create the
	 * BitSet saying which rows are fullfilled.
	 * @param cn      constant child of this compareNode
	 * @param in      input child of this compareNode
	 * @param less    if true, this compare Node interprets in < cn as true
	 * @param equal   if true, this compare Node interprets in = cn as true
	 * @param greater if true, this compare Node interprets in > cn as true
	 * @param numRows number of rows in the input table
	 * @param rows    input table
	 */
	public StaticCompareNode(StaticConstantNode cn, StaticInputNode in, boolean less,
			boolean equal, boolean greater,int numRows, byte[][] rows) {
		constant = cn;
		input = in;
		this.less = less;
		this.equal = equal;
		this.greater = greater;
		createBitset(numRows,rows);
	}	
	
	/**
	 * Creates a new compareNode with given children. The last three parameters
	 * specify the operator combination. 
	 * The BitSet says which rows are fullfilled by this node.
	 * @param cn      constant child of this compareNode
	 * @param in      input child of this compareNode
	 * @param less    if true, this compare Node interprets in < cn as true
	 * @param equal   if true, this compare Node interprets in = cn as true
	 * @param greater if true, this compare Node interprets in > cn as true
	 * @param fullf   saying which rows are fullfiled by this node
	 */
	public StaticCompareNode(StaticConstantNode cn, StaticInputNode in, boolean less,
			boolean equal, boolean greater, BitSet fullf) {
		constant = cn;
		input = in;
		this.less = less;
		this.equal = equal;
		this.greater = greater;
		this.fulfilling = fullf;
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
		boolean a = ( equal   && (i == j));
		boolean b = ( less    && (i <  j));
		boolean c = ( greater && (i >  j));
		return ( a || b || c);
	}	

	/**
	 * Creates a BitSet that contains whether this compareNode fullfilles the rows of the input table.
	 * @param numRows number of rows in the input table
	 * @param rows    input table
	 */
	private void createBitset(int numRows, byte[][] rows) {
//		System.out.println("Update with "+numRows+"="+rows.length+" Data rows");
		if (numRows != rows.length) {
			return;			
		}
		fulfilling = new BitSet(numRows);
		for (int i = 0; i < numRows; i++) {
			fulfilling.set(i, getValue(rows[i]));
		}
	}
	
	/**
	 * Gives a bitset that says which input rows are fullfilled by this compareNode.
	 * This does not take into accout which function value the rows have!
	 * @return
	 */
	public BitSet getValueBitset(){
//		System.out.println(this.toString()+" liefert Bitset der L�nge "+fulfilling.length()+ " mit "+fulfilling.size() +" Bits (=Datenzeilen?)");
		return fulfilling;
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
	
	public BitSet getLiteralBitSet () {
		BitSet rueckgabe = new BitSet(Data.getNumCompareNodes());
		rueckgabe.clear();
		rueckgabe.set(Data.getIndexOfCompareNode(this));
		return rueckgabe;
	}

	// ************ overwritten java methods ******************************************************
	
	/**
	 * Returns whether this compareNode is equal to a given object.
	 * Equality means that the given object is also a compareNode 
	 * and that both pair of children are equal.
	 */
	public boolean equals(Object o) {
		if ( (o==null) || (!(o instanceof StaticCompareNode))) return false;
		StaticCompareNode cn = (StaticCompareNode)o;
		return (    getConstantChild().equals(cn.getConstantChild()) 
				 && getInputChild().equals( cn.getInputChild() ) );
	}
	
	/**
	 * Overrides the standard function to provide more information about this compareNode.
	 * Example: (x_2!=2) 
	 * Example: (x_7=3) 
	 */
	public String toString(){
		String rueckgabe="("+input.toString();
		if (RFlags.getUseCase()==RFlags.R) {
			if (less&&!greater) rueckgabe = rueckgabe +"<";
			if (equal) rueckgabe = rueckgabe + "==";
			if (greater&&!less) rueckgabe = rueckgabe + ">";
			if (greater&&less) rueckgabe = rueckgabe + "!=";
			rueckgabe = rueckgabe + constant.toString() + ")";
		} else {
			if (less) rueckgabe = rueckgabe +"<";
			if (equal) rueckgabe = rueckgabe + "=";
			if (greater) rueckgabe = rueckgabe + ">";
			rueckgabe = rueckgabe + constant.toString() + ")";
		}
		return rueckgabe;
	}
	
	/**
	 * Clones this compareNode
	 **/
	public Object clone(){
	 StaticConstantNode cn = (StaticConstantNode)constant.clone();
	 StaticInputNode in = (StaticInputNode)input.clone();
	 // cloning bitsets creates a real copy according to java api
	 StaticCompareNode com = new StaticCompareNode(cn,in,less, equal, greater, (BitSet)fulfilling.clone());
	 com.setIndex(this.index);
	 return com;
	}

	public void updateBitset() {
		createBitset(Data.getNumRows(),Data.getValues());
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
