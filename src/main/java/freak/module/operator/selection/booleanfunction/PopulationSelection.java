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
public class PopulationSelection extends Selection implements Configurable {
	
	private int noOfPopulation = 0;
	
	/**
	 * The constructor of the class <code>PopulationSelection</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public PopulationSelection(OperatorGraph graph) {
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
			
		if (original[0].size() == 0) {
			IndividualList result[] = new IndividualList[1];
			result[0] = new Population(getOperatorGraph().getSchedule());
			return result;
		}
		
		// create the resulting lists ...
		IndividualList result[] = new IndividualList[1];
		
		// convert the original-list to array (==> faster)
		Individual[] origin = original[0].toArray();
		
/*		int noOfIndToSelect = 0;
					
		for (int i = 0; i < origin.length; i++) {
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)(origin[i].getGenotype());			
			if (bfg.getPopulation()==noOfPopulation){
				result[0].addIndividual(origin[i]);
				noOfIndToSelect++;
			}
		}*/

		// create a new IndividualList
		//result[0] = new Population(graph.getSchedule(), noOfIndToSelect);
		result[0] = new Population(graph.getSchedule());
		
		for (int i = 0; i < origin.length; i++) {
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)(origin[i].getGenotype());			
//			System.out.print(bfg.getPopulation()+" ");
			if (bfg.getPopulation()==noOfPopulation) {
				result[0].addIndividual(origin[i]);
			}
		}
//		System.out.println();

		return result;
	}
	/**
	 *
	 * Returns the number of individuals which are chosen.
	 *
	 * @return a wrapped integer containing the number of individuals to be
	 * chosen.
	 */
	public Integer getPropertyNoOfPopulation() {
		return new Integer(noOfPopulation);
	}
	
	/**
	 * Sets the number of individuals which will be chosen. At least one
	 * individual has to be chosen, so setting to a value less or equal to
	 * zero will set the value to 1.
	 *
	 * @param noOfIndividualsToSelect a wrapped integer containing the number
	 * of individuals which are chosen
	 */
	public void setPropertyNoOfPopulation(Integer noOfPopulation) {
		this.noOfPopulation = noOfPopulation.intValue();
	}
	
	public String getShortDescriptionForNoOfPopulation() {
		return "Selected Population";
	}
	
	public String getLongDescriptionForNoOfPopulation() {
		return "All individuals of this population will be returned.";
	}
	
	public String getName() {
		return "PopulationSelection";
	}
	
	public String getDescription() {
		return "Selects all individuals from the specified population.";
	}
	
}
