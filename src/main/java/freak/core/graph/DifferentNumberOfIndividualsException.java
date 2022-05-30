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
 * This exception is thrown if the sizes of the individual lists differ.
 *
 *  @author Heiko
 */
public class DifferentNumberOfIndividualsException extends GraphException {

	/**
	 * Constructs a <code>DifferentNumberOfIndividualsException</code> with a
	 * detail message.
	 * @param message the detail message.
	 */
	public DifferentNumberOfIndividualsException(String message) {
		super(message);
	}
}
