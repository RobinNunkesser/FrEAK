/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.searchspace;

/**
 * This interface can be implemented by search spaces in order to make a 
 * dimension available. A dimension is a expedient information for the 
 * parametrisation of some modules
 *
 * @author Michael
 */
public interface HasDimension {

	/**
	 * This method returns the internal dimension of a search space.
	 *  
	 * @return the dimension of a search space.
	 */
	public int getDimension();
}
