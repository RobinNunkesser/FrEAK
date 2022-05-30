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
 * individuals whose DNFTrees fulfill more than <code>threshold</code>
 * percent of the 1-lines of the input table.
 *
 * @author Melanie
 *
 */
public class CaseSelection extends Selection implements Configurable {
	
	private double threshold = 25;
	
	/**
	 * The constructor of the class <code>CaseSelection</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public CaseSelection(OperatorGraph graph) {
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

		result[0] = new Population(graph.getSchedule());
		
		for (int i = 0; i < origin.length; i++) {
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)(origin[i].getGenotype());	
			if ((double)bfg.evaluate1s() / (double)bfg.getNum1Rows() >= threshold / 100) {
				result[0].addIndividual(origin[i]);
			}
		}

		return result;
	}


	public Double getPropertyThreshold() {
		return new Double(threshold);
	}
	

	public void setPropertyThreshold(Double threshold) {
		this.threshold = threshold.doubleValue();
	}
	
	public String getShortDescriptionForThreshold() {
		return "Min. for fulfilled 1-lines.";
	}
	
	public String getLongDescriptionForThreshold() {
		return "Minimum percentage of fulfilled 1-lines for selected individuals.";
	}
	
	public String getName() {
		return "CaseSelection";
	}
	
	public String getDescription() {
		return "Selects all individuals whose DNFTree fulfilles more than threshold percent of the 1-lines.";
	}
	
}
