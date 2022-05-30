/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.searchspace;

import freak.core.population.*;

/**
 * This class represents the genotype of a permutation.
 *
 * @author Heiko, Michael
 */
public class PermutationGenotype extends Genotype {

	/**
	 * The permutation is stored in the array genotype as function table.
	 */
	private int[] genotype;

	//for better performance
	private String outputBuffer;

	/**
	 * Creates a new object representing the given genotype.
	 * @param genotype the genotype of the new object is set to this genotype.
	 */
	public PermutationGenotype(int[] genotype) {
		this.genotype = genotype;
	}

	public boolean equals(Object o) {
		if ((o == null) | (!(o instanceof PermutationGenotype)))
			return false;
		PermutationGenotype g = (PermutationGenotype)o;
		if (g.genotype.length != genotype.length)
			return false;
		for (int i = 0; i < genotype.length; i++) {
			if (g.genotype[i] != genotype[i])
				return false;
		}
		return true;
	}

	public int hashCode() {
		int n = genotype.length;
		int hashCode = 0;
		int factor = 1;
		for (int i = 0; i < n; i++) {
			hashCode = hashCode + factor * genotype[i];
			factor = factor * n;
		}
		return hashCode;
	}

	public String toString() {
		if (outputBuffer != null)
			return outputBuffer;
		StringBuffer s = new StringBuffer(genotype.length * 2 + 1);
		s.append("(" + genotype[0]);
		for (int i = 1; i < genotype.length; i++) {
			s.append("," + genotype[i]);
		}
		s.append(")");
		outputBuffer = s.toString();
		return outputBuffer;
	}

	public int[] getIntArray() {
		return genotype;
	}

	public Object clone() {
		PermutationGenotype copy;
		try {
			copy = (PermutationGenotype)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e.toString());
		}
		copy.genotype = (int[])genotype.clone();
		return copy;
	}
}
