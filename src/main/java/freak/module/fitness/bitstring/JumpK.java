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
 * This class implements the fitness function JumpK. <br>
 * It returns the number of set bits in the input added by n. So far it is 
 * like (a shifted) OneMax, but if the number of set bits is in between 
 * n-k and n (without the borders) the fitness value is set to n - "number of
 * set bits".
 * 
 * So there is a gap "between" the optimum and the search point with values
 * just a little bit lower.
 * 
 * @author Christian, Heiko
 */

public class JumpK extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable {

	/*
	 * Parameter which specifies the size of the gap.
	 */
	private int k;

	/**
	 * Creates a new object with a standard value of k = 3.  
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public JumpK(Schedule schedule) {
		super(schedule);
		k = 3;
	}

	public double evaluate(Genotype genotype) {
		BitSet set = ((BitStringGenotype)genotype).getBitSet();

		// count the number of 1s in the set
		int cardinality = set.cardinality();
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();

		if (cardinality > n - k && cardinality < n)
			return n - cardinality;
		else
			return k + cardinality;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension() + k;
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {

		// the optimum lies in 1^n ...
		int dim = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		BitSet bs = new BitSet(dim);
		bs.set(0, dim);

		return new BitStringGenotype(bs, dim);
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

		return "The parameter k specifies the size of the gap. The gap " + "ranges from n-k+1 to n-1.";

	}

	public String getDescription() {
		return "This fitness function contains a gap the individuals have to jump over " + "to reach the optimum. The gap contains all individuals whose number of ones " + "lies between n-k and n (excluded).\n" + "Individuals not in the gap get fitness k + OneMax.\n" + "Individuals inside the gap get fitness n - OneMax.";
	}

	public String getName() {
		return "JumpK";
	}

}
