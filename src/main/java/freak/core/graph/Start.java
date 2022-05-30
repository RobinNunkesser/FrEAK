/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import java.util.HashSet;
import java.util.Iterator;

import freak.core.event.IndividualListEvent;
import freak.core.event.IndividualListEventListener;
import freak.core.event.IndividualListEventSource;
import freak.core.population.IndividualList;

/**
 * Represents the graph's start operator.
 *  
 * @author Matthias, Dirk
 */
public class Start extends AbstractFork implements IndividualListEventSource {

	private HashSet individualListEventListeners = new HashSet();

	/**
	 * @param graph a link back to the operator graph.
	 */
	public Start(OperatorGraph graph) {
		super(graph);
		super.addOutPort(0);
	}

	/**
	 * Initializes the start operator with a population and fires 
	 * <code>IndividualListEvents</code>.
	 * 
	 * @param population population to put as input for the start operator.
	 */
	protected void putPopulation(IndividualList population) {
		getInPort(0).put(population);
		
		// send event if listeners are present
		if (individualListEventListeners.size() > 0) {

			//create event
			IndividualListEvent evt = new IndividualListEvent(this, population);

			//send event
			for (Iterator iter = individualListEventListeners.iterator(); iter.hasNext();) {
				((IndividualListEventListener)iter.next()).individualList(evt);
			}
		}
		
	}

	/**
	 * Performs a syntax check and throws exceptions
	 * if the operator is not in a correct state or
	 * if it is not linked correctly.
	 * 
	 * Note that this check is different from that one
	 * in AbstractOperator because we must not check the inport
	 * of the starting node. 
	 */
	public void checkSyntax() throws GraphSyntaxException {
		for (int i = 0; i < getNumberOfOutPorts(); i++) {
			getOutPort(i).checkSyntax();
		}
	}

	public String getName() {
		return "Start";
	}

	public String getDescription() {
		return "The start operator where individuals are put into the graph.";
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

	/* (non-Javadoc)
	 * @see freak.core.graph.AbstractOperator#process(freak.core.population.IndividualList[])
	 */
	public IndividualList[] process(IndividualList[] input) throws GraphException {
		return input;
	}
}
