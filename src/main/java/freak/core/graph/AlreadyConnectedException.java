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
 * Is thrown when the GUI tries to connect a Port which is already connected. 
 * 
 * @author Matthias
 */
public class AlreadyConnectedException extends PortConnectException {
	/**
	 * Constructs a <code>AlreadyConnectedException</code> without a detail message.
	 */
	public AlreadyConnectedException() {
		super();
	}

	/**
	 * Constructs a <code>AlreadyConnectedException</code> with a detail message.
	 * @param message the detail message.
	 */
	public AlreadyConnectedException(String message) {
		super(message);
	}
}
