/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.observer;

/**
 * Is thrown when an observer and a view do not match.
 * 
 * @author Dirk
 */
public class ObserverViewMismatchException extends Exception {

	/**
	 * Constructs a new <code>ObserverViewMismatchException</code> without a 
	 * detail message.
	 */
	public ObserverViewMismatchException() {
		super();
	}

	/**
	 * Constructs a new <code>ObserverViewMismatchException</code> with the 
	 * specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public ObserverViewMismatchException(String message) {
		super(message);
	}

}
