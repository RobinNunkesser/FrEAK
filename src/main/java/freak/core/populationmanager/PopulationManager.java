/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.populationmanager;

import freak.core.event.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.population.*;

/**
 * A <code>PopulationManager</code> controls the population and creates the 
 * population of the new generation by putting individuals into the 
 * operator graph. 
 * The <code>PopulationManager</code> can select arbitrary sets of individuals 
 * to be processed by the operator graph and thus subclasses can implement 
 * arbitrary population management strategies, i.e. parallel multistarts or 
 * subpopulations.
 * 
 * @author Dirk
 */
public interface PopulationManager extends Module, IndividualListEventSource {
	/**
	 * Initializes the population by using the specified 
	 * <code>Initialization</code> operator.
	 * An <code>IndividualListEvent</code> is fired after the new population is
	 * created.
	 * 
	 * @param initialization an Initialization operator that is used to create the starting population.
	 * @throws GraphException if there is a problem with the specified initialization operator.
	 * @throws NoSuchIndividualException if the specified initialization operator provides an empty population.
	 */
	public void initPopulation(Initialization initialization) throws GraphException, NoSuchIndividualException;

	/**
	 * Creates new generation's population.
	 * 
	 * @return the new population.
	 * @throws GraphException if an error within the graph occurs.
	 */
	public void createNewGeneration() throws GraphException;

	/**
	 * Returns the current population.
	 * 
	 * @return the current population.
	 */
	public IndividualList getPopulation();
}