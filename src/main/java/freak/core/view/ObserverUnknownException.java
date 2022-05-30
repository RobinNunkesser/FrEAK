/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.view;

/**
 * Is thrown by <code>AbstractView.getCurrentValue()</code> if the observer is 
 * <code>null</code>.
 * 
 * @author Dirk
 */
public class ObserverUnknownException extends Exception {

	public ObserverUnknownException() {
		super();
	}

	public ObserverUnknownException(String message) {
		super(message);
	}

}
