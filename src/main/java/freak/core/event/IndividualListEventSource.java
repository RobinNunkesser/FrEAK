/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.event;

/**
 * An event source for <code>IndividualListEvents</code>.
 * 
 * @see freak.core.event.IndividualListEvent
 * @author Dirk
 */
public interface IndividualListEventSource extends EventSource {
	void addIndividualListEventListener(IndividualListEventListener l);
	void removeIndividualListEventListener(IndividualListEventListener l);
}
