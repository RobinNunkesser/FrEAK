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
public class SizeSelection extends Selection implements Configurable {
	
	private int maxSize  = 5;
	
	/**
	 * The constructor of the class <code>PopulationSelection</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public SizeSelection(OperatorGraph graph) {
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
		
		// create a new IndividualList
		//result[0] = new Population(graph.getSchedule(), noOfIndToSelect);
		result[0] = new Population(graph.getSchedule());
		
		for (int i = 0; i < origin.length; i++) {
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)(origin[i].getGenotype());			
//			System.out.print(bfg.getPopulation()+" ");
			if (bfg.evaluateSize() <= maxSize) {
				result[0].addIndividual(origin[i]);
			}
		}
//		System.out.println();

		return result;
	}
	
	/**
	 * Returns the number of individuals which are chosen.
	 * @return a wrapped integer containing the maximal number of monoms that an individual may have to get into the output
	 */
	public Integer getPropertyMaxSize() {
		return new Integer(maxSize);
	}
	
	/**
	 * Sets the maximal number of monoms that an individual may have to get into the output.
	 *
	 * @param maxSize a wrapped integer containing the maximal number of monoms
	 */
	public void setPropertyMaxSize(Integer maxSize) {
		this.maxSize = maxSize.intValue();
	}
	
	public String getShortDescriptionForMaxSize() {
		return "Maximal number of monoms";
	}
	
	public String getLongDescriptionForMaxSize() {
		return "The maximal number of monoms that an individual may have to get into the outport.";
	}
	
	public String getName() {
		return "SizeSelection";
	}
	
	public String getDescription() {
		return "Selects all individuals with at most maxSize monoms.";
	}
	
}
