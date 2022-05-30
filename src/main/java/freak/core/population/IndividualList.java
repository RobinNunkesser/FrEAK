/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.population;

import java.io.Serializable;
import java.util.Iterator;

/**
 * A list of individuals providing random access to the individuals within.<br>
 *
 * Like individuals, individual lists are to be handled read-only, too.
 * Modules that intend to alter the list have to clone it and work on the clone 
 * instead. Altering the original list directly can cause unpredictable results 
 * in other parts of FrEAK, such as 
 * <code>ConcurrentModificationException</code>s.
 * 
 * @author Dirk
 */
public interface IndividualList extends Serializable, Cloneable {

	/**
	 * Appends the specified individual to the end of the list.
	 *
	 * @param individual the individual to be appended.
	 */
	public void addIndividual(Individual individual);

	/**
	 * Inserts the specified individual at the specified position.
	 *
	 * @param index at which the specified individual is to be inserted.
	 * @param individual individual to be inserted.
	 */
	public void addIndividual(int index, Individual individual);

	/**
	 * Replace the individual at the specified position with @param individual
	 *
	 * @param index at which the specified individual is to be placed.
	 * @param individual individual to be set.
	 */
	public void replaceIndividual(int index, Individual individual);

	/**
	 * Appends all the specified individuals.
	 *
	 * @param list the individuals to be appended.
	 */
	public void addAllIndividuals(IndividualList list);

	/**
	 * Returns the individual at the specified position.
	 *
	 * @param index the index of the individual to return.
	 * @return the individual at the specified position.
	 */
	public Individual getIndividual(int index);

	/**
	 * Removes the individual at the specified position.
	 * @param i index of the individual to be removed.
	 */
	public void removeIndividual(int i);

	/**
	 * Returns <code>true</code> if the specified individual is contained
	 * at least once in the list.
	 *
	 * @param individual individual whose presence in this list is to be tested.
	 * @return <code>true</code> if the individual is contained at least once in the list; <code>false</code> otherwise.
	 */
	public boolean containsIndividual(Individual individual);

	/**
	 * Counts the number of individuals equal to the specified individual.
	 *
	 * @param the individual to which the equal individuals are to be counted.
	 * @return the number of individuals equal to the specified individual.
	 */
	public int getIndividualMultiplicity(Individual individual);

	/**
	 * Returns a randomly chosen individual from within the list.
	 *
	 * @return a randomly chosen individual.
	 * @throws NoSuchIndividualException if the list is empty.
	 */
	public Individual getRandomIndividual();

	/**
	 * Returns all individuals with possible fitness rank
	 * <code>rank</code>.
	 * The fitness rank is the individual's position in a descending sorted
	 * sequence and is thus a value between 1 and the number <code>n</code>
	 * of individuals.
	 * Rank 1 means highest fitness, rank <code>n</code> lowest fitness.<br>
	 * If multiple individuals got the same fitness value, all individuals
	 * are returned that can possibly occupy the specified position in the
	 * according descending sorted sequence. <br>
	 *
	 * @return all inviduals with the specified fitness rank.
	 * @param rank a number between 1 and the size of the individual list specifying the fitness rank.
	 * @throws IllegalArgumentException if the specified rank is out of bounds.
	 * @throws NoSuchIndividualException if no requested individual is found.
	 */
	public IndividualList getAllIndividualsWithRank(int rank) throws IllegalArgumentException, NoSuchIndividualException;

	/**
	 * Returns an individual with the specified fitness rank.
	 * The fitness rank is the individual's position in a descending sorted
	 * sequence and is thus a value between 1 and the number <code>n</code>
	 * of individuals.
	 * Rank 1 means highest fitness, rank <code>n</code> lowest fitness.<br>
	 *
	 * @return an invidual with the specified fitness rank.
	 * @param rank a number between 1 and the size of the individual list specifying the fitness rank.
	 * @throws IllegalArgumentException if the specified rank is out of bounds.
	 * @throws NoSuchIndividualException if no requested individual is found.
	 */
	public Individual getIndividualWithRank(int rank) throws IllegalArgumentException, NoSuchIndividualException;

	/**
	 * Returns the number of individuals in the list.
	 * If an individual is contained <code>x</code> times, it is counted
	 * <code>x</code> times.
	 *
	 * @return the number of individuals in the list.
	 */
	public int size();

	/**
	 * Removes all individuals from the <code>IndividualList</code>
	 */
	public void clear();

	/**
	 * Searches for the first occurence of the specified individual using the
	 * <code>equals</code> method.
	 *
	 * @param an individual.
	 * @return the index of the first occurence of the specified individual; returns <code>-1</code> if the individual is not found.
	 */
	public int indexOf(Individual individual);

	/**
	 * Returns a shallow copy of the list.
	 */
	public Object clone();

	/**
	 * Returns an array containing all individuals in the list in the
	 * correct order.
	 *
	 * @return an array containing all individuals in the list in the correct order.
	 */
	public Individual[] toArray();

	/**
	 * Returns an iterator over the individuals in the list in proper sequence.
	 *
	 * @return an iterator over the individuals in the list in proper sequence.
	 */
	public Iterator iterator();

	/**
	 * Tests if the list is empty.
	 *
	 * @return <code>true</code> if the list is empty; <code>false</code> otherwise.
	 */
	public boolean isEmpty();

}
