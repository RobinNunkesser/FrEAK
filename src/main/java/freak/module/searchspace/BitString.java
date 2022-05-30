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

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.*;
import freak.core.modulesupport.Configurable;
import freak.core.population.Genotype;
import freak.core.searchspace.*;
import java.util.BitSet;

/**
 * BitString represents the well known searchspace {0,1}^n. This searchspace is implemented seperatly from GeneralString in order to obtain greater performance on this important searchspace.
 * @author  Michael, Heiko
 */
public class BitString extends AbstractSearchSpace implements Configurable, HasMetric, HasDimension {

	private int dimension;
	
	/**
	 * Constructs a new BitString object with default dimension 64.
	 */
	public BitString(Schedule schedule) {
		super(schedule);
		dimension = 64;
	}

	/**
	 * Constructs a new BitString object with dimension <code>dimension</code>.
	 */
	public BitString(Schedule schedule, int dimension) {
		super(schedule);
		this.dimension = dimension;
	}
	
	public String getDescription(){
		return "The search space {0,1}^dimension.";
	}
	
	public String getName(){
		return "Bit String";
	}

	/**
	 * returns 2^dimension
	 */
	public double getSize() {
		return Math.pow(2,dimension);
	}
	
	/**
	 * Returns the hamming distance of gt1 and gt2. The hamming distance is
	 * defined as the number of positions in which gt1 and gt2 differ.
	 */
	public double getDistance(Genotype gt1, Genotype gt2){
		BitStringGenotype bst1 = (BitStringGenotype)gt1;
		BitStringGenotype bst2 = (BitStringGenotype)gt2;
		if (bst1.size()!=bst2.size()) throw new RuntimeException("Size of both genotypes is not equal");
		BitSet bsXOR = ((BitSet)bst1.getBitSet().clone());
		bsXOR.xor(bst2.getBitSet());
		return bsXOR.cardinality();
	}
	
	/**
	 * Returns a random generated Genotype of size dimension.
	 */
	public Genotype getRandomGenotype() {
		BitSet bs = new BitSet(dimension);
		RandomElement re = schedule.getRandomElement();
		if (re == null) throw new NullPointerException("no random element");
		for (int i = 0; i < dimension; i++) {
			if (re.choose(2)==1) bs.set(i);
		}
		return new BitStringGenotype(bs, dimension);
	}
	
	public String getLongDescriptionForDimension() {
		return "The number of bits in all bit strings.";
	}
	
	/**
	 * Sets the dimension of this search space.
	 * @param dim the value to which dimension is set.
	 */
	public void setPropertyDimension(Integer dim) {
		if (dim.intValue() >= 1) dimension = dim.intValue();
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
