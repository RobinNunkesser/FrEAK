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
 * Is thrown when sombody tries to connect/disconnect ports that shouldn't be
 * connected/disconnected (e.g.&nbsp;connect two inports). 
 * 
 * @author Matthias
 */
public class PortConnectException extends GraphException {
	/**
	 * Constructs a <code>PortConnectException</code> without a detail message.
	 */
	public PortConnectException() {
		super();
	}

	/**
	 * Constructs a <code>PortConnectException</code> with a detail message.
	 * @param message the detail message.
	 */
	public PortConnectException(String message) {
		super(message);
	}

}
