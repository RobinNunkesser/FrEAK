/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

import java.util.BitSet;

/**
 * Each position becomes a crossing point with probability prob. So the distance
 * between two crossing points is negative binomially distributed
 * (asymptotically this equals a poisson distribution).
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class PoissonCrossover extends MultiPairwiseCrossover implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	protected double prob;
	
	private int nextPos;
	private int doubleImprecision;
	private double logbasis;
	
	/**
	 * Creates a new PoissonCrossover object.
	 */
	public PoissonCrossover(OperatorGraph graph) {
		super(graph);
		setPropertyProb(new Double(0.1));
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof GeneralString) || (searchspace instanceof BitString)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	/**
	 * Sets the probability that each position becomes a crossing point.
	 */
	public void setPropertyProb(Double prob) {
		if (prob.doubleValue() >= 0 && prob.doubleValue() <= 1) {
			this.prob = prob.doubleValue();
			nextPos = -1;
			logbasis = Math.log(1 - this.prob);
			if (logbasis == 0) {
				doubleImprecision = Integer.MAX_VALUE;
			} else {
				doubleImprecision = (int)Math.floor(Math.log(Math.pow(2, -47)) / logbasis);
			}
		}
	}
	
	/**
	 * @return the probability that each position becomes a crossing point
	 */
	public Double getPropertyProb() {
		return new Double(prob);
	}
	
	public String getLongDescriptionForProb() {
		return "The probability that each position becomes a crossing point";
	}
	
	public String getShortDescriptionForProb() {
		return "Probability";
	}
	
	/** 
	 * Save position information for the next query.
	 */
	private int savePos(int pos, int max) {
		if (pos > max) {
			nextPos = pos - max - 1;
			return -1;
		} else {
			nextPos = -1;
			return pos;
		}
	}
	
	/** 
	 * Returns the next position for the parting point.
	 *
	 * @param max is the maximum return value
	 */
	protected int getNextPosition(int max) {
		//recycle position information
		if (nextPos >= 0) {
			return savePos(nextPos, max);
		}
		RandomElement re = graph.getSchedule().getRandomElement();
		
		/* the prob that no position will be chosen is (1^-pm)^n
		 * the prob that position k will be chosen is (1-pm)^k*pm
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
				nextPosition = getNextPosition(max - doubleImprecision - 1) + doubleImprecision + 1;
			} else {
				nextPosition = -1;
			}
		}
		return savePos(nextPosition, max);
	}
	
	/**
	 * Chooses the crossing points with respect to the given negative binomial
	 * distribution and performs the crossover.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		SearchSpace searchspace = graph.getSchedule().getGenotypeSearchSpace();
		
		int dimension = ((HasDimension)searchspace).getDimension();
		
		if (searchspace instanceof BitString) {
			
			BitSet bs1 = ((BitStringGenotype)ind1.getGenotype()).getBitSet();
			BitSet bs2 = ((BitStringGenotype)ind2.getGenotype()).getBitSet();
			BitSet bsOut = (BitSet)bs1.clone();
			//index i, for iterating
			int i = 0, j;
			int secEnd;
			while (i < dimension) {
				j = getNextPosition(dimension - i - 1);
				if (j != -1) {
					i += j;
					secEnd = getNextPosition(dimension - i - 2) + i + 1;
					// no more parting points, iterate until n-1
					if (secEnd == i) {
						secEnd = dimension;
					}
					
					//second genotype will copied over bsOut
					while (i < secEnd) {
						bsOut.set(i, bs2.get(i));
						i++;
					}
					i++;
				} else {
					//abort
					i = dimension;
				}
			}
			return new Individual(graph.getSchedule(), new BitStringGenotype(bsOut, dimension), new Individual[] { ind1, ind2 });
			
		} else {
			//now generals string
			
			int[] bs1 = ((GeneralStringGenotype)ind1.getGenotype()).getIntArray();
			int[] bs2 = ((GeneralStringGenotype)ind2.getGenotype()).getIntArray();
			int numChars = ((GeneralString)searchspace).getNumChars();
			int[] bsOut = (int[])bs1.clone();
			//index i, for iterating
			int i = 0, j;
			int secEnd;
			while (i < dimension) {
				j = getNextPosition(dimension-i-1);
				if (j != -1) {
					i += j;
					secEnd = getNextPosition(dimension-i-2)+i+1;
					
					// no more parting points, iterate until n-1
					if (secEnd == i) {
						secEnd = dimension;
					}
					
					//second genotype will copied over bsOut
					while (i<secEnd) {
						bsOut[i] = bs2[i];
						i++;
					}
					i++;
				} else {
					//abort
					i = dimension;
				}
			}
			return new Individual(graph.getSchedule(), new GeneralStringGenotype(bsOut, numChars), new Individual[] {ind1, ind2});
		}
	}
	
	public String getDescription() {
		return "Each position becomes a crossing point with probability prob.";
	}
	
	public String getName() {
		return "Poisson Crossover";
	}
	
}
