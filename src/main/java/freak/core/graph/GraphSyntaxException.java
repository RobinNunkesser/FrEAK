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
 * Thrown when a syntactical error in the <code>OperatorGraph</code>
 * was detected.  
 * 
 * @author Matthias
 */

public class GraphSyntaxException extends GraphException {

	/**
	 * Constructs an <code>GraphSyntaxException</code> with no detail message. 
	 */
	public GraphSyntaxException() {
		super();
	}

	/**
	 * Constructs an <code>GraphSyntaxException</code> with the specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public GraphSyntaxException(String message) {
		super(message);
	}
}
