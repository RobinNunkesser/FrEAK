/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.populationmanager;

import freak.core.control.Schedule;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.graph.GraphException;
import freak.core.graph.Initialization;
import freak.core.graph.OutputLengthMismatchException;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.NoSuchIndividualException;
import freak.core.population.Population;
import freak.core.populationmanager.AbstractPopulationManager;
import freak.module.operator.selection.FitnessProportionalSelection;

import java.util.Iterator;

/**
 * The population is partitioned into <code>SubpopulationCount</code>  subpopulations. Each subpolution has epochs with length <code>EpochLength</code> generations of isolated evolution. After an epoch <code>MigrantsCount</code> individuals will migrate between the subpolutions. 
 * @author  Michael
 */

public class IslandModel extends AbstractPopulationManager implements Configurable {

	private int subpopulationcount;
	private int migrantsCount;
	private int epochLength;
	private FitnessProportionalSelection selection;
	private boolean copyMigrants;

	/**
	 * The current subpopulations
	 * @uml.property  name="populations"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private IndividualList[] populations;

	/**
	 * Constructs a new <code>IslandModel</code>.
	 */
	public IslandModel(Schedule schedule) {
		super(schedule);
		subpopulationcount = 2;
		migrantsCount = 0;
		epochLength = 10;
		copyMigrants = true;
	}
	
	public void initialize() {
		super.initialize();
		selection = new FitnessProportionalSelection(getSchedule().getOperatorGraph());
		selection.setPropertyNoOfIndividualsToSelect(new Integer(migrantsCount));
		selection.setPropertyUnique(new Boolean(true));
		selection.initialize();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (selection != null) {
			selection.testSchedule(schedule); 
		} 
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
	}

	private Population mergeSubpopulations(IndividualList[] p) {
		Population result = new Population(getSchedule());
		for (int i = 0; i < p.length; i++) {
			result.addAllIndividuals(p[i]);
		}
		return result;
	}

	public void initPopulation(Initialization initialization) throws GraphException {
		populations = new Population[subpopulationcount];

		//create new subpolutions
		for (int i = 0; i < subpopulationcount; i++) {
			populations[i] = new Population(getSchedule());
		}

		if (initialization == null)
			throw new NullPointerException("Specified initialization operator is null.");
		IndividualList[] resultOfInitialization = initialization.process(new IndividualList[] {
		});
		if (resultOfInitialization == null || resultOfInitialization.length == 0)
			throw new OutputLengthMismatchException("Output of initialization operator is missing.");

		// assign each indiviual to a subpolution
		Iterator it = resultOfInitialization[0].iterator();
		for (int j = 0; it.hasNext(); j++) {
			populations[j % subpopulationcount].addIndividual((Individual)it.next());
		}

		setPopulation(mergeSubpopulations(populations));
		fireIndividualList(getPopulation());
	}

	/**
	 * Creates new subpopulations by putting each subpopulation
	 * into the operator graph.
	 * When this is done, an <code>IndividualListEvent</code> is fired.
	 *
	 * @return the new population.
	 * @throws GraphException if an error within the graph occurs.
	 * @throws NoSuchIndividualException if the population is empty.
	 */
	public void createNewGeneration() throws GraphException, NoSuchIndividualException {
		if (populations[0].isEmpty())
			throw new NoSuchIndividualException("Population is empty.");

		for (int i = 0; i < subpopulationcount; i++) {
			populations[i] = getSchedule().getOperatorGraph().process(populations[i]);
		}

		// communication between the islands
		if ((getSchedule().getCurrentGeneration() % epochLength == epochLength - 1) && (migrantsCount > 0)) {

			// since the subpopulations are to be modified, they are cloned 
			// first
			for (int i = 0; i < subpopulationcount; i++) {
				populations[i] = (IndividualList)populations[i].clone();
			}

			// select the migrants
			IndividualList[] migrants = new IndividualList[subpopulationcount];
			for (int i = 0; i < subpopulationcount; i++) {
				migrants[i] = selection.process(new IndividualList[] { populations[i] })[0];
				if (!copyMigrants) {

					//remove migrants from old subpolution
					for (int j = 0; j < migrants[i].size(); j++) {
						populations[i].removeIndividual(populations[i].indexOf(migrants[i].getIndividual(j)));
					}
				}
			}

			for (int i = 0; i < subpopulationcount; i++) {

				// migrate the migrants
				populations[i].addAllIndividuals(migrants[(i + 1) % subpopulationcount]);
			}

			if (copyMigrants) {
				IndividualList todelete;
				selection.setPropertyInvert(new Boolean(true));
				for (int i = 0; i < subpopulationcount; i++) {
					todelete = selection.process(new IndividualList[] { populations[i] })[0];

					// reduce size to original size
					for (int j = 0; j < todelete.size(); j++) {
						populations[i].removeIndividual(populations[i].indexOf(todelete.getIndividual(j)));
					}
				}
				selection.setPropertyInvert(new Boolean(false));
			}
		} //end communication

		setPopulation(mergeSubpopulations(populations));
		fireIndividualList(getPopulation());
	}

