/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

/**
 * Interface for Operators that have as many OutPorts as incoming connections.
 * 
 * @author Andrea, Matthias
 */
public interface HasFloatingNumberOfOutPorts {

	/**
	 * Appends an OutPort to the list of OutPorts. 
	 */
	public void addOutPort();

	/**
	 * Inserts an OutPort with the specified number.
	 * Increases the number of the OutPort, which currently has the number index 
	 * (if any) and any subsequent OutPorts, by one.
	 * 
	 * @param index position at which the port is inserted.
	 */
	public void addOutPort(int index);

	/**
	 * Removes the OutPort with the specified number.
	 * Decreases the number of any subsequent OutPorts by one.
	 *  
	 * @param index number of port to remove.
	 */
	public void removeOutPort(int index);
	
	/**
	 * Exchanges the outports with index <code>x</code> <code>y</code>.
	 * @param x index of first port to exchange with second one.
	 * @param y index of second port to exchange with first one.
	 */
	public void exchangeOutPorts(int x, int y);


}
