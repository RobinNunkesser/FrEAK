/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.population;

/**
 * Thrown when a requested individual can not be found.
 * 
 * @author Dirk
 */
public class NoSuchIndividualException extends RuntimeException {

	/**
	 * Constructs a <code>NoSuchIndividualException</code> without a detail message.
	 */
	public NoSuchIndividualException() {
		super();
	}

	/**
	 * Constructs a <code>NoSuchIndividualException</code> with a detail message.
	 * 
	 * @param message the detail message.
	 */
	public NoSuchIndividualException(String message) {
		super(message);
	}

}
