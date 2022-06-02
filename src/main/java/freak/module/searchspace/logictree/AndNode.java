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
public class AndNode extends MultipleOperatorNode implements Serializable{	
	
	/**
	 * Constructor for andNodes without parameters.
	 */
	public AndNode(){
		super();
	}	
	
	/**
	 * Contructor that takes a Vector with OperatorNodes as input.
	 * The vector must not contain any other objects.
	 * @param childr List of children for the new Node.
	 */
	public AndNode(OperatorNodeVector childr){
		super(childr);
	}

	
	/**
	 * Evaluates the branch starting at this And-Node.
	 * Gives true if all children return true.
	 * Needs row for the evaluation of inpudNodes deeper in the tree.
	 * @param row The row of the function table for which the tree should be evaluated. 
	 */
	public boolean getValue(byte[] row){
		boolean rueckgabe = true;
		int size = children.size();
	    for (int i = 0; i < size; i++){
	    	if ( ! (children.get(i)).getValue(row) ) {
	    		rueckgabe = false;
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
		rueckgabe.set(0,size); // first number inclusively, second number exclusively
	    for (int i = 0; i < children.size(); i++){
	    	rueckgabe.and( (children.get(i)).getValueBitset() );
	    }
//		System.out.println(this.toString()+" liefert Bitset der L�nge "+rueckgabe.length()+ " mit "+rueckgabe.size() +" Bits (=Datenzeilen?)");
		return rueckgabe;
	}
	
	/**
	 * Gives a randomly chosen child of this AndNode.
	 * @return a <code>StaticCompareNode</code> that is child of this <code>AndNode</code>.
	 */
	public StaticCompareNode getRandomChildComp(){
		OperatorNode on = super.getRandomChild();
		if (on == null) return null;
		if (!( on instanceof StaticCompareNode)) throw new RuntimeException("AndNode has child that is not StaticCompareNode!");
		return (StaticCompareNode)on;
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

	/**
	 * Returns whether this AndNode is equal to a given Object o.
	 * Equality means that the given object is also an AndNode
	 * and that all children are equal in the sense of operatorNode-Equality.
	 */
	public boolean equals(Object o){
		if ( (o==null) || (!(o instanceof AndNode))) return false;
		int size = children.size();
		AndNode an = (AndNode)o;
		if (size != an.getNumberOfChildren()) return false;
		boolean rueckgabe = true;
	    for (int i = 0; i < size; i++){
	    	if ( ! ( getChildAt(i).equals(an.getChildAt(i))) ) {
	    		rueckgabe = false;
	    	}
	    }
		return rueckgabe;		
	}	
	
	/**
	 * Overrides the standard function to provide more information about the structure of this andNode.
	 * String starts with "&", then gives the description of all children and ends with ";".
	 */
	public String toString(){
		String rueckgabe = (children.size()==1)?"":"(";
		OperatorNode kind;
		if (RFlags.getUseCase()==RFlags.R) {
			for (int i=0; i < children.size();i++){
				kind = children.get(i);
				rueckgabe= rueckgabe+kind.toString();
				if ((i<children.size()-1)) rueckgabe= rueckgabe+" & ";
			}
			rueckgabe=rueckgabe+((children.size()==1)?"":")");
		} else {
			rueckgabe = "&:";
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
		AndNode mop = new  AndNode(childr);
		return mop;
	}
}
