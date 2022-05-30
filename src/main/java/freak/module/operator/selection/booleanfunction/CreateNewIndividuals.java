/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.selection.booleanfunction;

import freak.core.control.Schedule;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.module.searchspace.BooleanFunction;
import freak.module.searchspace.BooleanFunctionGenotype;


/**
 *
 * This class implements the <code>PopulationSelection</code>. 
 * It gets an
 * <code>individualList</code> "original" and returns all 
 * individuals that are in population <code>noOfPopulation</code>.
 *
 * @author Melanie
 *
 */
public class CreateNewIndividuals extends Selection implements Configurable {
	
	private int noOfNewInd = 1;
	
	/**
	 * The constructor of the class <code>CreateNewIndividuals</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public CreateNewIndividuals(OperatorGraph graph) {
		super(graph);
		// Add ports
		super.addInPort();
		super.addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);		
	}
	
	/**
	 * This method does the selection described above.
	 *
	 * @param original this is the <code>individualList</code> the individuals
	 * are chosen from.
	 * @return this is the list of the chosen individuals.
	 */
	public IndividualList[] process(IndividualList[] original) {
			
		// create the resulting lists ...
		IndividualList result[] = new IndividualList[1];
		
		// copy old individuals
		result[0] = original[0];
		
		// add new individuals
		for (int i = original.length; i < original.length + noOfNewInd; i++){
			BooleanFunction bfs = (BooleanFunction)(graph.getSchedule()).getGenotypeSearchSpace();
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)bfs.getRandomGenotype();
			Individual ind = new Individual(graph.getSchedule(),bfg, new Individual[]{});
			result[0].addIndividual(ind);
//			return new Individual(graph.getSchedule(), copy, new Individual[] { ind });
		}
		return result;
	}
	
	/**
	 *
	 * Returns the number of individuals which shall be created.
	 *
	 * @return a wrapped integer containing the number of individuals to be
	 * chosen.
	 */
	public Integer getPropertyNoOfNewInd() {
		return new Integer(noOfNewInd);
	}
	
	/**
	 * Sets the number of individuals which will be created. The number must be non negative.
	 *
	 * @param noOfIndividualsToSelect a wrapped integer containing the number
	 * of individuals which are chosen
	 */
	public void setPropertyNoOfNewInd(Integer noOfNewInd) {
		if (noOfNewInd.intValue() >= 0)
		  this.noOfNewInd = noOfNewInd.intValue();
	}
	
	public String getShortDescriptionForNoOfNewInd() {
		return "New individuals";
	}
	
	public String getLongDescriptionForNoOfNewInd() {
		return "The number of new individuals to create.";
	}
	
	public String getName() {
		return "CreateNewIndividuals";
	}
	
	public String getDescription() {
		return "Creates new individuals and puts them at the end of the given individual list.";
	}
	
}
