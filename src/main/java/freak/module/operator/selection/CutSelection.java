/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;

import java.util.*;

/**
 *
 * This class implements the <code>CutSelection</code>. Choosing from a given
 * <code>individualList</code> "original" it returns the
 * <code>noOfIndividualsToSelect</code> best individuals of this
 * <code>individualList</code>. These are the individuals
 * with the greatest fitness value. If individuals with the same fitness value
 * are chosen, the ones coming from the current generation are preferred.
 *
 * @author Christian, Heiko, Michael
 *
 */
public class CutSelection extends Selection implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	private int noOfIndividualsToSelect = 1;
	
	// for optimized version in some cases
	private boolean fastCut = true;
	
	/**
	 * The constructor of the class <code>CutSelection</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public CutSelection(OperatorGraph graph) {
		super(graph);
		// Add ports
		super.addInPort();
		super.addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(graph.getSchedule().getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This operator works on single objective fitness functions only.");
		}
		
		try {
			((SingleObjectiveFitnessFunction)schedule.getFitnessFunction()).compareIndividuals(null, null);
			fastCut = true;
		} catch (UnsupportedOperationException e) {
			fastCut = false;
		}
	}
	
	public IndividualList[] selectOneOnly(IndividualList[] input) {
		
		if (input[0].size() <= 1) {
			return input;
		}
		
		// create the resulting lists ...
		IndividualList output[] = new IndividualList[1];
		
		// create a new IndividualList
		output[0] = new Population(graph.getSchedule(), 1);
		
		Individual rival;
		
		Iterator it = input[0].iterator();
		SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)graph.getSchedule().getFitnessFunction();
		
		Individual theBest = (Individual)it.next();
		while (it.hasNext()) {
			rival = (Individual)it.next();
			switch (fitness.compareIndividuals(theBest, rival)) {
				// the youngest individual wins
				case 0:
					if (theBest.getDateOfBirth() < rival.getDateOfBirth())
						theBest = rival;
					break;
					//rival is better
				case -1:
					theBest = rival;
					break;
			}
		}
		
		output[0].addIndividual(theBest);
		
		return output;
	}
	
	/**
	 * This method does the selection described above.
	 *
	 * @param original this is the <code>individualList</code> the individuals
	 * are chosen from.
	 * @return this is the list of the chosen individuals.
	 */
	public IndividualList[] process(IndividualList[] original) {
		
//		System.out.println("CutSelection: "+noOfIndividualsToSelect+" from "+original[0].size());
		
		if (noOfIndividualsToSelect == 1 && fastCut) {
			return selectOneOnly(original);
		}
		
		if (original[0].size() == 0) {
			IndividualList result[] = new IndividualList[1];
			result[0] = new Population(getOperatorGraph().getSchedule());
			return result;
		}
		
		// create the resulting lists ...
		IndividualList result[] = new IndividualList[1];
		
		// create a new IndividualList
		result[0] = new Population(graph.getSchedule(), noOfIndividualsToSelect);
		
		// convert the original-list to array (==> faster)
		Individual[] origin = original[0].toArray();
		
		// use quickselect to get the noOfIndividualsToSelect best
		FitnessFunction fitness = graph.getSchedule().getFitnessFunction();
		int generation = graph.getSchedule().getCurrentGeneration();
		double fValue[] = new double[origin.length];
		boolean thisGen[] = new boolean[origin.length];
		
		for (int i = 0; i < origin.length; i++) {
			fValue[i] = ((SingleObjectiveFitnessFunction)fitness).evaluate(origin[i],original[0]);
			thisGen[i] = (origin[i].getDateOfBirth() == generation);
		}
		
		Individual[] selResult = quickSelect(origin, fValue, thisGen, noOfIndividualsToSelect, graph.getSchedule().getRandomElement());
		
		// now select noOfIndidualsToSelect individuals
		for (int i = 0; i < noOfIndividualsToSelect; i++) {
			result[0].addIndividual(selResult[i]);
		}
		
		return result;
	}
	
	/**
	 *
	 * Returns the number of individuals which are chosen.
	 *
	 * @return a wrapped integer containing the number of individuals to be
	 * chosen.
	 */
	public Integer getPropertyNoOfIndividualsToSelect() {
		return new Integer(noOfIndividualsToSelect);
	}
	
	/**
	 * Sets the number of individuals which will be chosen. At least one
	 * individual has to be chosen, so setting to a value less or equal to
	 * zero will set the value to 1.
	 *
	 * @param noOfIndividualsToSelect a wrapped integer containing the number
	 * of individuals which are chosen
	 */
	public void setPropertyNoOfIndividualsToSelect(Integer noOfIndividualsToSelect) {
		this.noOfIndividualsToSelect = noOfIndividualsToSelect.intValue();
		if (this.noOfIndividualsToSelect <= 0)
			this.noOfIndividualsToSelect = 1;
	}
	
	public String getShortDescriptionForNoOfIndividualsToSelect() {
		return "Selected individuals";
	}
	
	public String getLongDescriptionForNoOfIndividualsToSelect() {
		return "The number of individuals to be selected.";
	}
	
	/*
	  use carefully!!!! origin and fValue maybe modified
	  @param origin array of individuals to select from
	  @param fValue fitnessvalues to decide which individuals are better
	  @param noToSelect number of individuals to select
	  @param randomGen random-number-generator to select pivot-point randomly
	 */
	private Individual[] quickSelect(Individual[] origin, double[] fValue, boolean[] thisGen, int noToSelect, RandomElement randomGen) {
		// select a pivot-point uniform randomly
		int pivot = randomGen.choose(0, origin.length - 1);
		int left = 0, right = origin.length - 1;
		
		Individual[] result = new Individual[noToSelect];
		
		// select all?
		if (noToSelect == origin.length) {
			for (int i = 0; i < noToSelect; i++)
				result[i] = origin[i];
			return result;
		}
		
		// put the pivot to the "right" place
		while (left != right) {
			// search a smaller one left
			while ((fValue[left] > fValue[pivot] || (fValue[left] == fValue[pivot] && thisGen[left])) && left < pivot)
				left++;
			// search a greater one right
			while ((fValue[right] <= fValue[pivot] || (fValue[right] == fValue[pivot] && !thisGen[right])) && right > pivot)
				right--;
			
			// if not both are pivot
			if (left != right) { // do swap
				double temp = fValue[left];
				fValue[left] = fValue[right];
				fValue[right] = temp;
				
				Individual temp2 = origin[left];
				origin[left] = origin[right];
				origin[right] = temp2;
				
				if (pivot == left)
					pivot = right;
				else if (pivot == right)
					pivot = left;
			}
			if (left < pivot)
				left++;
			if (right > pivot)
				right--;
		}
		
		// now we found the pivot+1 greatest individuals.
		// so decide what to do
		if (pivot + 1 < noToSelect) { // not enough elements selected
			// isolate the found ones ...
			for (int i = 0; i < pivot + 1; i++)
				result[i] = origin[i];
			
			// calculate the rest recursively
			Individual[] newOrigin = new Individual[origin.length - pivot - 1];
			double[] newFValue = new double[origin.length - pivot - 1];
			boolean[] newThisGen = new boolean[origin.length - pivot - 1];
			for (int i = 0; i < origin.length - pivot - 1; i++) {
				newOrigin[i] = origin[pivot + 1 + i];
				newFValue[i] = fValue[pivot + 1 + i];
				newThisGen[i] = thisGen[pivot + 1 + i];
			}
			Individual[] selResult = quickSelect(newOrigin, newFValue, newThisGen, noToSelect - pivot - 1, randomGen);
			
			// now merge the to parts
			for (int i = 0; i < selResult.length; i++) {
				result[pivot + 1 + i] = selResult[i];
			}
			return result;
		}
		
		if (pivot + 1 == noToSelect) { // enough selected
			// isolate the best individuals in origin
			for (int i = 0; i < noToSelect; i++) {
				result[i] = origin[i];
			}
			return result;
		}
		
		if (pivot + 1 > noToSelect) { // too much selected
			// calculate it recursively
			Individual[] newOrigin = new Individual[pivot];
			double[] newFValue = new double[pivot];
			boolean[] newThisGen = new boolean[pivot];
			for (int i = 0; i < pivot; i++) {
				newOrigin[i] = origin[i];
				newFValue[i] = fValue[i];
				newThisGen[i] = thisGen[i];
			}
			result = quickSelect(newOrigin, newFValue, newThisGen, noToSelect, randomGen);
			return result;
		}
		
		return result;
	}
	
	public String getName() {
		return "Cut Selection";
	}
	
	public String getDescription() {
		return "Selects the best individuals. " + "Individuals coming from this generation are preferred if their " + "fitness values equal.";
	}
	
}
