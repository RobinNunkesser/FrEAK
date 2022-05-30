/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.bitstring;

import java.util.*;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 *
 *
 * @author Michael
 */

public class SP2 extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable {
	
	/*
	 * Parameter which specifies the size of the gap.
	 */
	private int k;
	
	/**
	 * Creates a new object with a standard value of k = 1.
	 *
	 * @param schedule a back-link to the currently used schedule.
	 */
	public SP2(Schedule schedule) {
		super(schedule);
		k = 1;
	}
	
	public double evaluate(Genotype genotype) {
		BitSet bs = ((BitStringGenotype)genotype).getBitSet();
		
		// count the number of 1s in the set
		int cardinality = bs.cardinality();
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		
		int maxOnes = (int)Math.ceil(n/(double)(3*k))*k;
		
		if (cardinality%k==0 && cardinality<=maxOnes) {
			BitSet tbs = new BitSet(n);
			tbs.set(0, cardinality);
			if (bs.equals(tbs)) {
				return n*(cardinality+1);
			}
		}
		if (cardinality%k==0 && cardinality<=maxOnes-k) {
			BitSet tbs = new BitSet(n);
			tbs.set(n-cardinality, n);
			if (bs.equals(tbs)) {
				return n*(cardinality+1);
			}
		}
		return n-cardinality;
	}
	
	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		return n*(Math.ceil(n/(double)(3*k))*k+1);
	}
	
	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}
	
	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}
	
	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		
		BitSet bs = new BitSet(n);
		bs.set(0, (int)Math.ceil(n/(double)(3*k))*k);
		return new BitStringGenotype(bs, n);
	}
	
	/**
	 * Sets the value of the attribute <code>k</code>.
	 *
	 * @param k value you wish attribute <code>k</code> to be.
	 */
	public void setPropertyK(Integer k) {
		if (k.intValue() > 0) {
			this.k = k.intValue();
		}
	}
	
	/**
	 * Returns the value of attribut <code>k</code>.
	 *
	 * @return the wrapped value of attribute <code>k</code>.
	 */
	public Integer getPropertyK() {
		return new Integer(k);
	}
	
	/**
	 * @return a string, which describes the meaning of parameter <code>k</code>
	 */
	public String getLongDescriptionForK() {
		return "k";
	}
	
	public String getShortDescriptionForK() {
		return "k";
	}
	
	public String getDescription() {
		return "Two Short Paths k is very similar to Short Path k but with two short paths, both starting in 0^n. " +
		"One of the form 1^i0^(n-i) leading to the unique global optimum, the other of the " +
		"form 0i1n-i leading to a local optimum. For symmetry reasons, the (1+1) EA " +
		"will reach the local optimum in about half of the runs. Then, a direct mutation " +
		"of at least (2/3)n - k bits is necessary to reach the optimum. Thus, the (1+1) " +
		"EA is very efficient in about half of the runs and very inefficient in about the " +
		"other half of the runs and thus very inefficient on average.";
	}
	
	public String getName() {
		return "Two Short Paths k";
	}
	
}
