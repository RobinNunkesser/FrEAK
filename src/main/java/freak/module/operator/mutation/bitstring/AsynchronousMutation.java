/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.bitstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;

/**
 * This operator flips one-bits independently with probability
 * k/CountOfOnesInBitString and null-bits independently with probability
 * k/CountOfNullsInBitString. On the average k one-bits and k null-bits are
 * flipped.
 *
 * @author Michael
 */
public class AsynchronousMutation extends Mutation implements Configurable {
	
	private double k = 1;
	
	public AsynchronousMutation(OperatorGraph graph) {
		super(graph);
	}
	
	public void initialize() {
		super.initialize();
		
		int dim = ((HasDimension)getGenotypeSearchSpace()).getDimension();
		if (k > dim) k = dim; 
	}
	
	public void setPropertyK(Double num) {
		if ((num.doubleValue() >= 0) && (num.doubleValue() <= ((BitString)getGenotypeSearchSpace()).getDimension()))
			k = num.doubleValue();
	}
	
	/**
	 * Returns the number k of positions to flip.
	 */
	public Double getPropertyK() {
		return new Double(k);
	}
	
	public String getLongDescriptionForkK() {
		return "On the average k one-bits and k null-bits are flipped.";
	}
	
	public String getShortDescriptionForK() {
		return "k";
	}
	
	/** 
	 * Returns the next position to mutate.
	 */
	protected int getNextFlipPosition(double prob, int max) {
		int doubleImprecision;
		double logbasis;
		logbasis = Math.log(1 - prob);
		if (logbasis == 0) {
			doubleImprecision = Integer.MAX_VALUE;
		} else {
			doubleImprecision = (int)Math.floor(Math.log(Math.pow(2, -47)) / logbasis);
		}
		
		RandomElement re = graph.getSchedule().getRandomElement();
		
		/* the prob that no position will mutate is (1^-pm)^n
		 * the prob that position k will mutate is (1-pm)^k*pm
		 * so for all possibilities:
		 * sum(0<=k<=n-1){(1-pm)^k*pm}+(1^-pm)^n=1
		 * the random number will fall into one of these possibilities intervals
		 * for position k the interval ends at
		 *    sum(0<=i<=k){(1-pm)^k*pm}
		 *  = sum(0<=i<=k){(1-pm)^k-(1-pm)^(k+1)}
		 *  = 1 - (1-pm)^(k+1)
		 * => k+1 = ceil(log_(1-pm)(1-random number))
		 * => k = floor(log_(1-pm)(random number))
		 * so the random number will assigned to a k
		 */
		int nextPosition;
		// fix bug, because value/0 = negative??? in java
		if (logbasis == 0) {
			nextPosition = -1;
		} else {
			nextPosition = (int)Math.floor(Math.log(re.raw()) / logbasis);
		}
		/* the random generator returns a double between 0 and 1
		 * the mantissa of the type double has ~51 bits
		 * the random number will be very imprecisely after the position
		 * doubleImprecision
		 */
		if (doubleImprecision < nextPosition) {
			if (max > doubleImprecision) {
				nextPosition = getNextFlipPosition(prob, max - doubleImprecision - 1) + doubleImprecision + 1;
			} else {
				nextPosition = -1;
			}
		}
		if (nextPosition > max) nextPosition = -1;
		return nextPosition;
	}
	
	protected Individual doMutation(Individual ind) {
		int dimension = ((BitString)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		BitStringGenotype bs = (BitStringGenotype) ((BitStringGenotype)ind.getGenotype()).clone();
		int numberOfOnes = bs.getBitSet().cardinality();
		
		int onesCount = 0; int nullsCount = 0;
		int[] ones = new int[numberOfOnes];
		int[] nulls = new int[dimension-numberOfOnes];
		for (int i = 0; i < dimension; i++) {
			if (bs.get(i)) {
				// ones
				ones[onesCount++] = i;
			} else {
				//nulls
				nulls[nullsCount++] = i;
			}
		}
		
		double prob = k/onesCount;
		int startindex = 0;
		int nextPos = getNextFlipPosition(prob, onesCount - startindex - 1);
		while (nextPos != -1) {
			bs.flip(ones[startindex + nextPos]);
			startindex += nextPos + 1;
			nextPos = getNextFlipPosition(prob, onesCount - startindex - 1);
		}
		
		prob = k/nullsCount;
		startindex = 0;
		nextPos = getNextFlipPosition(prob, nullsCount - startindex - 1);
		while (nextPos != -1) {
			bs.flip(nulls[startindex + nextPos]);
			startindex += nextPos + 1;
			nextPos = getNextFlipPosition(prob, nullsCount - startindex - 1);
		}
		return new Individual(graph.getSchedule(), bs, new Individual[] { ind });
	}
	
	public String getDescription() {
		return "This operator flips one-bits independently with probability k/CountOfOnesInBitString and null-bits independently with probability k/CountOfNullsInBitString. On the average k one-bits and k null-bits are flipped.";
	}
	
	public String getName() {
		return "Asynchronous Mutation";
	}
}
