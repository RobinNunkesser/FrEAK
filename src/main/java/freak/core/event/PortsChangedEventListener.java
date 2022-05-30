/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.event;

import freak.core.graph.*;

/**
 * 
 * @author Matthias
 */
public interface PortsChangedEventListener extends EventListener {
	void portAdded(Operator source, Port port);
	void portRemoved(Operator source, Port port);
	void portsExchanged(Operator source, Port port1, Port port2);
}
