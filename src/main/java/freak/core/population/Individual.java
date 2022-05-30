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
import freak.core.mapper.*;
import java.io.*;
import java.util.*;

/**
 * An <code>Individual</code> consists of a genotype and tags. Tags can store arbitrary data and are used i.e. by parameter controllers to mark certain individuals. Moreover, an individual knows its latest fitness value and its date of birth.<br> What is important here is that individuals are to be handled read-only. Modules that intend to alter attributes such as the genotype or tags have to clone the individual and work on the clone instead. Altering the original individual directly can cause unpredictable results in other parts of FrEAK.
 * @author  Dirk, Heiko, Michael
 */
public class Individual implements Cloneable, Serializable {
	
	public static final int INHERITABLE = 0;
	public static final int NOT_INHERITABLE = 1;
	
	private class Tag implements Serializable {
		public Object key;
		public Object value;
		public int type;
		
		public Tag(Object key, Object value, int type) {
			this.key = key;
			this.value = value;
			this.type = type;
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof Tag)) {
				return false;
			}
			return ((Tag)o).key.equals(((Tag)o).key);
		}
	}
	
	/**
	 * A set of tags that can be attached to an individual.
	 * This can be used to implement i.e. adaptive strategies where an
	 * individual is tracked throughout the graph.
	 */
	private LinkedList tags;
	
	/**
	 * A reference to the gene data of the individual.
	 * The genotype is read only so that several individuals
	 * may reference an instance of <code>Genotype</code>.
	 * When an individual is cloned, only the reference to the genotype is
	 * returned.
	 */
	private Genotype genotype;
	
	/**
	 * genotype -> mapper -> phenotype
	 * This is for caching only.
	 */
	private Genotype phenotype;
	
	/**
	 * The date of birth of the individual.
	 */
	private int dateOfBirth;
	
	/**
	 * A link back to the current schedule.
	 */
	private Schedule schedule;
	
	/**
	 * The latest known fitness is cached.
	 */
	private Double[] latestKnownFitnessValue;
	
	/**
	 * Constructs an individual with the specified genotype and the tags
	 * of its parents.
	 * The tags of the specified parents are merged so that the individual
	 * contains all of its parents' tags. The specified parents are not
	 * stored in any way, their only use is to provide the tags.
	 * The latest known fitness value is set
	 * to <code>null</code> and the date of birth is set to the current
	 * generation.
	 *
	 * @see freak.core.population.Genotype
	 * @param genotype the genotype of the individual.
	 * @param parents the parents of the individual whose tags are to be copied. May be <code>null</code> or an empty array to create an empty set of tags.
	 * @param schedule a link back to the current schedule.
	 */
	public Individual(Schedule schedule, Genotype genotype, Individual[] parents) {
		if (schedule == null || genotype == null)
			throw new NullPointerException("Some of the construction parameters are null.");
		this.schedule = schedule;
		this.genotype = genotype;
		dateOfBirth = schedule.getCurrentGeneration();
		
		tags = new LinkedList();
		if (parents != null) {
			for (int i = 0; i < parents.length; i++) {
				Iterator it = parents[i].tags.iterator();
				while (it.hasNext()) {
					Tag tag = (Tag)it.next();
					if (tag.type == INHERITABLE) {
						if (!tags.contains(tag)) {
							addTag(tag.key,tag.value,INHERITABLE);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Sets the date of birth of the individual.
	 * @param i  the new generation in which the individual has been created.
	 * @uml.property  name="dateOfBirth"
	 */
	public void setDateOfBirth(int i) {
		dateOfBirth = i;
	}
	
	/**
	 * Returns the date of birth of the individual.
	 * @return  the generation in which the individual has been created.
	 * @uml.property  name="dateOfBirth"
	 */
	public int getDateOfBirth() {
		return dateOfBirth;
	}
	
	/**
	 * Returns the genotype of the individual.
	 * @return  the genotype of the individual.
	 * @uml.property  name="genotype"
	 */
	public Genotype getGenotype() {
		return genotype;
	}
	
	/**
	 * Sets the genotype of the individual.
	 * @param genotype  the new genotype.
	 * @uml.property  name="genotype"
	 */
	public void setGenotype(Genotype genotype) {
		this.genotype = genotype;
		// delete phenotype cache
		phenotype = null;
	}
	
	/**
	 * Returns the phenotype of the individual.
	 * @return  the phenotype of the individual.
	 * @uml.property  name="phenotype"
	 */
	public Genotype getPhenotype() {
		if (phenotype != null)
			return phenotype;
		Mapper mapper = schedule.getMapper();
		if (mapper == null)
			return genotype;
		phenotype = mapper.genotypeToPhenotype(genotype);
		return phenotype;
	}
	
	/**
	 * Adds a new tag to the individual.
	 *
	 * @param key the key which identifies the tag.
	 * @param value the information which is to be stored.
	 * @param type the type of the tag (INHERITABLE or NOT_INHERITABLE).
	 */
	public void addTag(Object key, Object value, int type) {
		tags.add(new Tag(key,value,type));
	}
	
	/**
	 * Returns the tag specified by key. If no such tag is available null is
	 * returned instead.
	 * 
	 * @param key the key which specifies the tag to be returned.
	 * @return the tag specified by key.
	 */
	public Object getTag(Object key) {
		Iterator it = tags.iterator();
		while (it.hasNext()) {
			Tag tag = (Tag)it.next();
			if (tag.key.equals(key)) {
				return tag.value;
			}
		}
		return null;
	}
	
	/**
	 * Returns a string representation of the individual containing
	 * the date of birth, the genotype and the tags.
	 *
	 * @return a string representation of the individual.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("date of birth: " + getDateOfBirth() + "; ");
		s.append("genotype: " + getGenotype().toString() + "; ");
		if (schedule.getMapper() != null)
			s.append("phenotype: " + getPhenotype().toString() + "; ");
		if ((tags == null) || (tags.isEmpty()))
			s.append("no tags.");
		else
			s.append("tags: " + tags.toString());
		return s.toString();
	}
	
	/**
	 * Indicates wheather the individual is equal to another object.
	 * An individual equals another individual iff their genotypes, their
	 * dates of birth and their tags are equal.
	 *
	 * @param o the <code>Object</code> to be compared with the individual.
	 * @return <code>true</code> if the individual equals the specified object; <code>false</code> otherwise.
	 */
	public boolean equals(Object o) {
		if ((o == null) || (!(o instanceof Individual)))
			return false;
		Individual individual = (Individual)o;
		return (this.genotype.equals(individual.genotype)) && (this.getDateOfBirth() == individual.getDateOfBirth()) && (this.tags.equals(individual.tags));
	}
	
	/**
	 * Returns a hash code value for the individual. The hash code is calculated
	 * by the xor of the hash codes of genotype, tags and dates of birth.
	 *
	 * @return a hash code value for the individual.
	 */
	public int hashCode() {
		return (getGenotype().hashCode() ^ tags.hashCode()) ^ getDateOfBirth();
	}
	
	/**
	 * Clones the individual by creating a new reference to the read only
	 * genotype and making a shallow copy of the tags.
	 *
	 * @return a clone of the specified individual.
	 */
	public Object clone() {
		// specify the individual itself as parent so that the tags are cloned in the constructor
		Individual individual = new Individual(this.schedule, this.genotype, new Individual[] { this });
		individual.phenotype = phenotype;
		individual.setDateOfBirth(this.getDateOfBirth());
		return individual;
	}
	
	/**
	 * Sets the latest known fitness value. If the value <code>null</code> is specified, the fitness value is assumed to be unknown. This method is be used only by the fitness function to provide a cache if the fitness value for a particular individual does not vary.
	 * @param fitness  the fitness value to be set. Can be <code>null</code> if the fitness value happens to be unknown.
	 * @uml.property  name="latestKnownFitnessValue"
	 */
	public void setLatestKnownFitnessValue(Double[] fitness) {
		latestKnownFitnessValue = fitness;
	}
	
	/**
	 * Returns the latest known fitness value or <code>null</code> if unknown. This is only a cache that may only be used by the fitness function if the fitness value for a particular individual does not vary. To get the correct fitness value, <code>FitnessFunction.evaluate</code> must be called.
	 * @return  the latest known fitness value or <code>null</code> if the fitness is unknown.
	 * @uml.property  name="latestKnownFitnessValue"
	 */
	public Double[] getLatestKnownFitnessValue() {
		return latestKnownFitnessValue;
	}
	
}
