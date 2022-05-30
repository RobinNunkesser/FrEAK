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

import java.awt.*;
import java.util.*;
import org.jgraph.graph.*;

/**
 * @author  Andrea
 */
public class FreakPort extends DefaultPort {

	protected Port port;

	private OperatorGraphCell cell;

	/**
	 * 
	 */
	public FreakPort(Port port, OperatorGraphCell cell) {
		super(port);
		this.port = port;
		this.cell = cell;
		GraphConstants.setAbsolute(attributes, true);
		//GraphConstants.setOpaque(attributes, true);
		arrange();
	}

	public void arrange() {
		Map cellAttributes = cell.getAttributes();
		if (port instanceof InPort) {
			GraphConstants.setOffset(attributes, new Point((1 + port.getNumber()) * GraphConstants.getBounds(cellAttributes).width / (cell.getOperator().getNumberOfInPorts() + 1), 0));
		} else {
			GraphConstants.setOffset(attributes, new Point((1 + port.getNumber()) * GraphConstants.getBounds(cellAttributes).width / (cell.getOperator().getNumberOfOutPorts() + 1), GraphConstants.getBounds(cellAttributes).height));
		}
	}

	/**
	 * @return  the port
	 * @uml.property  name="port"
	 */
	public Port getPort() {
		return port;
	}
}
