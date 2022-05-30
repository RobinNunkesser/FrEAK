/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.populationmanager;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import java.util.*;

/**
 * An abstract superclass for all population managers.
 * @author  Dirk
 */
abstract public class AbstractPopulationManager extends AbstractModule implements PopulationManager {

	/**
	 * The current population.
	 */
	private IndividualList population;

	/**
	 * A dictionary for the currently registered event listeners.
	 */
	private HashSet individualListEventListeners;

	/**
	 * Creates a new <code>PopulationManager</code> without a population.
	 * To initialize the population at the beginning of a run, 
	 * <code>initPopulation</code> has to be called.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public AbstractPopulationManager(Schedule schedule) {
		super(schedule);

		individualListEventListeners = new HashSet();
		population = null;
	}

	public void initPopulation(Initialization initialization) throws GraphException {
		population = new Population(getSchedule());
		if (initialization == null)
			throw new NullPointerException("Specified initialization operator is null.");
		IndividualList[] resultOfInitialization = initialization.process(new IndividualList[] {
		});
		if (resultOfInitialization == null || resultOfInitialization.length == 0)
			throw new OutputLengthMismatchException("Output of initialization operator is missing.");

		if (resultOfInitialization[0].isEmpty())
			throw new NoSuchIndividualException("Initialization operator provides an empty population.");
		population.addAllIndividuals(resultOfInitialization[0]);

		fireIndividualList(population);
	}

	/**
	 * @return  the population
	 * @uml.property  name="population"
	 */
	public IndividualList getPopulation() {
		return population;
	}

	/**
	 * Sets a new population.
	 * @param population  the new population.
	 * @uml.property  name="population"
	 */
	protected void setPopulation(IndividualList population) {
		this.population = population;
	}

	/**
	 * Adds a <code>IndividualListEventListener</code>.
	 * 
	 * @param l a <code>IndividualListEventListener</code> to be added.
	 */
	public void addIndividualListEventListener(IndividualListEventListener l) {
		individualListEventListeners.add(l);
	}

	/**
	 * Removes a <code>IndividualListEventListener</code>.
	 * 
	 * @param l a <code>IndividualListEventListener</code> to be removed.
	 */
	public void removeIndividualListEventListener(IndividualListEventListener l) {
		individualListEventListeners.remove(l);
	}

	/**
	 * Fires a <code>IndividualListEvent</code>.
	 * A <code>IndividualListEvent</code> is fired after the new population 
	 * has been created.
	 * 
	 * @param population the new population that has been created.
	 */
	protected void fireIndividualList(IndividualList population) {
		if (individualListEventListeners.size() > 0) {

			IndividualListEvent event = new IndividualListEvent(this, population);

			Iterator iter = individualListEventListeners.iterator();
			while (iter.hasNext()) {
				IndividualListEventListener listener = (IndividualListEventListener)iter.next();
				listener.individualList(event);
			}
		}
	}
}
