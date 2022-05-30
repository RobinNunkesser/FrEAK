/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import java.util.*;

import freak.core.event.*;
import freak.core.population.*;

/**
 * The output of an <code>Operator</code> is send through the <code>OutPort</code>s.
 * An OutPort can only connect to an <code>InPort</code> (which means the partner is set
 * to an InPort).  
 * 
 * @author Matthias, Dirk
 */

public class OutPort extends Port implements IndividualListEventSource {

	private HashSet individualListEventListeners = new HashSet();

	/**
	 * Creates a new <code>OutPort</code>.
	 * 
	 * @param operator a link back to the operator.
	 * @param number the index number of the port.
	 */
	public OutPort(Operator operator, int number) {
		super(operator, number);
	}

	/**
	 * Sends a list of individuals to the communication partner (if present).
	 * 
	 * @param individuals the list of individuals to send.
	 */
	public void send(IndividualList individuals) {
		
		// send event if listeners are present
		if (individualListEventListeners.size() > 0) {

			//create event
			IndividualListEvent evt = new IndividualListEvent(this, individuals);

			//send event
			for (Iterator iter = individualListEventListeners.iterator(); iter.hasNext();) {
				((IndividualListEventListener)iter.next()).individualList(evt);
			}
		}

		for (Iterator iter = getPartnerIterator(); iter.hasNext();) {
			((InPort)iter.next()).put(individuals);
		}		
	}

	public void addPartner(Port partner) {
		if ((partner == null) || (partner instanceof InPort)) {
			super.addPartner(partner);
		} else {
			throw new UnsupportedOperationException("Can't connect two outPorts.");
		}
	}

	/* (non-Javadoc)
	 * @see freak.core.event.IndividualListEventSource#addIndividualListEventListener(freak.core.event.IndividualListEventListener)
	 */
	public void addIndividualListEventListener(IndividualListEventListener l) {
		individualListEventListeners.add(l);
	}

	/* (non-Javadoc)
	 * @see freak.core.event.IndividualListEventSource#removeIndividualListEventListener(freak.core.event.IndividualListEventListener)
	 */
	public void removeIndividualListEventListener(IndividualListEventListener l) {
		individualListEventListeners.remove(l);
	}
}