/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.searchspace;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.population.*;

/**
 * This class represents the genotype of a general string.
 *
 * @author Michael
 */
public class GeneralStringGenotype extends Genotype {

	private int numChars;
	private int[] genotype;

	/**
	 * Creates a new instance of BitStringGenotype
	 *
	 * @param num is the maximum value-1 in the parameter gen
	 * @param gen will be saved as the genotype of this object
	 */
	public GeneralStringGenotype(int[] gen, int num) {
		genotype = gen;
		numChars = num;
	}

	/**
	 *	Indicates whether some other <code>GeneralStringGenotype</code> is equal
	 * to this one.
	 *
	 * @param o must be of type GeneralStringGenotype
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof GeneralStringGenotype))
			return false;
		int[] genotype2 = ((GeneralStringGenotype)o).genotype;
		if (genotype2.length != genotype.length)
			return false;
		for (int i = 0; i < genotype.length; i++) {
			if (genotype[i] != genotype2[i])
				return false;
		}
		return true;
	}

	public int hashCode() {
		int hash = 261376721;
		for (int i = 0; i < genotype.length; i++) {
			hash = (hash << 2) + genotype[i] + hash / 31;
		}
		return hash;
	}

	/**
	 * Returns a string representation of this
	 * <code>GeneralStringGenotype</code>
	 */
	public String toString() {
		StringBuffer s;

		// case 0..9
		if (numChars < 11) {
			s = new StringBuffer(genotype.length);
			for (int i = 0; i < genotype.length; i++) {
				s.append(genotype[i]);
			}
		} else {
			//case > 9

			s = new StringBuffer(genotype.length * 2 - 1);
			s.append(genotype[0]);
			for (int i = 1; i < genotype.length; i++) {
				s.append("," + genotype[i]);
			}
		}
		return s.toString();
	}

	/**
	 *	Returns the genotype as array of ints
	 */
	public int[] getIntArray() {
		return genotype;
	}

	/**
	 * Sets the char at the specified index to the specified value.
	 */
	public void set(int index, int value) {
		genotype[index] = value;
	}

	/**
	 * Returns the value of the int with the specified index.
	 */
	public int get(int index) {
		return genotype[index];
	}

	/**
	 * Sets the int at the specified index randomly to an another value.
	 */
	public void flip(int index, RandomElement re) {
		int newvalue = re.choose(0, numChars - 2);
		if (newvalue == genotype[index])
			newvalue = (numChars - 1);
		genotype[index] = newvalue;
	}

	public Object clone() {
		GeneralStringGenotype copy;
		try {
			copy = (GeneralStringGenotype)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e.toString());
		}
		copy.genotype = (int[])genotype.clone();
		return copy;
	}
}
