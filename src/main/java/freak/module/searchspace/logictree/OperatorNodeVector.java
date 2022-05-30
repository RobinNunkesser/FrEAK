/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import java.io.Serializable;
import java.util.Vector;
import java.util.Iterator;

/**
 * Encapsulates a Vector such that only OperatorNodes can be stored.
 * @author Melanie
 */
public class OperatorNodeVector implements Serializable{
	
	Vector vector;
	
	public OperatorNodeVector(){
		vector = new Vector();
	}
	
	public void add(OperatorNode on){
		vector.add(on);
	}
	
	public OperatorNode get(int i){
		Object o = vector.get(i);
		if (! (o instanceof OperatorNode)){
			throw new RuntimeException("OperatorNodeVector contains Object from different class?!?");
		}
		return (OperatorNode)o;
	}
	
	public int size(){
		return vector.size();
	}

	public int indexOf(OperatorNode on){
		return vector.indexOf(on);
	}
	
	public boolean contains(OperatorNode on){
		return vector.contains(on);
	}
	
	public void remove(OperatorNode on){
		if (contains(on)) {
			vector.remove(on);
		}
	}
	
	public void removeAllElements(){
		vector.removeAllElements();
	}

	private Vector getInternList(){
		return vector;
	}
	
	public boolean addAll(OperatorNodeVector onv){
		boolean rueckgabe = vector.addAll(onv.getInternList());
		return rueckgabe;
	}
	
	public Iterator iterator(){
		return vector.iterator();
	}
	public OperatorNode[] toArray(){
		Object[] asObjects = vector.toArray();
		OperatorNode[] asNodes = new OperatorNode[asObjects.length];
		for (int i = 0; i < asObjects.length; i++){
			if (! (asObjects[i] instanceof OperatorNode)){
				throw new RuntimeException("OperatorNodeVector contains Object from different class?!?");
			}
			asNodes[i] = (OperatorNode)asObjects[i];
		}
		return asNodes;
	}

}
