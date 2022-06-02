/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import freak.rinterface.control.RFlags;

import java.io.Serializable;
import java.util.BitSet;

/**
 * @author Melanie
 */
public class OrNode extends MultipleOperatorNode implements Serializable{
 
	/**
	 * Creates a new OrNode.
	 */
	public OrNode(){
		super();
	}
	
	
	/**
	 * Contructor that takes a Vector with OperatorNodes as input.
	 * The vector must not contain any other objects.
	 * @param childr List of children for the new Node.
	 */
	public OrNode(OperatorNodeVector childr){
		super(childr);
	}
	
	/**
	 * Evaluates the branch starting at this Or-Node.
	 * Gives true if at least one child returns true.
	 * Needs row for the evaluation of inpudNodes deeper in the tree.
	 * @param row The row of the function table for which the tree should be evaluated. 
	 */
	public boolean getValue(byte[] row){
		boolean rueckgabe = false;
		int size = children.size();
	    for (int i = 0; i < size; i++){
	    	if ( (children.get(i)).getValue(row) ) {
	    		rueckgabe = true;
	    	}
	    }
		return rueckgabe;
	}
	
	/**
	 * Gives a Bitset that says which rows of the input table are fullfilled by this node
	 * (independent of function value).
	 */
	public BitSet getValueBitset(){
		if (numRows == -1){
			numRows = Data.getNumRows();
		}
		int size = numRows;
		BitSet rueckgabe = new BitSet(size);
		rueckgabe.clear();
	    for (int i = 0; i < children.size(); i++){
	    	rueckgabe.or( (children.get(i)).getValueBitset() );
	    }
//		System.out.println(this.toString()+" liefert Bitset der L�nge "+rueckgabe.length()+ " mit "+rueckgabe.size() +" Bits (=Datenzeilen?)");	    
		return rueckgabe;
	}

	 /**
	  * Returns the maximum size of a monomial.
	  */
	public int getMaximumMonomialSize() {
		int rueckgabe = 0;
		int size = children.size();
		for (int i = 0; i < size; i++) {
			if ((children.get(i)).getSubtreeSize() > rueckgabe)
				rueckgabe = (children.get(i)).getSubtreeSize();
		}
		return rueckgabe;
	} 	
	
	/**
	 * Gives a randomly chosen child of this <code>OrNode</code>.
	 * @return an <code>AndNode</code> that is child of this <code>OrNode</code>.
	 */
	public AndNode getRandomChildAnd(){
		OperatorNode on = super.getRandomChild();
		if (on == null) return null;
		if (!( on instanceof AndNode)) throw new RuntimeException("OrNode has child that is not an AndNode!");
		return (AndNode)on;
	}
	
	public BitSet getLiteralBitSet () {
		BitSet rueckgabe = new BitSet(Data.getNumCompareNodes());
		rueckgabe.clear();
		OperatorNode kind;
		for (int i=0; i < children.size();i++){
			kind = children.get(i);
			rueckgabe.or(kind.getLiteralBitSet());
		}
		return rueckgabe;
	}

	// ************ overwritten java methods ******************************************************
	
	/**
	 * Returns whether this orNode is equal to a given Object o.
	 * Equality means that the given object is also an orNode
	 * and that all children are equal in the sense of operatorNode-Equality.
	 */
	public boolean equals(Object o){
		if ( (o==null) || (!(o instanceof OrNode))) return false;
		int size = children.size();
		OrNode on = (OrNode)o;
		if (size != on.getNumberOfChildren()) return false;
		boolean rueckgabe = true;
	    for (int i = 0; i < size; i++){
	    	if ( ! ( getChildAt(i).equals(on.getChildAt(i))) ) {
	    		rueckgabe = false;
	    	}
	    }
		return rueckgabe;		
	}	
	
	public String toDotGraph(){
		return "";
	}

	/**
	 * Overrides the standard function to provide more information about the structure of this andNode.
	 * String starts with "|", then gives the description of all children and ends with ";".
	 */
	public String toString(){
		String rueckgabe = "";//"(";
		if (RFlags.getUseCase()==RFlags.R) {
			OperatorNode kind;
			for (int i=0; i < children.size();i++){
				kind = children.get(i);
				rueckgabe= rueckgabe+kind.toString();
				if ((i<children.size()-1)) rueckgabe= rueckgabe+" | ";
			}
//			rueckgabe=rueckgabe+")";
		} else {
			rueckgabe = "|:";
			OperatorNode kind;
			for (int i=0; i < children.size();i++){
				kind = children.get(i);
				rueckgabe = rueckgabe+" "+kind.toString()+" ";
			}
			rueckgabe=rueckgabe+";";
		}
		return rueckgabe;
	}
	
	
	/**
	 * Clones this operatorNode.
	 * The inherit children vector is cloned.
	 */
	public Object clone(){
		OperatorNodeVector childr = new OperatorNodeVector();
		for (int i = 0; i < children.size(); i++){
			OperatorNode child = children.get(i);
			OperatorNode newchild = (OperatorNode)(child.clone());
			childr.add(newchild);
		}
		OrNode mop = new OrNode(childr);
		return mop;
	}
	
}
