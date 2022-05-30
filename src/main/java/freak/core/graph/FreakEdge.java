/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.graph;

import org.jgraph.graph.*;

/**
 * @author  Andrea
 */
public class FreakEdge extends DefaultEdge {

	protected FreakPort sourcePort;
	protected FreakPort targetPort;

	/**
	 * 
	 */
	public FreakEdge(FreakPort sourcePort, FreakPort targetPort) {
		super();
		this.sourcePort = sourcePort;
		this.targetPort = targetPort;
	}

	/**
	 * @return
	 * @uml.property  name="sourcePort"
	 */
	public FreakPort getSourcePort() {
		return sourcePort;
	}

	/**
	 * @return
	 * @uml.property  name="targetPort"
	 */
	public FreakPort getTargetPort() {
		return targetPort;
	}

}
