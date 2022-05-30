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
 * Is thrown if a module does not support the environment
 * represented by the schedule.
 * 
 * @author Patrick
 */

public class UnsupportedEnvironmentException extends Exception {

	/**
	 * Constructs a new <code>UnsupportedEnvironmentException</code> without a 
	 * detail message.
	 */
	public UnsupportedEnvironmentException() {
		super();
	}

	/**
	 * Constructs a new <code>UnsupportedEnvironmentException</code> with the 
	 * specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public UnsupportedEnvironmentException(String message) {
		super(message);
	}

}
