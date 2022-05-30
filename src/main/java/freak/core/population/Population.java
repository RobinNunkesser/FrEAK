/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.population;

import freak.core.control.*;
import freak.core.fitness.*;
import java.util.*;

/**
 * A list of individuals containing the current population. This implementation is backed by an instance of <code>ArrayList</code>, thus allowing random access and fast append operations.
 * @author  Dirk, Michael
 */
public class Population implements IndividualList {

	/**
	 * A link back to the current schedule.
	 */
	private Schedule schedule;

	/**
	 * An internal data structure that contains the individuals.
	 */
	private ArrayList delegate;

	/**
	 * Constructs a new empty Population.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public Population(Schedule schedule) {
		if (schedule == null)
			throw new NullPointerException("Schedule is null.");
		this.schedule = schedule;
		delegate = new ArrayList();
	}

	/**
	 * Constructs a new empty Population with an initial capacity.
	 *
	 * @param schedule a link back to the current schedule.
	 * @param initialCapacity initialized the internal ArrayList with this size.
	 */
	public Population(Schedule schedule, int initialCapacity) {
		if (schedule == null)
			throw new NullPointerException("Schedule is null.");
		this.schedule = schedule;
		delegate = new ArrayList(initialCapacity);
	}

	/**
	 * Constructs a new Population out of the specified individual.
	 *
	 * @param schedule a link back to the current schedule.
	 * @param individual an individual to be added to the new empty population.
	 */
	public Population(Schedule schedule, Individual individual) {
		if (schedule == null)
			throw new NullPointerException("Back link to the schedule is null.");
		this.schedule = schedule;
		delegate = new ArrayList();
		addIndividual(individual);
	}

	/**
	 * Constructs a new Population out of the specified individual list.
	 *
	 * @param schedule a link back to the current schedule.
	 * @param individuals the individuals to be added to the new empty population.
	 */
	public Population(Schedule schedule, IndividualList individuals) {
		if (schedule == null)
			throw new NullPointerException("Back link to the schedule is null.");
		this.schedule = schedule;
		delegate = new ArrayList();
		addAllIndividuals(individuals);
	}

	public IndividualList getAllIndividualsWithRank(int rank) throws IllegalArgumentException, NoSuchIndividualException {    	
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedOperationException("This operation works on single objective fitness functions only.");
		}

		if (isEmpty())
			throw new NoSuchIndividualException("The population is empty!");
		if (rank < 1 || rank > size())
			throw new IllegalArgumentException("rank has to be a number between 1 and the size of the population.");

