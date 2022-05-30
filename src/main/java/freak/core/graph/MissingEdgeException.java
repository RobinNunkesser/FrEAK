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
 * Is thrown when a desired edge is missing. 
 * 
 * @author Dirk
 */
public class MissingEdgeException extends GraphSyntaxException {
	/**
	 * Constructs a <code>MissingEdgeException</code> without a detail message.
	 */
	public MissingEdgeException() {
		super();
	}

	/**
	 * Constructs a <code>MissingEdgeException</code> with a detail message.
	 * @param message the detail message.
	 */
	public MissingEdgeException(String message) {
		super(message);
	}

}
