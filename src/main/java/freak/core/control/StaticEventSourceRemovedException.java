/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.control;

/**
 * Is thrown when a static event source is replaced by <code>null</code>.
 * 
 * @author Dirk
 */
public class StaticEventSourceRemovedException extends NullPointerException {

	public StaticEventSourceRemovedException() {
		super();
	}

	public StaticEventSourceRemovedException(String message) {
		super(message);
	}
}
