/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.searchspace;

import java.util.*;

import freak.core.population.*;

/**
 * This class represents the genotype of a BitString.
 *
 * @author Michael
 */
public class BitStringGenotype extends Genotype {

	private BitSet genotype;
	private int dimension;

	/**
	 *  Creates a new instance of BitStringGenotype
	 */
	public BitStringGenotype(BitSet bs, int dim) {
		genotype = bs;
		dimension = dim;
	}

	public BitStringGenotype(boolean[] gen) {
		dimension = gen.length;
		genotype = new BitSet(dimension);
		for (int i = 0; i < dimension; i++) {
			genotype.set(i, gen[i]);
		}
	}

	/**
	 * Indicates whether some other <code>BitStringGenotype</code> is equal
	 * to this one.
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BitStringGenotype))
			return false;
		BitStringGenotype bsg = (BitStringGenotype)o;
		if (bsg.dimension != dimension)
			return false;
		return genotype.equals(bsg.genotype);
	}

	public int hashCode() {
		return genotype.hashCode();
	}

	/**
	 *	Returns a string representation of this <code>BitStringGenotype</code>
	 */
	public String toString() {
		StringBuffer s = new StringBuffer(dimension);
		for (int i = 0; i < dimension; i++) {
			s.append(genotype.get(i) ? '1' : '0');
		}
		return s.toString();
	}

	/**
	 *	Returns the genotype as <code>java.util.BitSet</code>
	 */
	public BitSet getBitSet() {
		return genotype;
	}

	/**
	 *	Returns the size of the bit string
	 */
	public int size() {
		return dimension;
	}

	/**
	 * Sets the bit at the specified index to the specified value.
	 */
	public void set(int bitIndex, boolean value) {
		if (bitIndex < 0 || bitIndex >= dimension)
			throw new IndexOutOfBoundsException();
		genotype.set(bitIndex, value);
	}

	/**
	 * Returns the value of the bit with the specified index.
	 */
	public boolean get(int bitIndex) {
		if (bitIndex < 0 || bitIndex >= dimension)
			throw new IndexOutOfBoundsException();
		return genotype.get(bitIndex);
	}

	/**
	 * Sets the bit at the specified index to the complement of its current value.
	 */
	public void flip(int bitIndex) {
		if (bitIndex < 0 || bitIndex >= dimension)
			throw new IndexOutOfBoundsException();
		genotype.flip(bitIndex);
	}

	/**
	 * Returns the genotype as boolean array
	 */
	public boolean[] getBooleanArray() {
		boolean[] result = new boolean[dimension];
		for (int i = 0; i < dimension; i++) {
			result[i] = genotype.get(i);
		}
		return result;
	}

	public Object clone() {
		BitStringGenotype copy;
		try {
			copy = (BitStringGenotype)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e.toString());
		}
		copy.genotype = (BitSet)genotype.clone();
		return copy;
	}
}
