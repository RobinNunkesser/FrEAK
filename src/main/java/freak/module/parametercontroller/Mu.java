/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.parametercontroller;

import freak.core.event.*;
import freak.core.graph.*;
import freak.core.parametercontroller.AbstractParameterController;

/**
 * Sets the controlled properties to the number of individuals put into the 
 * operator graph.
 * 
 * @author Dirk
 */
public class Mu extends AbstractParameterController implements IndividualListEventListener {

	public static final String NAME = "Mu";

	private int mu;

	/**
	 * Constructs a new <code>Mu</code> controller.
	 * 
	 * @param s a link back to the current schedule.
	 */
	public Mu(OperatorGraph opGraph) {
		super(opGraph);

		addParameter(NAME, Integer.class);
	}

	public void createEvents() {
		getSchedule().getEventController().addEvent(this, IndividualListEvent.class, opGraph.getStart());
	}

	public String getDescription() {
		return "Sets the number of individuals put into the operator graph.";
	}

	public String getName() {
		return "Mu";
	}

	public void individualList(IndividualListEvent evt) {
		mu = evt.getIndividualList().size();
		setParameter(NAME, new Integer(mu));
	}

}
