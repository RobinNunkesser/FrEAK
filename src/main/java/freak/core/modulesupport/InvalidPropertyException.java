/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport;

/**
 * Is thrown if a module contains invalid property settings that must be 
 * corrected by the user before doing something else.
 * 
 * @author Dirk
 */

public class InvalidPropertyException extends Exception {

	/**
	 * Constructs a new <code>InvalidPropertyException</code> without a 
	 * detail message.
	 */
	public InvalidPropertyException() {
		super();
	}

	/**
	 * Constructs a new <code>InvalidPropertyException</code> with the 
	 * specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public InvalidPropertyException(String message) {
		super(message);
	}

}
