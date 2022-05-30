/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.populationmanager;

import freak.core.control.*;
import freak.core.graph.*;
import freak.core.population.*;
import freak.core.populationmanager.*;

/**
 * Implements the default population management strategy treating the 
 * population as a whole.
 * 
 * @author Dirk 
 */
public class DefaultPopulationManager extends AbstractPopulationManager {

	/**
	 * Constructs a new <code>DefaultPopulationManager</code>. 
	 */
	public DefaultPopulationManager(Schedule schedule) {
		super(schedule);
	}

	/**
	 * Creates new generation's population by putting the population into the 
	 * operator graph as a whole.
	 * When this is done, an <code>IndividualListEvent</code> is fired.
	 * 
	 * @return the new population. 
	 * @throws GraphException if an error within the graph occurs.
	 * @throws NoSuchIndividualException if the population is empty.
	 */
	public void createNewGeneration() throws GraphException, NoSuchIndividualException {
		if (getPopulation().isEmpty())
			throw new NoSuchIndividualException("Population is empty.");

		// call the operator graph
		IndividualList result = getSchedule().getOperatorGraph().process(getPopulation());

		setPopulation(result);

		fireIndividualList(getPopulation());
	}

	public String getDescription() {
		return "No specific population model. The population is treated as a whole.";
	}

	public String getName() {
		return "Default Population Model";
	}

}
