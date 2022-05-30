/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.cycle;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.population.Genotype;
import freak.module.searchspace.Cycle;
import freak.module.searchspace.PermutationGenotype;

/**
 * Asymmetric TSP
 * The traveling salesman problem is to find a tour through n locations with 
 * minimal costs. A cost matrix describes the costs of travelling from i to j
 * for each pair of locations.
 *
 * @author Christian, Michael, Stefan
 */
public class ATSP extends AbstractStaticSingleObjectiveFitnessFunction implements BatchEventListener {

	private double[][] costMatrix;

	/**
	 * the constructor of the class. 
	 * 
	 * @param schedule the fitness function has to know the schedule
	 */
	public ATSP(Schedule schedule) {
		super(schedule);
	}

	public void initialize() {
		super.initialize();
		// initialize the fitness function
		int dim = ((Cycle)getSchedule().getPhenotypeSearchSpace()).getDimension();

		// re-compute costMatrix if search space dimension has changed  
		if (costMatrix != null && costMatrix.length != dim) {
			costMatrix = new double[dim][dim];

			for (int i = 0; i < dim; i++)
				for (int j = 0; j < dim; j++)
					if (i != j)
						costMatrix[i][j] = getSchedule().getRandomElement().uniform(1, 2);
					else
						costMatrix[i][j] = 0;
		}
	}

	public double evaluate(Genotype genotype) {
		double cost = 0;
		int[] gene = ((PermutationGenotype)genotype).getIntArray();

		// now calculate the costs of the path ...
		for (int pos = 1; pos < gene.length; pos++)
			cost += costMatrix[gene[pos - 1] - 1][gene[pos] - 1];

		// ... and the way back
		cost += costMatrix[gene[gene.length - 1] - 1][gene[0] - 1];

		return 1.0 / cost;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return 1;
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public String getDescription() {
		return "The traveling salesman problem is to find a tour through n locations with minimal costs.\n" + "A cost matrix describes the costs of travelling from i to j for each pair of locations.";
	}

	public String getName() {
		return "Asymmetric TSP";
	}

	// handle the BatchEvents
	/**
	 * Used to signalize the fitness function to initialize itself for the next
	 * batch. <br>
	 * 
	 * It creates a cost matrix representing the instance of the TSP.
	 */
	public void batchStarted(BatchEvent evt) {
		// initialize the fitness function
		int dim = ((Cycle)getSchedule().getPhenotypeSearchSpace()).getDimension();

		costMatrix = new double[dim][dim];

		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++)
				if (i != j)
					costMatrix[i][j] = getSchedule().getRandomElement().uniform(1, 2);
				else
					costMatrix[i][j] = 0;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}

}
