/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.searchspace;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.control.*;
import freak.core.modulesupport.Configurable;
import freak.core.population.*;
import freak.core.searchspace.*;

/**
 * GeneralString represents the search space {0,&nbsp;&nbsp;&nbsp;,numChar-1}^n
 * @author  Michael, Heiko
 */
public class GeneralString extends AbstractSearchSpace implements Configurable, HasMetric, HasDimension {
	
	// The search space {0,...numChars-1}^dimension is represented by this class.
	private int numChars;
	private int dimension;
	
	public GeneralString(Schedule schedule) {
		super(schedule);
		dimension = 20;
		numChars = 3;
	}
	
	public String getName() {
		return "General String";
	}
	
	public String getDescription() {
		return "The search space {0,...,k-1}^dimension.";
	}
	
	/**
	 * returns k^dimension
	 */
	public double getSize() {
		return Math.pow(numChars, dimension);
	}
	
	/**
	 * Returns the hamming distance of gt1 and gt2. The hamming distance is
	 * defined as the number of positions in which gt1 and gt2 differ.
	 */
	public double getDistance(Genotype gt1, Genotype gt2) {
		int distance = 0;
		int[] gta1 = ((GeneralStringGenotype)gt1).getIntArray();
		int[] gta2 = ((GeneralStringGenotype)gt2).getIntArray();
		if (gta1.length != gta2.length)
			throw new RuntimeException("Size of both genotypes is not equal");
		for (int i = 0; i < gta1.length; i++) {
			if (gta1[i] != gta2[i])
				distance++;
		}
		return distance;
	}
	
	/** returns a random generated genotype
	 */
	public Genotype getRandomGenotype() {
		int[] genotype = new int[dimension];
		RandomElement re = schedule.getRandomElement();
		if (re == null)
			throw new NullPointerException("no random element");
		for (int i = 0; i < dimension; i++) {
			genotype[i] = re.choose(0, numChars - 1);
		}
		return new GeneralStringGenotype(genotype, numChars);
	}
	
	/**
	 * Sets the number of chars of this searchspace.
	 */
	public void setPropertyNumberOfChars(Integer num) {
		if (num.intValue() > 1)
			numChars = num.intValue();
	}
	
	/**
	 * returns numChars
	 * @uml.property  name="numChars"
	 */
	public int getNumChars() {
		return numChars;
	}
	
	/**
	 * returns numChars
	 */
	public Integer getPropertyNumberOfChars() {
		return new Integer(numChars);
	}
	
	public String getLongDescriptionForDimension() {
		return "The length of all strings.";
	}
	
	public String getLongDescriptionForNumberOfChars() {
		return "The size of one character's domain.";
	}
	
	public String getShortDescriptionForNumberOfChars() {
		return "k";
	}
	
	/**
	 * Sets the dimension of this search space.
	 * @param dim the value to which dimension is set.
	 */
	public void setPropertyDimension(Integer dim) {
		if (dim.intValue() > 1)
			dimension = dim.intValue();
	}
	
	/**
	 * @return the dimension of the search space.
	 */
	public Integer getPropertyDimension() {
		return new Integer(dimension);
	}
	
	/**
	 * Returns the dimension of the seach space.
	 * @return  the dimension of the search space.
	 * @uml.property  name="dimension"
	 */
	public int getDimension() {
		return dimension;
	}
}
