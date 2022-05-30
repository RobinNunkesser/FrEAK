/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import java.io.Serializable;

//import java.util.BitSet;


/**
 * Abstract class for Nodes with an arbitrary number of Children.
 * @author  Melanie
 */
public abstract class MultipleOperatorNode implements OperatorNode, Serializable {
 
	protected OperatorNodeVector children; 
//	private BitSet fulfilling = null;
	int numRows = -1;
	
	// Clone muss überschrieben werden.. 
	public abstract Object clone();
	
	/**
	 * Creates a new MultipleOperatorNode.
	 */
	public MultipleOperatorNode(){
		children = new OperatorNodeVector();
//		fulfilling = new BitSet();
	}
	
	
	/**
	 * Contructor that takes a OperatorNodeVector with OperatorNodes as input.
	 * The vector must not contain any other objects.
	 * @param childr List of children for the new Node.
	 */
	public MultipleOperatorNode(OperatorNodeVector childr){
		for (int i = 0; i < childr.size();i++){
	      if (! (childr.get(i) instanceof OperatorNode) ) {
				throw new RuntimeException("Vector childr contains Element that is not instance of OperatorNode");
			}
		}
		children = childr;
//		fulfilling = this.getValueBitset();
	}
	
	/*/**
	 * Gives the cashed BitSet that says which lines of the input are fulfilled by this node.
	 * BitSet gets updated with every operation performed on the node.
	 *
	public BitSet getCashedValueBitSet(){
		return fulfilling;
	}*/
	
	/**
	 * Gives the number of children of this DoubleOperatorNode.
	 */
	public int getNumberOfChildren(){
		return children.size();
	}

	/**
	 * Gives the child at position index.
	 * @param index of the child that is wanted.
	 */
	OperatorNode getChildAt(int index){
		if (index > children.size()) return null;
		if (! (children.get(index) instanceof OperatorNode) ) {
			throw new RuntimeException("Vector children contains Element that is not instance of OperatorNode");
			}
		return (OperatorNode)children.get(index);
	}
	
	/**
	 * Deletes one of this OrNode's children randomly.
	 * If this OrNode does not have any children, none is deleted and there will be no error message.
	 */
	OperatorNode getRandomChild(){
		if (children.size() == 0) return null;
		int nr = Data.nextRandInt(children.size());
		return children.get(nr);
	}
	
	/**
	 * Returns a OperatorNodeVector that contains copies of all ChildrenNodes of this node.
	 */
	OperatorNodeVector getCopyOfChildren(){
		OperatorNodeVector rueckgabe = new OperatorNodeVector();
		for (int i = 0; i< children.size();i++){
			rueckgabe.add( (OperatorNode)children.get(i).clone() );
		}
		return rueckgabe;
	}
	
	/**
	 * Returns wheter the <code>OperatorNode on</code> is a child of this MultipleOperatorNode.
	 * @param <code>on</code> <code>OperatorNode</code> that we look for in the children list
	 * @return true, if <code>on</code> is a child of this node
	 */
	public boolean contains(OperatorNode on){
		return children.contains(on);
	}
	
//	private void updateBitSet(){
//		fulfilling = this.getValueBitset();
//	}
	
	/**
	 * Concatenates the childrenlist of this node with the list onv.
	 * @param onv List of new OperatorNodes
	 */
	void addChildrenVector(OperatorNodeVector onv){
		children.addAll(onv);
//		updateBitSet();
	}
	
	/**
	 * Adds a given OperatorNode to the children of this OrNode. 
	 * @param child OperatorNode to be added
	 */
	public void addChild(OperatorNode child){
		if (child==null) return;
		children.add(child);
//		updateBitSet();
	}
	
	/**
	 * Deletes a given Operatornode from the list of childrin of this OrNode.
	 * @throws IllegalArgumentException if child is not a child of this OrNode
	 * @param child OperatorNode to be deleted
	 */
	public void deleteChild(OperatorNode child){
		if (child==null) return;
//		System.out.println(children.contains(child));		
		if (!(children.contains(child))) throw new IllegalArgumentException("Given OperatorNode is no child of this OrNode.");
		children.remove(child);
//		updateBitSet();
	}
	
	public void deleteAllChildren(){
		children.removeAllElements();
	}
	
	
	/**
	 * Deletes one of this OrNode's children randomly.
	 * If this OrNode does not have any children, none is deleted and there will be no error message.
	 */
	void deleteRandomChild(){
		if (children.size() == 0) return;
		int nr = Data.nextRandInt(children.size());
//		System.out.println("size" + children.size() + " kind nummer" + nr);
		deleteChild((OperatorNode)children.get(nr));
//		updateBitSet();
	}
	
	/**
	 * Returns the number of CompareNodes in this subtree.
	 */
	public int getSubtreeSize(){
		int rueckgabe = 0;
		int size = children.size();
	    for (int i = 0; i < size; i++){
	    	rueckgabe += (children.get(i)).getSubtreeSize();
	    }
		return rueckgabe;
	}

	/**
	 * @return  the children
	 * @uml.property  name="children"
	 */
	public OperatorNodeVector getChildren() {
		return children;
	}
	
	public void updateBitset(){
		this.numRows=Data.getNumRows();
		int size = children.size();
	    for (int i = 0; i < size; i++){
	    	(children.get(i)).updateBitset();
	    }		
	}
	
}