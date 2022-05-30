/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import java.util.BitSet;

/**
 * @author Melanie
 */
public interface OperatorNode extends Node {

    /**
     * Evaluates the subtree starting at this Node acccording to the given row.
     */
	public boolean getValue(byte[] row);
	public int getSubtreeSize();
	public BitSet getValueBitset();
	public void updateBitset();
	public BitSet getLiteralBitSet ();
    // Die Unterklassen müssen clone überschreiben.
	public Object clone();
	
}
