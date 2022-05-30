/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.populationmanager;

import java.util.*;

import freak.core.control.*;
import freak.core.graph.*;
import freak.core.population.*;
import freak.core.populationmanager.*;

/**
 * Implements parallel multistarts by putting the individuals separately into 
 * the operator graph.
 * 
 * @author Dirk 
 */
public class ParallelMultistarts extends AbstractPopulationManager {

	/**
	 * Constructs a new <code>ParallelMultistarts</code>. 
	 */
	public ParallelMultistarts(Schedule schedule) {
		super(schedule);
	}

	/**
	 * Creates new generation's population by putting the individuals into the 
	 * operator graph seperately and thus implementing parallel independent 
	 * multistarts.
	 * When this is done, an <code>IndividualListEvent</code> is fired.
	 * 
	 * @return the new population. 
	 * @throws GraphException if an error within the graph occurs.
	 * @throws NoSuchIndividualException if the population is empty.
	 */
	public void createNewGeneration() throws GraphException, NoSuchIndividualException {
		if (getPopulation().isEmpty())
			throw new NoSuchIndividualException("Population is empty.");

		// iterate over the individuals of the old population
		Iterator iter = getPopulation().iterator();
		setPopulation(new Population(getSchedule()));
		while (iter.hasNext()) {
			Individual individual = (Individual)iter.next();
			IndividualList result = getSchedule().getOperatorGraph().process(new Population(getSchedule(), individual));
			getPopulation().addAllIndividuals(result);
		}

		fireIndividualList(getPopulation());
	}

	public String getDescription() {
		return "Parallel independent multistarts of populations with size 1.";
	}

	public String getName() {
		return "Parallel Multistarts";
	}

}
