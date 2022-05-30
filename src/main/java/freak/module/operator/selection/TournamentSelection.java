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
 * 
 * This selection class implements the method of <code>TournamentSelection</code>.
 * It gets an <code>individualList</code> "original", from which it
 * chooses <code>tournamentSize</code> Individuals uniform randomly.
 * These k Individuals are compared (by their fitness value) and the best one
 * is put into the returned population.
 * 
 * This is repeated <code>noOfIndivualsToSelect</code> times to be able to 
 * return an <code>individualList</cde> of size <code>noOfIndivualsToSelect</code>.
 * 
 * @author Christian, Heiko
 */
public class TournamentSelection extends Selection implements Configurable, CompatibleWithDifferentSearchSpaces {

	/*
	 * Size of the performed tournament.
	 */
	private int tournamentSize = 1;

	/*
	 * No of individuals to select form the given list of indivduals.
	 */
	private int noOfIndividualsToSelect = 1;

	/**
	 * The constructor of the class <code>TournamentSelection</code>. 
	 * 
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public TournamentSelection(OperatorGraph graph) {
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
	 * @param original this is the <code>individualList</code> the individuals
	 * are chosen from.
	 * @return this is the list of the chosen individuals.
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

		// convert the original-list to array (==> faster)
		Individual[] origin = original[0].toArray();

		// now select noOfIndidualsToSelect individuals
		FitnessFunction fitness = graph.getSchedule().getFitnessFunction();

		Individual tournament[] = new Individual[tournamentSize];
		double fValue[] = new double[tournamentSize];
		RandomElement randomGen = graph.getSchedule().getRandomElement();

		for (int i = 0; i < noOfIndividualsToSelect; i++) {
			for (int j = 0; j < tournamentSize; j++) {
				// create a random number between 0 and origin.length-1;
				int number = randomGen.choose(0, origin.length - 1);

				// put the individual into the tournament
				tournament[j] = origin[number];
                fValue[j] = ((SingleObjectiveFitnessFunction)fitness).evaluate(origin[number],original[0]);
            }

			// and put the winner into the selection
			result[0].addIndividual(doTournament(tournament, fValue));
		}

		return result;
	}

	/**
	 * 
	 * This method is used to get the size of the tournament. 
	 * 
	 * @return a wrapped integer containing the number competing each other in
	 * one step of the selection.
	 */
	public Integer getPropertyTournamentSize() {
		return new Integer(tournamentSize);
	}

	/**
	 * 
	 * Sets the number of individuals competing each other. <br>
	 * 
	 * This is the number of individuals competing each other to get one
	 * individual to select.
	 * 
	 * @param tournamentSize a wrapped integer containing the number of
	 * individuals competing each other in one step of the selection
	 */
	public void setPropertyTournamentSize(Integer tournamentSize) {
		this.tournamentSize = tournamentSize.intValue();
	}

	public String getShortDescriptionForTournamentSize() {
		return "Tournament size";
	}

	public String getLongDescriptionForTournamentSize() {
		return "The number of individuals contesting in a tournament.";
	}

	/**
	 * 
	 * Returns the number of individuals which are chosen.
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
	 * @return may be null
	 **/
	private Individual doTournament(Individual[] individual, double[] fValue) {
		if (individual.length > 0) {
			int best = 0;
			for (int i = 1; i < individual.length; i++)
				if (fValue[i] > fValue[best])
					best = i;

			return individual[best];
		} else {
			return null;
		}
	}

	public String getName() {
		return "Tournament Selection";
	}

	public String getDescription() {
		return "Selects individuals as winners of tournaments." + "The individuals for the tournaments are chosen uniform randomly " + "and the winner is put into selection. " + "This procedure is repeated until the specified amount of individuals is selected.";
	}

}
