/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.cycle;

import java.util.*;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.control.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * Inver-over Operator for the TSP<br>
 * This operator mutates every individual determined by other randomly selected
 * individuals.
 *
 * @author Michael
 */
public class InverOver extends Recombination implements Configurable {

	private double p = 0.02; //possibility to create new connections

	public InverOver(OperatorGraph graph) {
		super(graph);
		super.addInPort();
		super.addOutPort();
	}

	/**
	 * Sets the probability to create new connections.
	 * @param prob must be between [0..1]
	 */
	public void setPropertyProbNewConnections(Double prob) {
		if (prob.doubleValue() >= 0 && prob.doubleValue() <= 1)
			p = prob.doubleValue();
	}

	/**
	 * Returns the probability to create new connections.
	 */
	public Double getPropertyProbNewConnections() {
		return new Double(p);
	}

	public String getShortDescriptionForProbNewConnections() {
		return "New connections' probability";
	}

	public String getLongDescriptionForProbNewConnections() {
		return "Probability to create new connections.";
	}

	private int findIndex(int[] tour, int city) {
		int i;
		for (i = 0; tour[i] != city; i++) {
		}
		return i;
	}

	/**
	 * This method iterates the individual list and applies the Inver-over
	 * Operator on every individual
	 */
	public IndividualList[] process(IndividualList[] input) {
		Schedule schedule = graph.getSchedule();
		RandomElement re = schedule.getRandomElement();
		IndividualList population = input[0];
		Iterator it = population.iterator();
		int city, nextCity, indexOfCity, indexOfNextCity, indexNCity, indexPCity;
		int[] tour, tour2;
		Individual parent, child;
		boolean exitloop;
		int input0size = input[0].size();
		IndividualList[] output = new IndividualList[1];
		output[0] = new Population(schedule, input0size);

		// for each individual of the population/individuallist
		while (it.hasNext()) {
			parent = (Individual)it.next();
			tour = ((PermutationGenotype)parent.getGenotype()).getIntArray();

			//select randomly a city from "tour"
			indexOfCity = re.choose(0, tour.length - 1);
			city = tour[indexOfCity];
			exitloop = false;
			do {
				if (re.raw() <= p) {
					//select the nextcity from the remaining cities in "tour"
					indexOfNextCity = re.choose(0, tour.length - 2); //0..n-2
					if (indexOfCity == indexOfNextCity)
						indexOfNextCity = tour.length - 1;
					nextCity = tour[indexOfNextCity];
				} else {

					//select randomly an individual from Population
					tour2 = ((PermutationGenotype)population.getIndividual(re.choose(0, input0size - 1)).getGenotype()).getIntArray();

					//assign to "nextCity" the next city to the "city" in the selected individual
					nextCity = tour2[(findIndex(tour2, city) + 1) % tour2.length];
					// is unknown
					indexOfNextCity = -1;
				}

				//if the next or previous city of "city" in "tour" is "nextCity"
				indexNCity = (indexOfCity + 1) % tour.length;
				indexPCity = (indexOfCity == 0) ? tour.length - 1 : indexOfCity - 1;
				if (tour[indexNCity] == nextCity || tour[indexPCity] == nextCity) {
					exitloop = true;
				} else {
					tour = inverse(tour, indexNCity, (indexOfNextCity == -1) ? findIndex(tour, nextCity) : indexOfNextCity);
				}
			} while (!exitloop);

			child = new Individual(schedule, new PermutationGenotype(tour), new Individual[] { parent });
			output[0].addIndividual(child);
		}
		return output;
	}

	/**
	 * Inverse a substring from position start to end
	 * @return the array with the inverted section
	 */
	private int[] inverse(int[] tour, int start, int end) {
		int[] result = (int[])tour.clone();
		for (int i = 0;(start + i - 1) % tour.length != end; i++) {
			result[(start + i) % tour.length] = tour[(end - i < 0) ? end - i + tour.length : end - i];
		}
		return result;
	}

	public String getName() {
		return "Inver-Over";
	}

	public String getDescription() {
		return "Inver-Over is a well known operator for the TSP.";
	}
}
