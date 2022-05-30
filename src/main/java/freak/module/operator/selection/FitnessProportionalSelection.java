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
import freak.core.population.NoSuchIndividualException;
import freak.core.population.Population;

/**
 * With this class one can do the <code>FitnessProportionalSelection</code>. An
 * <code>individualList</code> "original" is given. The method <code>doSelection</code> 
 * chooses randomly <code>noOfIndividualsToSelect</code> from this list and
 * returns them.
 * 
 * The selection of this individuals is done according to a given density-function 
 * which is defined as follows:
 * 
 * p(x,original)=fitness(x,original)/sum(fitness(i,original),i in original).
 * 
 * In words: The probability of an individual x (coming from the 
 * <code>individualList</code> original) to be chosen is the portion of the 
 * fitness value in respect to the "total fitness value" of the
 * <code>individualList</code> (the sum of the fitness-value of
 * its individuals).
 * 
 * @author Christian, Heiko
 */
public class FitnessProportionalSelection extends Selection implements Configurable, CompatibleWithDifferentSearchSpaces {

	/**
	 * No of individuals to select from the given list of individuals.
	 */
	private int noOfIndividualsToSelect = 1;

	/**
	 * fitness proportional or anti-fitness-proportional?
	 */
	private boolean invert;

	/**
	 * return each individual form the given list of individuals almost once?
	 */
	private boolean unique;

	/**
	 * The constructor of the class <code>FitnessProportionalSelection</code> 
	 * @param graph a link to the current <code>OperatorGraph</code>
	 */
	public FitnessProportionalSelection(OperatorGraph graph) {
		super(graph);

		super.addInPort();
		super.addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This operator works on single objective fitness functions only.");
		}
	}

	/**
	 * This method does the selection described above.
	 * 
	 * @param original this is the <code>individualList</code> the
	 * individuals are chosen from
	 * @return this is the list of the chosen individuals 
	 */
	public IndividualList[] process(IndividualList[] original) {
		// check if IndividualList is empty
		for (int i = 0; i < original.length; i++)
			if (original[i] == null)
				throw new NoSuchIndividualException();

		// create the resulting lists ...
		IndividualList result[] = new IndividualList[1];

		// create a new IndividualList
		result[0] = new Population(graph.getSchedule(), noOfIndividualsToSelect);

		if (original[0].size() > 0) {
			// convert the original-list to array (==> faster)
			Individual[] origin = original[0].toArray();

			// if unique and all or more individuals should be returned, return all
			if (unique && noOfIndividualsToSelect >= original[0].size()) {
				for (int i = 0; i < origin.length; i++)
					result[0].addIndividual(origin[i]);
			}

			// get the sum of the fitnessvalues belonging to the individuals in origin
			FitnessFunction fitness = graph.getSchedule().getFitnessFunction();
			double sum = 0;
			double fvalue[] = new double[origin.length];
			double limit[] = new double[origin.length];
			for (int i = 0; i < origin.length; i++)
				sum += fvalue[i] = ((SingleObjectiveFitnessFunction)fitness).evaluate(origin[i],original[0]);

			// create a "fitnessproportional" partition of the intervall [0,1]
			if (invert) {
				// find the maximum
				double max = fvalue[0];
				for (int i = 0; i < origin.length; i++)
					if (fvalue[i] > max)
						max = fvalue[i];

				// calculate the right sum
				for (int i = 0; i < origin.length; i++)
					sum += fvalue[i] = 1 + max - fvalue[i];

				limit[0] = 1 + max - fvalue[0] / sum;
				for (int i = 1; i < origin.length; i++) {
					limit[i] = limit[i - 1] + (1 + max - fvalue[i] / sum);
				}
			} else {
				for (int i = 0; i < origin.length; i++)				
					sum += fvalue[i] = ((SingleObjectiveFitnessFunction)fitness).evaluate(origin[i],original[0]);

				limit[0] = fvalue[0] / sum;
				for (int i = 1; i < origin.length; i++) {
					limit[i] = limit[i - 1] + (fvalue[i] / sum);				
				}
			}

			// now select noOfIndidualsToSelect individuals       
			RandomElement randomGen = graph.getSchedule().getRandomElement();

			boolean selected[] = new boolean[origin.length];

			for (int i = 0; i < noOfIndividualsToSelect; i++) {
				int select;

				do {
					// create a randomnumber between 0,1;
					double number = randomGen.uniform(0, 1);

					// and calculate the number of the individual to select
					select = doSelect(limit, number);
				} while (unique && selected[select] == true);

				// ... and put it into the selection & mark it
				selected[select] = true;
				result[0].addIndividual(origin[select]);
			}
		}

		return result;
	}

	private int doSelect(double[] limit, double number) {
		int reference = 0;
		int first = 0;
		int last = limit.length - 1;

		// perform a binary search until only two elements are still there
		while (last - first > 1) {
			// check the number in the middle
			reference = (int) ((last + first) / 2.0);

			// our searched value is in the left half
			if (limit[reference] > number)
				last = reference;

			// out searched value is in the right half
			if (limit[reference] < number)
				first = reference;

			// got the right one
			if (limit[reference] == number)
				return reference;
		}

		// compare these two and decide
		if (limit[first] > number)
			return first;

		return last;
	}

	/**
	 * 
	 * This method is used to get the number of individuals which are chosen
	 * 
	 * @return a wrapped integer containing the number of individuals to be
	 * chosen
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
	 * 
	 * This method is used to recieve the current status of the value 
	 * <code>invert</code>
	 * 
	 * @return a wrapped boolean containing the the status of 
	 * <code>invers</code>
	 */
	public Boolean getPropertyInvert() {
		return new Boolean(invert);
	}

	/**
	 * This method is used to set the status of the parameter 
	 * <code>invert</code>. <br>
	 * 
	 * With this parameter set to true the selection is done the other way round.
	 * Not the best individuals get a greater chance to be chosen, but the bad
	 * ones.
	 *  
	 * @param invert a wrapped boolean containing the status <code>invert</code>
	 * should be set to.
	 */
	public void setPropertyInvert(Boolean invert) {
		this.invert = invert.booleanValue();
	}

	public String getLongDescriptionForInvert() {
		return "When 'invert' is enabled, an individual is chosen with probability (1+max-f(x))/f(population).";
	}

	/**
		* 
		* This method is used to get the status of <code>unique</code>.
		*  
		* @return a wrapped boolean containing the the status of <code>unique</code>
		*/
	public Boolean getPropertyUnique() {
		return new Boolean(unique);
	}

	/**
		* This method is used to set the status of <code>unique</code>. <br>
		* 
		* With this parameter set to true, the returned elements are all unique.
		* 
		* @param unique a wrapped boolean containing the status <code>unique</code>
		* should be set to.
		*/
	public void setPropertyUnique(Boolean unique) {
		this.unique = unique.booleanValue();
	}

	public String getLongDescriptionForUnique() {
		return "If enabled, an individual is only returned once.";
	}

	public String getName() {
		return "Fitness Proportional Selection";
	}

	public String getDescription() {
		return "Selects individuals using fitness proportional selection.\r\n" + "Normally, an individual is chosen with probability f(x)/f(population). " + "You may, however, specify options changing this behavior.";
	}

}
