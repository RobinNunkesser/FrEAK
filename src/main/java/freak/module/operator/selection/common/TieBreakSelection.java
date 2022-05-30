/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;


/**
 *
 * This class implements the <code>OneCriteriaSelection</code>. Choosing from a given
 * <code>individualList</code> "original" it returns the
 * <code>noOfIndividualsToSelect</code> best individuals of this
 * <code>individualList</code> according to one of different fitness values. 
 * If individuals with the same fitness value
 * are chosen, the ones coming from the current generation are preferred.
 *
 * @author Christian, Heiko, Michael, changed by Melanie
 *
 */
public class TieBreakSelection extends Selection implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	private int noOfIndividualsToSelect = 1;
	private int noOfCriteria = 0;
	private int secondCriteria = 0;
	
	/**
	 * The constructor of the class <code>OneCriteriaSelection</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public TieBreakSelection(OperatorGraph graph) {
		super(graph);
		// Add ports
		super.addInPort();
		super.addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(graph.getSchedule().getFitnessFunction() instanceof MultiObjectiveFitnessFunction)) {			
			throw new UnsupportedEnvironmentException("This operator works on two objective fitness functions only.");				
		} //else 
//			if (((MultiObjectiveFitnessFunction)graph.getSchedule().getFitnessFunction()).getDimensionOfObjectiveSpace() != 2) {
	//			throw new UnsupportedEnvironmentException("This operator works on two objective fitness functions only.");				
		//	}
		
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
		
		if (original[0].size() <= noOfIndividualsToSelect){
			return original;			
		}
		
		// create the resulting lists ...
		IndividualList result[] = new IndividualList[1];
		
		// create a new IndividualList
		result[0] = new Population(graph.getSchedule(), noOfIndividualsToSelect);
		
		// convert the original-list to array (==> faster)
		Individual[] origin = original[0].toArray();
		
        // Melanie: get wished values for used criteria, if criteria exists for IndividualList
		FitnessFunction fitness = graph.getSchedule().getFitnessFunction();
		int generation = graph.getSchedule().getCurrentGeneration();
		double fValue[] = new double[origin.length];
		boolean thisGen[] = new boolean[origin.length];
		
		double maximum=Math.abs(((MultiObjectiveFitnessFunction)fitness).evaluate(origin[0],original[0])[ (noOfCriteria + 1) % 2 ]);
		
		// Melanie: maximum value of the other criteria is searched for,
		// fvalue is sum of wished criteria + other criteria divided by maximum.
		// the effect is that indivuals are sorted by other criteria if a tie comes up.
		
		for (int i = 0; i < origin.length; i++){
			double m = Math.abs(((MultiObjectiveFitnessFunction)fitness).evaluate(origin[i],original[0])[ (noOfCriteria + 1) % 2 ]);
			if (m > maximum) {
				maximum = m;
			}
		}		
		
		if (maximum == 0){
			maximum = 1;
		}
		
		for (int i = 0; i < origin.length; i++) {
			fValue[i] = ((MultiObjectiveFitnessFunction)fitness).evaluate(origin[i],original[0])[noOfCriteria];
			fValue[i] += ((MultiObjectiveFitnessFunction)fitness).evaluate(origin[i],original[0])[ secondCriteria ]/(maximum+1);
//			System.out.print(fValue[i]+" ");
			thisGen[i] = (origin[i].getDateOfBirth() == generation);
		}
		
//		System.out.println();
		
		// use quickselect to get the noOfIndividualsToSelect best
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
	
	/**
	 * Returns the number of the criteria of the multiple fitness function that should be used for selecting.
	 * Numbering is starting with zero.
	 * @return a wrapped integer containing the number of the used criteria
	 */	
	public Integer getPropertyNoOfCriteria(){
		return new Integer(noOfCriteria);
	}

	/**
	 * Sets the number of the criteria that should be used for selecting individuals.
	 * If the wanted criteria doesn't exist, zero will be used instead.
	 * @param noOfCriteria number of the wished criteria for selecting.
	 */
	public void setPropertyNoOfCriteria(Integer noOfCriteria){
		this.noOfCriteria = noOfCriteria.intValue();	
		FitnessFunction fitness = graph.getSchedule().getFitnessFunction();
		if ( this.noOfCriteria >= ((MultiObjectiveFitnessFunction)fitness).getDimensionOfObjectiveSpace() ){
			this.noOfCriteria = 0;
		}			

	}
	
	public String getShortDescriptionForNoOfCriteria(){
		return "Criteria";
	}
	
	public String getLongDescriptionForNoOfCriteria(){
		return "The number of the criteria used to select individuals";
	}
	
	/**
	 * Returns the number of the second criteria of the multiple fitness function that should be used for selecting.
	 * Numbering is starting with zero.
	 * @return a wrapped integer containing the number of the used criteria
	 */	
	public Integer getPropertySecondCriteria(){
		return new Integer(secondCriteria);
	}
	
	/**
	 * Sets the number of the second criteria that should be used for selecting individuals.
	 * If the wanted criteria doesn't exist, zero will be used instead.
	 * @param secondCriteria number of the second criteria for selecting.
	 */
	public void setPropertySecondCriteria(Integer secondCriteria){
		this.secondCriteria = secondCriteria.intValue();
		FitnessFunction fitness = graph.getSchedule().getFitnessFunction();
		if ( this.secondCriteria >= ((MultiObjectiveFitnessFunction)fitness).getDimensionOfObjectiveSpace() ){
			this.secondCriteria = ((MultiObjectiveFitnessFunction)fitness).getDimensionOfObjectiveSpace() - 1;
		}	
	}
	
	public String getShortDescriptionForSecondCriteria(){
		return "Second Criteria";
	}
	
	public String getLongDescriptionForSecondCriteria(){
		return "The number of the second critera used to select indiviuals";
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
		return "Tie-Breaking Selection";
	}
	
	public String getDescription() {
		return "Selects the best individuals according to a chosen criteria of a multi value fitness function. " + "If there is a tie, the chosen second criteria will be used." + "Individuals coming from this generation are preferred if their " + "fitness values equal.";
	}
	
}
