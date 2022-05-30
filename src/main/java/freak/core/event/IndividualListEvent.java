/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.event;

import freak.core.population.*;

/**
 * Is thrown when new individuals are created or received.
 * @author  Dirk
 */
public class IndividualListEvent extends Event {

	private IndividualList individualList;

	public IndividualListEvent(IndividualListEventSource source, IndividualList individualList) {
		this.source = source;
		this.individualList = individualList;
	}

	/**
	 * @return  the individualList
	 * @uml.property  name="individualList"
	 */
	public IndividualList getIndividualList() {
		return individualList;
	}

	/**
	 * @return  the source
	 * @uml.property  name="source"
	 */
	public IndividualListEventSource getSource() {
		return source;
	}

	private IndividualListEventSource source;
}
