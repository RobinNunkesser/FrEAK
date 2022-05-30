/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

/**
 * @author Melanie
 */
public interface AtomicNode extends Node {

	/**
	 * Returns the integer value of this atomic node.
	 */
	public int getValue(byte[] row);
	
	/**
	 * Clones this atomicNode.
	 */
	public Object clone();
	
}