		// the most common case is handled seperately for performance reasons
		if (rank == 1) {
			return getElitistsOrLoser(false);
		} else if (rank == size()) {
			return getElitistsOrLoser(true);
		} else {

			/* cache fitness values in a map to enhance performance
			 * and prevent problems with varying fitness values.
			 */
			HashMap fitnessMap = new HashMap();
			SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();

			for (int i = 0; i < size(); i++) {
				Individual individual = getIndividual(i);
				fitnessMap.put(individual, new Double(fitness.evaluate(individual, this)));
			}

			return quickselect(this, rank, fitnessMap);
		}
	}

	public Individual getIndividualWithRank(int rank) throws IllegalArgumentException, NoSuchIndividualException {
		IndividualList all = getAllIndividualsWithRank(rank);
		// deterministically return the first individual in the list
		return all.getIndividual(0);
	}

	/**
	 * Returns the elitists or the losers, the individuals with a maximal or
	 * minimal fitness value within the population.
	 *
	 * @return an <code>IndividualList</code> containing the current elitists or
	 *         losers.
	 */
	public IndividualList getElitistsOrLoser(boolean worst) {
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedOperationException("This operation works on single objective fitness functions only.");
		}
		Population elitists = new Population(schedule);
		Population loser = new Population(schedule);

		SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
		double bestFitness = 0;
		double worstFitness = 0;

		Iterator iter = iterator();
		if (iter.hasNext()) {
			Individual individual = (Individual)iter.next();
			double currentFitness = fitness.evaluate(individual, this);
			bestFitness = currentFitness;
			elitists.addIndividual(individual);
			worstFitness = currentFitness;
			loser.addIndividual(individual);
		}

		while (iter.hasNext()) {
			Individual individual = (Individual)iter.next();
			double currentFitness = fitness.evaluate(individual, this);

			if (!worst && currentFitness >= bestFitness) {
				if (currentFitness > bestFitness) {
					elitists.clear();
					bestFitness = currentFitness;
				}
				elitists.addIndividual(individual);
			}

			if (worst && currentFitness <= worstFitness) {
				if (currentFitness < worstFitness) {
					loser.clear();
					worstFitness = currentFitness;
				}
				loser.addIndividual(individual);
			}
		}

		return worst ? loser : elitists;
	}

	/**
	 * An adapted quickselect algorithm selecting a set of individuals with
	 * possible rank <code>rank</code>.
	 *
	 * @param list the list of individuals from which the individuals are to be selected.
	 * @param rank a number between 1 and the size of the population specifying the fitness rank.
	 * @return all individuals with the specified fitness rank.
	 * @throws NoSuchIndividualException if the specified list happens to be empty in a recursive call.
	 */
	private Population quickselect(IndividualList list, int rank, final Map fitnessMap) throws NoSuchIndividualException {
		if (list.isEmpty())
			throw new NoSuchIndividualException();

		/* partition the individuals into sets of individuals with smaller fitness,
		   larger fitness and equal fitness compared to a randomly chosen pivot element.*/
		Population small = new Population(getSchedule());
		Population equal = new Population(getSchedule());
		Population large = new Population(getSchedule());

		Individual pivot = list.getRandomIndividual();
		double fitnessOfPivot = ((Double)fitnessMap.get(pivot)).doubleValue();

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Individual currentIndividual = (Individual)iter.next();
			double fitnessOfCurrentIndividual = ((Double)fitnessMap.get(currentIndividual)).doubleValue();

			if (fitnessOfPivot > fitnessOfCurrentIndividual)
				small.addIndividual(currentIndividual);
			if (fitnessOfPivot == fitnessOfCurrentIndividual)
				equal.addIndividual(currentIndividual);
			if (fitnessOfPivot < fitnessOfCurrentIndividual)
				large.addIndividual(currentIndividual);
		}

		// requested individuals lie within the set of small fitness values (-> high ranks)
		if (rank - 1 >= large.size() + equal.size())
			return quickselect(small, rank - large.size() - equal.size(), fitnessMap);
		// requested individuals lie within the set of large fitness values (-> low ranks)
		if (rank - 1 < large.size())
			return quickselect(large, rank, fitnessMap);
		// else, the set of requested individuals is found.
		return equal;
	}

	public Individual getRandomIndividual() throws NoSuchIndividualException {
		if (isEmpty())
			throw new NoSuchIndividualException("the population is empty!");
		int randomNumber = schedule.getRandomElement().choose(0, size() - 1);
		return getIndividual(randomNumber);
	}

	public void addIndividual(int index, Individual individual) {
		delegate.add(index, individual);
	}

	public void addIndividual(Individual individual) {
		delegate.add(individual);
	}

	public boolean containsIndividual(Individual individual) {
		return delegate.contains(individual);
	}

	public Individual getIndividual(int index) {
		return (Individual)delegate.get(index);
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public int size() {
		return delegate.size();
	}

	public Individual[] toArray() {
		return (Individual[])delegate.toArray(new Individual[size()]);
	}

	public void addAllIndividuals(IndividualList list) {
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Individual individual = (Individual)iter.next();
			addIndividual(individual);
		}
	}

	public void removeIndividual(int i) {
		delegate.remove(i);
	}

	public void clear() {
		delegate.clear();
	}

	public int getIndividualMultiplicity(Individual individual) {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			if (getIndividual(i).equals(individual))
				count++;
		}
		return count;
	}

	public int indexOf(Individual individual) {
		return delegate.indexOf(individual);
	}

	/**
	 * Returns a shallow copy of the population (the individuals themselves are
	 * not cloned).
	 *
	 * @return a clone of the population.
	 */
	public Object clone() {
		Population population = new Population(getSchedule());
		population.delegate = (ArrayList)this.delegate.clone();
		return population;
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Population))
			return false;
		return delegate.equals(((Population)o).delegate);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public String toString() {
		return delegate.toString();
	}

	public Iterator iterator() {
		return delegate.iterator();
	}

	public void replaceIndividual(int index, Individual individual) {
		delegate.set(index, individual);
	}

	/**
	 * Returns a link back to the current schedule.
	 * @return  a link back to the current schedule.
	 * @uml.property  name="schedule"
	 */
	public Schedule getSchedule() {
		return schedule;
	}

}