	public String getDescription() {
		return "Implements multiple subpopulations. Each subpolution has isolated evolution. After an epoch individuals will migrate between the subpolutions.";
	}

	public String getName() {
		return "Island Model";
	}

	/**
	 * Sets the number of islands in this model. Each island has its own
	 * subpolution.
	 */
	public void setPropertySubpopulationCount(Integer count) {
		if (count.intValue() > 1)
			subpopulationcount = count.intValue();
	}

	/**
	 * Returns the number of islands.
	 */
	public Integer getPropertySubpopulationCount() {
		return new Integer(subpopulationcount);
	}

	public String getShortDescriptionForSubpopulationCount() {
		return "Subpopulations";
	}

	public String getLongDescriptionForSubpopulationCount() {
		return "The number of subpopulations. The initialization population will be split to all subpopulations.";
	}

	/**
	 * After every epoch communication between the islands will progressed.
	 * The MigrantsCount determine how many individuals will migrate between
	 * two islands.
	 */
	public void setPropertyMigrantsCount(Integer count) {
		if (count.intValue() >= 0)
			migrantsCount = count.intValue();
		selection.setPropertyNoOfIndividualsToSelect(count);
	}

	/**
	 * @return how many idividuals will migrate after every epoch.
	 */
	public Integer getPropertyMigrantsCount() {
		return new Integer(migrantsCount);
	}

	public String getShortDescriptionForMigrantsCount() {
		return "Number of migrants";
	}

	public String getLongDescriptionForMigrantsCount() {
		return "Determines how many fitness proportional selected individuals will migrate between two subpopulations every epoch.";
	}

	/**
	 * Sets the number of generations an epoch takes.
	 */
	public void setPropertyEpochLength(Integer length) {
		if (length.intValue() > 0)
			epochLength = length.intValue();
	}

	/**
	 * @return the number of generations an epoch takes.
	 */
	public Integer getPropertyEpochLength() {
		return new Integer(epochLength);
	}

	public String getShortDescriptionForEpochLength() {
		return "Epoch length";
	}

	public String getLongDescriptionForEpochLength() {
		return "The number of generations an epoch takes. After every epoch, communication between the islands will progress.";
	}

	/**
	 * If CopyMigrants is set to false, then individuals migrating from an
	 * island a to an island b will deleted from the subpolution of island a
	 * and insert in the subpolution of island b. Otherwise they are added to
	 * the subpolution of island b without beeing removed from their old
	 * subpolation. After that the subpolution of island b will delete negativ
	 * fitness proportional selected individuals to reduce its size to the
	 * original size.
	 */
	public void setPropertyCopyMigrants(Boolean value) {
		copyMigrants = value.booleanValue();
	}

	/**
	 * @return the value of CopyMigrants.
	 */
	public Boolean getPropertyCopyMigrants() {
		return new Boolean(copyMigrants);
	}

	public String getShortDescriptionForCopyMigrants() {
		return "Copy migrants";
	}

	public String getLongDescriptionForCopyMigrants() {
		return "If checked, after every epoch the individuals will copied, not moved to another subpopulation. After that the subpopulations will reduce its size to the original size by deleting negativ fitness proportional selected individuals.";
	}

}