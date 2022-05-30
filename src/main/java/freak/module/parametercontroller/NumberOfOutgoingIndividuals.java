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
import freak.core.parametercontroller.*;

/**
 * Observes an outport inside the operator graph and sets the controlled 
 * properties to the number of outgoing individuals.
 * 
 * @author Dirk
 */
public class NumberOfOutgoingIndividuals extends AbstractParameterController implements IndividualListEventListener {

	public static final String NAME = "Number";

	private int number;

	/**
	 * Constructs a new <code>NumberOfOutgoingIndividuals</code> controller.
	 * 
	 * @param s a link back to the current schedule.
	 */
	public NumberOfOutgoingIndividuals(OperatorGraph opGraph) {
		super(opGraph);

		addParameter(NAME, Integer.class);
	}

	public String getName() {
		return "Number of Outgoing Individuals";
	}

	public String getDescription() {
		return "Sets the controlled properties to the number of individuals leaving the specified outport.";
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "A source for individuals", IndividualListEvent.class, getSchedule().getPopulationManager());
	}

	public void individualList(IndividualListEvent evt) {
		number = evt.getIndividualList().size();
		setParameter(NAME, new Integer(number));
	}

}
