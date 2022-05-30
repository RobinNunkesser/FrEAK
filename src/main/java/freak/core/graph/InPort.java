/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import java.util.ArrayList;

import freak.core.population.*;

/**
 * 
 * @author Matthias
 */
public class InPort extends Port {

	/**
	 * Creates a new <code>InPort</code>.
	 * 
	 * @param operator a link back to the operator.
	 * @param number the index number of the port.
	 */
	public InPort(Operator operator, int number) {
		super(operator, number);
	}

	public void addPartner(Port partner) {
		if ((partner == null) || (partner instanceof OutPort)) {
			super.addPartner(partner);
		} else {
			throw new UnsupportedOperationException("Can't connect two inPorts.");
		}
	}
	
	/**
	 * Puts an IndividualList in the InPort.
	 * Throws an exception if InPort is not empty.
	 * @param individuals IndividualList to be put in the InPort.
	 */
	public void put(IndividualList individuals) {
		if (individuals == null) {
			throw new UnsupportedOperationException("Tried to send null to inPort.");
		}
		cache.add(individuals);
	}
	
	/**
	 * Get received Input.
	 * @return Received input. May be null.
	 */
	public IndividualList get() {
		
		if (cache.size() == 1) {
			return (IndividualList)cache.get(0);
		}
		
		if (cache.size() > 1) {
			int sum = 0;
			for (int i = cache.size() -1; i>=0; i--) {
				sum += ((IndividualList)cache.get(i)).size();			
			}
			IndividualList result = new Population(getOperator().getOperatorGraph().getSchedule(), sum);
			for (int i = cache.size() -1; i>=0; i--) {
				result.addAllIndividuals((IndividualList)cache.get(i));			
			}
			return result;
		}
		
		return new Population(getOperator().getOperatorGraph().getSchedule());
	}

	/**
	 * Clear received Input.
	 *
	 */
	public void clearCache() {
		cache.clear();
	}

	public void checkSyntax() throws GraphSyntaxException {
		if (!(getOperator() instanceof HasFloatingNumberOfInPorts) && getNumberOfPartners() == 0) {
			throw new MissingEdgeException("thrown by: " + this + ("inport"));
		}
	}

	private ArrayList cache = new ArrayList();

}